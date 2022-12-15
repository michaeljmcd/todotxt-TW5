(ns todo.core-test
  (:require [clojure.test :refer [deftest is]]
            [edessa.parser :refer [apply-parser make-input success? result]]
            [clojure.pprint :refer [pprint]]
            [todo.core :as c]))

(def fs (js/require "fs"))

(defn load-resource [p]
  (.readFileSync fs p))

;(deftest parser-test
;  (let [inp (load-resource "test-resources/simple.txt")
;        res (apply-parser c/todos inp)]
;    (pprint inp)
;    (pprint res)
;    (is (= 0 res))))

; Low level parser tests
(deftest priority-test
  (let [inp (make-input "(A)")
        r (apply-parser c/priority inp)]
    (is (success? r))
    (is (= '{:input (),
             :position 3,
             :line-number 0,
             :column 3,
             :result ({:priority "A"})
             :error nil,
             :failed false}
           r))))
