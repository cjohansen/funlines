(ns funlines.core-test
  (:require [clojure.test :refer [deftest is]]
            [funlines.core :as sut]))

(deftest runs-a-simple-pipeline
  (is (= (sut/run [[:one (constantly 1)]
                   [:two (comp inc :one)]])
         {:one 1
          :two 2})))

(deftest threads-success-first
  (is (= (sut/ok-> (sut/run [[:one (constantly 1)]
                             [:two (comp inc :one)]])
                   :one
                   inc
                   inc)
         3)))

(deftest threads-success-first
  (is (= (sut/ok-> (sut/run [[:res (constantly 15)]])
                   :res
                   (/ 3))
         5)))

(deftest does-not-thread-failure-first
  (is (= (sut/ok-> (sut/run [[:res (constantly (sut/failure "Uh-oh"))]])
                   :res
                   (/ 3))
         {:funlines.core/failure? true
          :funlines.core/error-message "Uh-oh"
          :funlines.core/step :res})))

(deftest threads-failure-first
  (is (= (sut/failure-> (sut/run [[:res (constantly (sut/failure "Uh-oh"))]])
                        ::sut/error-message
                        (str "!!"))
         "Uh-oh!!")))

(deftest does-not-thread-success-first
  (is (= (sut/failure-> (sut/run [[:res (constantly 15)]])
                        ::sut/error-message
                        (str "!!"))
         {:res 15})))

(deftest threads-success-last
  (is (= (sut/ok->> (sut/run [[:res (constantly 15)]])
                    :res
                    (/ 3))
         1/5)))

(deftest does-not-thread-failure-last
  (is (= (sut/ok->> (sut/run [[:res (constantly (sut/failure "Uh-oh"))]])
                    :res
                    (/ 3))
         {:funlines.core/failure? true
          :funlines.core/error-message "Uh-oh"
          :funlines.core/step :res})))

(deftest threads-failure-last
  (is (= (sut/failure->> (sut/run [[:res (constantly (sut/failure "Uh-oh"))]])
                         ::sut/error-message
                         (str "!!"))
         "!!Uh-oh")))

(deftest does-not-thread-success-last
  (is (= (sut/failure->> (sut/run [[:res (constantly 15)]])
                         ::sut/error-message
                         (str "!!"))
         {:res 15})))
