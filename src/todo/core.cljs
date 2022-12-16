(ns todo.core
  (:require [edessa.parser :refer [apply-parser star parser one-of match then discard not-one-of optional choice plus times]]
            [cljs-time.format :as tf]))

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

(def description-text
  (parser
    (plus 
      (choice 
        (then (one-of [\@ \+])
              (not-one-of (concat UCASE-LETTERS LCASE-LETTERS [\newline])))
        (not-one-of [\newline \@ \+])))
    :using stringify))

(def description-content
  (parser
      (plus
        (choice
          description-text
          context-tag
          project-tag))
      :using (fn [x] {:description (into [] x)})))

(def custom-fields
  (parser
    (star 
      (then
        custom-field
        (discard (star non-breaking-ws))))
    :using (fn [x] {:fields (apply merge x)})))

(def description
  (parser
    (then
      description-content
      custom-fields)
  :using (fn [x]
           (apply merge 
                  (filter (fn [x] (or (not (contains? x :fields))
                                      (not (nil? (:fields x))))) x))
           )))

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

(defn parse-todos [text]
  (apply-parser todos text))
