(ns brownbag.service.customer
  (:require [brownbag.models.customer :as models]
            [clojure.tools.logging :as log]))

(defn get-customer
  "Get a customer from id"
  [id]
  (log/debug "Calling get-customer for" id)
  (let [customer (models/get-customer-model id)]
    (log/debug "customer found =" customer)
    (if-not (nil? customer)
      {:customer customer}
      nil)))

(defn create-customer
  "Create customer"
  [customer]
  (log/debug "Calling create-customer for " customer)
  (->
   customer
   (models/add-customer)
   (get (keyword "scope_identity()"))))

(defn update-customer [customer]
  (log/debug "Calling update-customer for " customer)
  (models/update-customer customer))
