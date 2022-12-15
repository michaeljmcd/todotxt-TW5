(ns todo.core-test
  (:require [clojure.test :refer [deftest is]]
            [edessa.parser :refer [apply-parser make-input success? result]]
            [clojure.pprint :refer [pprint]]
            [taoensso.timbre :as t :refer [debug error info merge-config!]]
            [cljs-time.extend :as te]
            [cljs-time.core :as tc]
            [todo.core :as c]))

(merge-config! {:min-level :error})
;:appenders {:println (t/println-appender {:stream *err*})}})

(def fs (js/require "fs"))

(defn load-resource [p]
  (.readFileSync fs p))

(deftest parser-test
  (let [inp (make-input (str (load-resource "test-resources/simple.txt")))
        res (apply-parser c/todos inp)]
    (is (success? res))
    (is (= '({:priority "A" :description "Fix your stuff dude."}
             {:priority "Z" :description "Get ice cream."})
           (result res)))))

(deftest simple-line-test
  (let [inp (make-input "(A) test")
          res (apply-parser c/todo-line inp)]
      (is (success? res))
      (is (= '({:priority "A" :description "test"})
             (result res)))))

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

(deftest date-test
  (let [inp (make-input "1982-03-12")
        r (apply-parser c/a-date inp)]
    (is (success? r))
    (is (tc/= (tc/date-time 1982 3 12)
           (first (result r))))))
