(ns todo.core
  (:require [edessa.parser :refer [apply-parser star parser one-of match then discard not-one-of optional choice plus times]]
            [cljs-time.format :as tf]))

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
  (parser
    (then
      (times 4 digit)
      dash
      (times 2 digit)
      dash
      (times 2 digit))
    :using (fn [x] (tf/parse (apply str x)))))

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
