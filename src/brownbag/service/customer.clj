(ns brownbag.service.customer
  (:require [brownbag.models.customer :as models]))

(defn get-customer [id]
  (let [customer (models/get-customer-model id)]
    (if-not (nil? customer)
      {:customer customer}
      nil)))

(defn create-customer [{:keys [id] :as customer}]
  (->
   customer
   (assoc :id (read-string id))
   (models/add-customer))
  {:customer customer})
