(ns brownbag.models.customer-test
  (:require [brownbag.models.customer :as models]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as sql]
            [korma.db :refer :all]))

(defn clean-customers [spec]
  (sql/delete! spec :customers []))

(defn seed-customers [spec]
  (sql/insert! spec :customers {:id 1 :name "Chris"}))

(defn database-fixture [f]
  (clean-customers models/db)
  (seed-customers models/db)
  (f))

(use-fixtures :once database-fixture)

(deftest test-get-customer-model
  (testing "retrieving customer with id 1"
    (is (= (models/get-customer-model 1)
           {:id 1 :name "Chris"}))))

(deftest test-create-customer
  (testing "create a new customer with id 2"
    (is (= (do
             (models/add-customer {:id 2 :name "Fred"})
             (->
              (sql/query models/db "select * from customers where id = 2")
              first))
           {:id 2 :name "Fred"}))))
