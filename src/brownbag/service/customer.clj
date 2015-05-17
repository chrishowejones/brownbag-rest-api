(ns brownbag.service.customer
  (:require [brownbag.models.customer :as models]))

(defn get-customer [id]
  (let [customer (models/get-customer-model id)]
    (if-not (nil? customer)
      {:customer customer}
      nil)))

(defn create-customer [customer]
  (->
   customer
   (models/add-customer)
   (get (keyword "scope_identity()"))))
