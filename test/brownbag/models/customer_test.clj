(ns brownbag.models.customer-test
  (:require [brownbag.models.customer :as models]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as sql]
            [korma.db :refer :all]))

(defn clean-customers [spec]
  (sql/delete! spec :customers []))

(defn seed-customers [spec]
  (sql/insert! spec :customers {:id 1 :name "Chris"})
  (sql/insert! spec :customers {:id 999 :name "Change this"}))

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
    (let [identity ((models/add-customer {:name "Fred"}) (keyword "scope_identity()"))]
      (is (= (-> (sql/query models/db (str "select * from customers where id = " identity))
                 first)
             {:id identity :name "Fred"}))))
  )

(deftest test-update-customer
  (testing "update an existing customer"
    (let [name "Changed Name"
          result (models/update-customer {:id 999 :name name})]
      (is (= (-> (sql/query models/db (str "select * from customers where id=999"))
                 first)
             {:id 999 :name name})))))
