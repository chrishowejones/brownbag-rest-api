(ns brownbag.service.customer-test
  (:require [brownbag.service.customer :refer :all]
            [clojure.test :refer :all]))

(deftest test-get-customer
  (testing "get customer from model"
    (with-redefs [brownbag.models.customer/get-customer-model (fn [_] {:id 1 :name "Chris"})]
      (is (= (get-customer 1)
             {:customer {:id 1 :name "Chris"}}))))
  (testing "nil if user not in model"
    (with-redefs [brownbag.models.customer/get-customer-model (fn [_] nil)]
      (is (= (get-customer 2)
             nil)))))

(deftest test-create-customer
  (testing "create customer"
    (with-redefs [brownbag.models.customer/add-customer (fn [_] {(keyword "scope_identity()") 100})]
      (is (= (create-customer {:name "new customer"})
             100)))))

(deftest test-update-customer
  (testing "update customer"
    (with-redefs [brownbag.models.customer/update-customer (fn [_] 1)
                  get-customer (fn [_] {:customer {:id 100 :name "updated customer"}})]
      (is (= (update-customer {:id 100 :name "updated customer"})
             1)))))
