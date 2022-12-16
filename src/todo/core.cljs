(ns todo.core
  (:require [edessa.parser :refer [apply-parser star parser one-of match then discard not-one-of optional choice plus times using result]]
            [taoensso.timbre :as t :refer [debug error info merge-config!]]
            [clojure.string :refer [trim]]
            [cljs-time.format :as tf]))

(merge-config! {:min-level :error})

(defn ^:export hello []
  (print "Hello world"))

; Grammar definition
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

(defn convert-todo [todo]
  {:type "element" :tag "h1" :children [{:type "text" :text (first (:description todo))}]})

(defn convert-parse-tree [todos]
  (if (empty? todos)
    [{:type "text" :text "Nothing to do!"}]
    (map convert-todo todos)))

(defn ^:export parse-todos [text]
  (apply-parser todos text))

(defn ^:export todo-to-wiki [text]
  (let [res (parse-todos text)]
    (-> res
        result
        convert-parse-tree
        clj->js)))
