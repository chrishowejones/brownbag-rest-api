(ns brownbag.models.customer
  (:require [korma
             [core :refer :all]
             [db :refer :all]]
            [clojure.string :as string]))

(def db {:classname   "org.h2.Driver"
         :subprotocol "h2"
         :subname     "./resources/db/customer;DATABASE_TO_UPPER=false"
         :user        "sa"})

(defdb customer-db db)

(defentity customers)

(defn get-customer-model [id]
  (first
   (select customers
           (where {:id id}))))

(defn add-customer [customer]
  (insert customers
          (values {:id (customer :id) :name (customer :name)})))

(defn update-customer [customer]
  (update customers
          (set-fields {:name (customer :name)})
          (where {:id (customer :id)})))
