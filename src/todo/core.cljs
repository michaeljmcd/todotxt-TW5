(ns todo.core
  (:require [edessa.parser :refer [apply-parser star parser one-of match then discard not-one-of optional choice plus times]]))

(defn ^:export hello []
  (print "Hello world"))

; Grammar definition
(def non-breaking-ws
  (parser (one-of [\space \tab])
          :name "Non-breaking whitespace"))

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
  (plus (not-one-of [\newline])))

(def todo-line 
  (parser
    (then
      (optional completion)
      (discard non-breaking-ws)
      (optional priority)
      (discard non-breaking-ws)
      (optional dates)
      (discard non-breaking-ws)
      description)
    ))

(def todos (star todo-line))

(defn parse-todos [text]
  (apply-parser todos text))
