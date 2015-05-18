(ns brownbag.customer-handler
  (:require [brownbag.service.customer :refer [get-customer create-customer]]
            [cheshire.core :refer :all]
            [compojure
             [core :refer :all]
             [route :as route]]
            [compojure.core :refer [context]]
            [liberator.core :refer :all]
            [ring.util.response :refer [header response created]]
            [schema.core :as s]
            [clojure.tools.logging :as log]))

(defn- find-customer
  [id]
  (if-let [cust-id (Integer/parseInt id)]
    (get-customer cust-id)))

(def customer-data
  "A schema for customer data"
  {:customer {(s/optional-key :id) s/Int
              :name s/Str
              }})

(def media-types
  ["application/json" "text/plain" "text/html"])

(defn error-in-ctx [ctx]
  (let [error (ctx :error)]
    (log/debug error)
    error))

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
  :malformed? (fn [ctx] (try
                          (let [customer (parse-string
                                                     (slurp
                                                      (get-in ctx [:request :body]))
                                                     true)]
                            (s/validate customer-data customer)
                            (log/info (format "Valid customer: %s" customer))
                            [false {:customer customer}])
                          (catch Exception e
                            (do (log/error e)
                                [true {:error (.getMessage e)}]))))
  :handle-malformed error-in-ctx
  :post! (fn [ctx] (if-let [customer (ctx :customer)]
                     (do
                       (log/debug "Calling create-customer for" customer)
                       {:customer-id (create-customer (customer :customer))})))
  :post-redirect? (fn [ctx] {:location (str "/api/customers/" (ctx :customer-id))}))

(defn customer-routes []
  (routes
   (ANY "/customers/:id" [id] (customer-resources id))
   (ANY "/customers" []
        customers)))
