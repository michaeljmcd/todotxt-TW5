(ns todo.core
  (:require [edessa.parser :refer [apply-parser star parser one-of match then discard not-one-of optional choice plus times]]))

(defn ^:export hello []
  (print "Hello world"))

; Grammar definition
(def non-breaking-ws
  (parser (one-of [\space \tab])
          :name "Non-breaking whitespace"))

(def nl
  (parser (match \newline)
          :name "Newline"
          :using (fn [_] {:type :newline :value (str \newline)})))

(def ucase-letter (one-of "ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
(def digit (one-of "0123456789"))
(def dash (match \-))

(def a-date
  (then
    (times 4 digit)
    dash
    (times 2 digit)
    dash
    (times 2 digit)))

(def priority 
  (parser
    (then
      (discard (match \())
      ucase-letter
      (discard (match \))))
    :using (fn [x] {:priority (first x)})))

(def completion (match \x))

(def dates
  (choice (then a-date (discard non-breaking-ws) a-date)
          (then a-date)))

(def description
  (parser
    (plus (not-one-of [\newline]))
    :using (fn [x] {:description (apply str x)})))

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
