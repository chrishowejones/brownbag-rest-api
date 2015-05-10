(ns brownbag.service.customer-test
  (:require [brownbag.service.customer :refer :all]
            [clojure.test :refer :all]))

(deftest test-get-customer
  (testing "get user from model"
    (with-redefs [brownbag.models.customer/get-customer-model (fn [_] {:id 1 :name "Chris"})]
      (is (= (get-customer 1)
             {:customer {:id 1 :name "Chris"}}))))
  (testing "nil if user not in model"
    (with-redefs [brownbag.models.customer/get-customer-model (fn [_] nil)]
      (is (= (get-customer 2)
             nil)))))
