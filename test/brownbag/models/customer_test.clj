(ns brownbag.models.customer-test
  (:require [brownbag.models.customer :as models]
            [clojure.test :refer :all]))

(deftest test-get-customer-model
  (testing "retrieving customer with id 1"
    (is (= (models/get-customer-model 1)
           {:id 1 :name "Chris"}))))

(deftest test-create-customer
  (testing "create a new customer with id 2"
    (is (= (->>
            (models/add-customer {:id 2 :name "Fred"})
            (filter #(= 2 (:id %)))
            (first))
           {:id 2 :name "Fred"}))))
