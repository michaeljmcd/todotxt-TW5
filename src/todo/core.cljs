(ns todo.core
  (:require [edessa.parser :refer [star parser one-of]]))

(defn ^:export hello []
  (print "Hello world"))

; Grammar definition
(def non-breaking-ws
  (parser (one-of [\space \tab])
          :name "Non-breaking whitespace"))

(def todo-line non-breaking-ws)

(def todos (star todo-line))
