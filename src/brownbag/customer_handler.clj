(ns brownbag.customer-handler
  (:require [brownbag.service.customer :refer [get-customer create-customer]]
            [cheshire.core :refer :all]
            [compojure
             [core :refer :all]
             [route :as route]]
            [compojure.core :refer [context]]
            [liberator.core :refer :all]
            [ring.util.response :refer [header response created]]))

(defn- find-customer
  [id]
  (if-let [cust-id (Integer/parseInt id)]
    (get-customer cust-id)))

(def media-types
  ["application/json" "text/plain" "text/html"])

(defresource customer-resources [id]
  :available-media-types media-types
  :exists? (fn [_]
             (if-let [customer (find-customer id)]
               {:customer customer}))
  :handle-not-found (format "No customer for id: %s" id)
  :handle-ok (fn [ctx] (ctx :customer)))

(defresource customers
  :method-allowed? [:get :post]
  :available-media-types media-types
  :post! (fn [ctx] (if-let [customer (parse-string (slurp
                                                    (get-in ctx [:request :body])) true)]
                     (create-customer (customer :customer))))
  :handle-ok {:customer nil})

(defn customer-routes []
  (routes
   (ANY "/customers/:id" [id] (customer-resources id))
   (ANY "/customers" []
        customers)))
