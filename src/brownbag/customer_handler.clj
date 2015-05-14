(ns brownbag.customer-handler
  (:require [brownbag.service.customer :refer [get-customer create-customer]]
            [compojure
             [core :refer :all]
             [route :as route]]
            [ring.util.response :refer [header response created]]
            [compojure.core :refer [context]]
            [liberator.core :refer :all]))

(defn- find-customer
  [id]
  (let [customer (get-customer (read-string id))]
           customer))

(defresource customer-resources [id]
  :available-media-types ["application/json" "text/plain" "text/html"]
  :exists? (fn [_] (if-let [customer (find-customer id)]
                       {:customer customer}))
  :handle-not-found (format "No customer for id: %s" id)
  :handle-ok (fn [ctx] (ctx :customer)))

(defn customer-routes []
  (routes
   (ANY "/customers/:id" [id] (customer-resources id))
   (POST "/customers/" [customer]
         (created (str "/api/customers/" (:id customer))
                  (create-customer customer)))))

(comment   :exists? (fn [ctx] (if-let [customer (find-customer id)]
                      {:customer customer})))
