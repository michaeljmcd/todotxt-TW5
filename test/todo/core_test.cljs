(ns todo.core-test
  (:require [clojure.test :refer [deftest is]]
            [edessa.parser :refer [apply-parser make-input success? result]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :refer [trim]]
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
    (is (= '({:priority "A" :description ["Fix your stuff dude."] :line-number 0}
             {:priority "Z" :description ["Get ice cream."] :line-number 1})
           (result res)))))

(deftest custom-field-file-test
  (let [inp (make-input (load-resource "test-resources/custom-field.txt"))
        res (apply-parser c/todos inp)]
    (is (success? res))
    (is (= '({:priority "Z" :description ["Do something."] :fields {"due" "2022-01-01" "BADGE" "fOO"} :line-number 0}
             {:priority "B" :description ["Do something else!"] :fields {"due" "2022-03-01"} :line-number 1})
           (result res)))))

(deftest simple-line-test
  (let [inp (make-input "(A) test")
        res (apply-parser c/todo-line inp)]
    (is (success? res))
    (is (= '({:priority "A" :description ["test"] :line-number 0})
           (result res)))))

(deftest tagged-line
  (let [inp (make-input "(D) test +todo in the @cli")
        r (apply-parser c/todo-line inp)]
    (is (success? r))
    (is (= '[{:priority "D"
              :line-number 0
              :description ["test"
                            {:project "todo"}
                            "in the"
                            {:context "cli"}]}]
           (result r))))

  (let [inp (make-input "(D) +todo test in the @cli")
        r (apply-parser c/todo-line inp)]
    (is (success? r))
    (is (= '[{:priority "D"
              :line-number 0
              :description [{:project "todo"}
                            "test in the"
                            {:context "cli"}]}]
           (result r))))

  (let [inp (make-input "(D) add 1 + 2")
        r (apply-parser c/todo-line inp)]
    (is (success? r))
    (is (= '[{:priority "D"
              :line-number 0
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

(deftest description-token-scratch
  (let [inp (make-input "measure space for +chapelShelving @chapel due:2016-05-30")
        r (apply-parser c/description inp)]
    (success? r)))

; Wiki rendering tests

(deftest description-generation
  (let [todo {:description ["something about" {:project "myproject"} "you see"]}
        config {"showProjectsInDescription" true "showContextsInDescription" true}
        r (c/description-cell config todo)]
    (is (= {:type "element"
            :tag "td"
            :children [{:type "text" :text "something about "}
                       {:type "element"
                        :tag "span"
                        :attributes {"class" {:type "string" :value "todo-project"}}
                        :children [{:type "text" :text "myproject "}]}
                       {:type "text" :text "you see "}]}
           r))))

(deftest hide-description-tags
  (let [todo {:description ["something something" {:project "asdf"} " " {:context "def"}]}
        config {"showProjectsInDescription" false "showContextsInDescription" false}
        r (c/description-cell config todo)]
    (is (= {:type "element"
            :tag "td"
            :children [{:type "text" :text "something something "}
                       {:type "text" :text "  "}]}))))

(deftest completion-cell-generation
  (let [todo {:description ["something about" {:project "myproject"} "you see"] :line-number 7}
        config {"showProjectsInDescription" true "showContextsInDescription" true}
        r (c/completion-cell config todo)]
    (is (= {:type "element"
            :tag "td"
            :children [{:type "tickbox"
                        :attributes {"checked" {:type "string" "value" "false"}
                                     "line-number" {:type "string" "value" 7}}}]}

           r))))

; Formatting tests
(deftest todo-to-text-tests
  (let [inp (make-input (load-resource "test-resources/simple.txt"))
        todos (:result (apply-parser c/todos inp))
        str-todos (c/todo-to-text todos)]
    (is (= (trim (load-resource "test-resources/simple.txt"))
           str-todos))))
