(ns todo.core-test
  (:require [clojure.test :refer [deftest is]]
            [edessa.parser :refer [apply-parser]]
            [clojure.pprint :refer [pprint]]
            [todo.core :refer [todos]]))

(def fs (js/require "fs"))

(defn load-resource [p]
  (.readFileSync fs p))

(deftest parser-test
  (let [inp (load-resource "test-resources/simple.txt")
        res (apply-parser todos inp)]
    (pprint inp)
    (pprint res)
    (is (= 0 res))))
