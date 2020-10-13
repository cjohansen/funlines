(ns funlines.core-test
  (:require [funlines.core :as sut]
            [clojure.test :refer [deftest is]]))

(deftest runs-a-simple-pipeline
  (is (= (sut/run [[:one (constantly 1)]
                   [:two (comp inc :one)]])
         {:one 1
          :two 2})))
