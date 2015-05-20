(ns brownbag.models.customer
  (:require [clojure.tools.logging :as log]
            [korma
             [core :refer :all]
             [db :refer :all]]))

(def db {:classname   "org.h2.Driver"
         :subprotocol "h2"
         :subname     "./resources/db/customer;DATABASE_TO_UPPER=false"
         :user        "sa"})

(defdb customer-db db)

(defentity customers)

(defn get-customer-model [id]
  (log/debug "get-customer-model for" id)
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
