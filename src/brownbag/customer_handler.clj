(ns brownbag.customer-handler
  (:require [brownbag.service.customer :refer [get-customer create-customer
                                               update-customer]]
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
  (if-let [cust-id id]
    (do
      (log/debug "find-customer" id)
      (if-let [customer (get-customer cust-id)]
        {:customer customer}))))

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
  [ctx]
  (not (customer-exists?)))

(def customer-data
  "A schema for customer data"
  {:customer {
              :name s/Str
              }})

(def media-types
  ["application/json" "text/plain" "text/html"])

(defn error-in-ctx [ctx]
  (let [error (ctx :error)]
    (log/debug error)
    error))

(defn- post-customer
  [customer]
  (let [new-id (create-customer customer)]
    {:location (str "/api/customers/" new-id)}))

(defn- put-customer
  [customer]
  (update-customer customer))

(defn- check-schema
  [ctx]
  (log/debug "check schema")
  (let [body (get-in ctx [:request :body])]
   (if-not (nil? body)
     (when-let [customer (-> body  (slurp) (parse-string true))]
       (log/debug "check schema for customer" customer)
       (try
         (s/validate customer-data customer)
         (log/info (format "Valid customer: %s" customer))
         [false {:customer customer}]
         (catch Exception e
           (do (log/error e)
               [true {:error (.getMessage e)}])))))))

(defn- handle-post-customer
  ([] (handle-post-customer nil))
  ([id]
   (fn [ctx] (when-let [customer (get-in ctx [:customer :customer])]
               (log/debug "Calling post-customer for" customer)
               (when-not (nil? id) (assoc customer :id id))
               (post-customer customer)))))

(defresource customer-resources [id]
  :method-allowed? [:get]
  :available-media-types media-types
  :malformed? (fn [ctx] (try
              (check-schema ctx)
              (catch Exception e
                (do (log/error e)
                    [true {:error (.getMessage e)}]))))
  :handle-malformed error-in-ctx
  :exists? (let [customer (find-customer id)] (log/debug "exists=" customer) customer)
  :put! (fn [ctx]
          (log/debug "*** entering put!***")
          (if-let [customer (get-in ctx [:customer :customer])]
            (do (log/debug "*** customer=" customer)
                (do
                  (log/debug "Calling put-customer for" customer)
                  (put-customer customer)))))
  :handle-not-found (format "No customer for id: %s" id)
  :handle-ok (fn [ctx] (ctx :customer)))


(defresource customers
  :method-allowed? [:post]
  :available-media-types media-types
  :malformed? (fn [ctx] (try
                          (check-schema ctx)
                          (catch Exception e
                            (do (log/error e)
                                [true {:error (.getMessage e)}]))))
  :exists? #(nil? (customer-exists? %))
  :new new-customer?
  :Handle-malformed error-in-ctx
  :post! (handle-post-customer))

(defn customer-routes []
  (routes
   (ANY "/customers/:id" [id] (customer-resources id))
   (ANY "/customers" [] customers)))

(comment
  :put! (fn [ctx]
          (log/debug "*** entering put!***")
          (if-let [customer (get-in ctx [:customer :customer])]
            (do (log/debug "*** customer=" customer)
                (do
                  (log/debug "Calling put-customer for" customer)
                  (put-customer customer)))))
  )
