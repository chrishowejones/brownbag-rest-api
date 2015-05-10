(ns brownbag.service.customer
  (:require [brownbag.models.customer :as models]))

(defn get-customer [id]
  (let [customer (models/get-customer-model id)]
    (if-not (nil? customer)
      {:customer customer}
      nil)))

(defn create-customer [{:keys [id] :as customer}]
  (->
   (if (instance? java.lang.String id)
     (assoc customer :id (read-string id))
     customer)
   (models/add-customer))
  {:customer customer})
