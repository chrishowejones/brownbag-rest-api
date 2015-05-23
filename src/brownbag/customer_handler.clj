(ns brownbag.customer-handler
  (:require [brownbag.service.customer
             :refer
             [create-customer get-customer update-customer]]
            [cheshire.core :refer :all]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [compojure.core :refer :all]
            [liberator.core :refer :all]
            [schema.core :as s])
  (:import java.net.URL))

(defn- find-customer
  [id]
  (if-let [cust-id id]
    (do
      (log/debug "find-customer" id)
      (if-let [customer (get-customer cust-id)]
        {:entry customer}))))

(defn customer-exists?
  [ctx]
  (if-let [id (get-in ctx [:request :customer :id])]
    (do
      (log/debug "Customer id to check =" id )
      (if-let [customer (find-customer id)] ;; find existing customer with id
        (do
          (log/debug "customer exists with value: " customer)
          customer)))))

(defn- new-customer?
  [id]
  (let [new (nil? (find-customer id))]
    (log/debug "new? " new)
    new))

(def customer-data
  "A schema for customer data"
  {:customer {
              :name s/Str
              }})

(def media-types
  ["application/json" "text/plain" "text/html"])

(defn error-in-ctx [ctx]
  (let [error (ctx :message)]
    (log/debug error)
    error))

(defn- post-customer
  [customer]
  (let [new-id (create-customer customer)]
    {:location (str "/api/customers/" new-id)}))

(defn- put-customer
  [id customer]
  (let [updated-customer (assoc customer :id id)]
    (update-customer updated-customer)))

(defn- invalid-schema?
  [ctx]
  (log/debug "check schema")
  (let [body (get-in ctx [:request :body])]
   (if-not (nil? body)
     (when-let [customer (-> body (slurp) (parse-string true))]
       (log/debug "check schema for customer" customer)
       (try
         (s/validate customer-data customer)
         (log/info (format "Valid customer: %s" customer))
         [false {:data customer}]
         (catch Exception e
           (do (log/error e)
               [true {:error (.getMessage e)}]))))
     (do (log/debug "valid-schema") [false ctx]))))

(defn- handle-post-customer
  ([] (handle-post-customer nil))
  ([id]
   (fn [ctx] (when-let [customer (get-in ctx [:data :customer])]
               (log/debug "Calling post-customer for" customer)
               (when-not (nil? id) (assoc customer :id id))
               (post-customer customer)))))

(defresource customer-resources [id]
  :method-allowed? [:get :put]
  :available-media-types media-types
  :malformed? (fn [ctx] (try
              (invalid-schema? ctx)
              (catch Exception e
                (do (log/error e)
                    [true {:message (.getMessage e)}]))))
  :handle-malformed error-in-ctx
  :exists? (let [customer (find-customer id)] (log/debug "exists=" customer) customer)
  :new? (new-customer? id)
  :put! (fn [ctx]
          (log/debug "*** entering put!***")
          (if-let [customer (get-in ctx [:data :customer])]
            (do (log/debug "*** customer=" customer)
                (do
                  (log/debug "Calling put-customer for" customer)
                  (put-customer id customer)))))
  :handle-not-found (format "No customer for id: %s" id)
  :handle-ok :entry)


(defresource customers
  :method-allowed? [:post]
  :available-media-types media-types
  :malformed? (fn [ctx] (try
                          (invalid-schema? ctx)
                          (catch Exception e
                            (do (log/error e)
                                [true {:error (.getMessage e)}]))))
  :exists? #(nil? (customer-exists? %))
  :handle-malformed error-in-ctx
  :post! (handle-post-customer))

(defn customer-routes []
  (routes
   (ANY "/customers/:id" [id] (customer-resources id))
   (ANY "/customers" [] customers)))
