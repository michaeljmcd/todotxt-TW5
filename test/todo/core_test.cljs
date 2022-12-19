(ns todo.core-test
  (:require [clojure.test :refer [deftest is]]
            [edessa.parser :refer [apply-parser make-input success? result]]
            [clojure.pprint :refer [pprint]]
            [taoensso.timbre :as t :refer [debug error info merge-config!]]
            [cljs-time.extend :as te]
            [cljs-time.core :as tc]
            [todo.core :as c]))

(merge-config! {:min-level :error})

(def fs (js/require "fs"))

(defn load-resource [p]
  (str (.readFileSync fs p)))

(deftest simple-test
  (let [inp (make-input (load-resource "test-resources/simple.txt"))
        res (apply-parser c/todos inp)]
    (is (success? res))
    (is (= '({:priority "A" :description ["Fix your stuff dude."]}
             {:priority "Z" :description ["Get ice cream."]})
           (result res)))))

(deftest custom-field-file-test
  (let [inp (make-input (load-resource "test-resources/custom-field.txt"))
        res (apply-parser c/todos inp)]
    (is (success? res))
    (is (= '({:priority "Z" :description ["Do something."] :fields {"due" "2022-01-01" "BADGE" "fOO"}}
             {:priority "B" :description ["Do something else!"] :fields {"due" "2022-03-01"}})
           (result res)))))

(deftest simple-line-test
  (let [inp (make-input "(A) test")
        res (apply-parser c/todo-line inp)]
    (is (success? res))
    (is (= '({:priority "A" :description ["test"]})
           (result res)))))

(deftest tagged-line
  (let [inp (make-input "(D) test +todo in the @cli")
        r (apply-parser c/todo-line inp)]
    (is (success? r))
    (is (= '[{:priority "D"
              :description ["test"
                            {:project "todo"}
                            "in the"
                            {:context "cli"}]}]
           (result r))))

  (let [inp (make-input "(D) +todo test in the @cli")
        r (apply-parser c/todo-line inp)]
    (is (success? r))
    (is (= '[{:priority "D"
              :description [{:project "todo"}
                            "test in the"
                            {:context "cli"}]}]
           (result r))))

  (let [inp (make-input "(D) add 1 + 2")
        r (apply-parser c/todo-line inp)]
    (is (success? r))
    (is (= '[{:priority "D"
              :description ["add 1 + 2"]}]
           (result r)))))

(deftest date-line-test
  (let [inp (make-input "2021-01-01 Get stuff")
        res (apply-parser c/todo-line inp)
        data (first (result res))]
    (is (success? res))
    (is (not (contains? data :completion-date)))
    (is (tc/= (tc/date-time 2021 1 1) (:creation-date data)))
    (is (= ["Get stuff"] (:description data))))

  (let [inp (make-input "2021-03-01 2021-01-01 Get stuff")
        res (apply-parser c/todo-line inp)
        data (first (result res))]
    (is (success? res))
    (is (tc/= (tc/date-time 2021 3 1) (:completion-date data)))
    (is (tc/= (tc/date-time 2021 1 1) (:creation-date data)))
    (is (= ["Get stuff"] (:description data)))))

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

(deftest project-tag-test
  (let [inp (make-input "+Foobar")
        r (apply-parser c/project-tag inp)]
    (is (success? r))
    (is (= {:project "Foobar"}
           (first (result r))))))

(deftest context-tag-test
  (let [inp (make-input "@Foobar")
        r (apply-parser c/context-tag inp)]
    (is (success? r))
    (is (= {:context "Foobar"}
           (first (result r))))))

(deftest custom-field-test
  (let [inp (make-input "Age:3rd")
        r (apply-parser c/custom-field inp)]
    (is (success? r))
    (is (= {"Age" "3rd"}
           (first (result r))))))

; experimental

(deftest description-token-scratch
  (let [inp (make-input "measure space for +chapelShelving @chapel due:2016-05-30")
        r (apply-parser c/description inp)]
    (success? r)))
