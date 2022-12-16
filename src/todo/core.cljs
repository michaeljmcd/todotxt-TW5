(ns todo.core
  (:require [edessa.parser :refer [apply-parser star parser one-of match then discard not-one-of optional choice plus times using result]]
            [taoensso.timbre :as t :refer [debug error info merge-config!]]
            [clojure.string :refer [trim]]
            [cljs-time.format :as tf]))

; The main goal here is to write a plugin that can parser [TODO.txt](https://github.com/todotxt/todo.txt)
; and render it as HTML within TiddlyWiki 5. This module will have all of the
; parsing and HTML 5 generation in it. There will be separate `.js` files to act
; as the glue between the core module and TiddlyWiki itself. Most importantly,
; one of those is the parser module that needs to be registered for the custom
; format to be rendered.

; Disable verbose logging from Edessa.
(merge-config! {:min-level :error})

; Grammar definition.
; The grammar here is built up on the (Edessa)[https://github.com/michaeljmcd/edessa] parser 
; combinator library. The format being parsed is linked above. What we are after
; is a list of todo entries, each of which follows the format below:
;
; {:complete true
;  :priority "A"
;  :creation-date "2021-01-01"
;  :completion-date "2021-01-01"
;  :description ["My text with" 
;                {:project "project"}
;                {:context "context"}
;                "tags"]
;  :fields {"due" "2021-01-01"}}
;
; The format closely mirrors what you would expect the biggest call-outs are
; probably the separate tokenization of tags and fields. The idea here was to
; make room for later pretty-rendering.
(defn stringify [x]
  (apply str x))

(def non-breaking-ws
  (parser (one-of [\space \tab])
          :name "Non-breaking whitespace"))

(def nl (match \newline))

(def ws (choice non-breaking-ws nl))

(def UCASE-LETTERS "ABCDEFGHIJKLMNOPQRSTUVWXYZ")
(def LCASE-LETTERS "abcdefghijklmnopqrstuvwxyz")

(def ucase-letter (one-of UCASE-LETTERS))
(def lcase-letter (one-of LCASE-LETTERS))
(def digit (one-of "0123456789"))
(def dash (match \-))
(def underscore (match \_))

(def a-date
  (parser
    (then
      (times 4 digit)
      dash
      (times 2 digit)
      dash
      (times 2 digit))
    :using (fn [x] (tf/parse (stringify x)))))

(def priority 
  (parser
    (then
      (discard (match \())
      ucase-letter
      (discard (match \))))
    :using (fn [x] {:priority (first x)})))

(def completion 
  (parser (match \x)
          :using (fn [_] {:complete true})))

(def dates
  (choice (parser (then a-date (discard non-breaking-ws) a-date)
                  :using (fn [x] {:completion-date (first x) :creation-date (second x)}))
          (parser a-date :using (fn [x] {:creation-date (first x)}))))

(def identifier
  (parser
    (then 
      (choice ucase-letter lcase-letter)
      (star (choice ucase-letter lcase-letter digit dash underscore)))
    :using stringify))

(def project-tag
  (parser
    (then
      (discard (match \+))
      identifier)
    :using (fn [x] {:project (stringify x)})))

(def context-tag 
  (parser
    (then
      (discard (match \@))
      identifier)
    :using (fn [x] {:context (stringify x)})))

(def custom-value
  (parser 
    (plus (not-one-of [\space \tab \newline]))
    :using stringify))

(def custom-field
  (parser
    (then
      identifier
      (discard (match \:))
      custom-value)
    :using (fn [x] (apply (partial assoc {}) x))))

(defn- custom-field? [x] (and (map? x)
                              (not (contains? x :project)) 
                              (not (contains? x :context))))

(defn str-accumulate [xs]
  (letfn [(str-acc-inner [xs st res]
            (cond
              (empty? xs)
                (if (empty? st)
                  res
                  (cons st res))
              (string? (first xs))
                (recur (rest xs)
                       (str st (first xs))
                       res)
                :else
                          (if (empty? st)
                            (recur (rest xs) "" (cons (first xs) res))
                            (recur (rest xs) "" (cons (first xs) (cons st res))))))]
    (reverse (str-acc-inner xs "" []))
  ))

(def description
  (parser
    (star
      (choice 
        context-tag
        project-tag
        custom-field
        (using (plus (not-one-of [\newline \tab \space])) stringify)
        non-breaking-ws
        ))
    :using (fn [x]
             (let [res {:description (map (fn [x] (if (string? x)
                                                    (trim x)
                                                    x))
                                          (str-accumulate (filter (comp not custom-field?) x)))
                        }
                   fields (filter custom-field? x)]
               (if (empty? fields)
                 res
                 (assoc res :fields (apply merge fields)))))
    ))

(def todo-line 
  (parser
    (then
      (optional completion)
      (discard (star non-breaking-ws))
      (optional priority)
      (discard (star non-breaking-ws))
      (optional dates)
      (discard (star non-breaking-ws))
      description)
    :using (fn [x] (apply merge x))))

(def todos
  (star
    (then todo-line
          (discard (optional nl)))))

; With the parsing business out of the way, we will turn towards wiki rendering.

(def cell 
      {:type "element"
       :tag "td"
       :children []})

(def row 
  {:type "element"
   :tag "tr"
   :children []})

(defn completion-cell [todo]
   (if (and (contains? todo :complete)
            (:complete todo))
     (assoc cell :children [{:type "raw" :html "&#x2611;"}])
     (assoc cell :children [{:type "raw" :html "&#x2610;"}])
     ))

(defn priority-cell [todo]
     (assoc cell :children [{:type "text" :text (:priority todo)}]))

(defn completion-date-cell [todo]
  (if (contains? todo :completion-date)
    (assoc cell :children [{:type "text" :text (tf/unparse (tf/formatters :date) (:completion-date todo))}])
    (assoc cell :children [{:type "text" :text ""}])
    ))

(defn creation-date-cell [todo]
  (if (contains? todo :creation-date)
    (assoc cell :children [{:type "text" :text (tf/unparse (tf/formatters :date) (:creation-date todo))}])
    (assoc cell :children [{:type "text" :text ""}])
    ))

(defn convert-todo [todo]
   (assoc row
          :children
          [
           (completion-cell todo)
           (priority-cell todo)
           (completion-date-cell todo)
           (creation-date-cell todo)
           ]))

(def header 
  {:type "element"
         :tag "table"
         :children [
                    {:type "element" :tag "thead"
                     :children [
                        {:type "element" :tag "tr"
                         :children [
                              {:type "element" :tag "td" :children [{:type "text" :text "Complete?"}]}
                              {:type "element" :tag "td" :children [{:type "text" :text "Priority"}]}
                              {:type "element" :tag "td" :children [{:type "text" :text "Completed At"}]}
                              {:type "element" :tag "td" :children [{:type "text" :text "Created At"}]}
                              {:type "element" :tag "td" :children [{:type "text" :text "Description"}]}
                              ]}]}
                    ]})

(defn convert-parse-tree [todos]
  (if (empty? todos)
    [{:type "text" :text "Nothing to do!"}]
    [(assoc header
           :children
           (concat (:children header)
                    (map convert-todo todos)))]))

(defn ^:export parse-todos [text]
  (clj->js (apply-parser todos text)))

(defn ^:export todo-to-wiki [text]
    (->> text
         (apply-parser todos)
         result
         convert-parse-tree
         clj->js))
