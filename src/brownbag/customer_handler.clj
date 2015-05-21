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

;; convert the body to a reader. Useful for testing in the repl
;; where setting the body to a string is much simpler.
(defn body-as-string [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      java.lang.String body
      (slurp (io/reader body)))))

;; For PUT and POST parse the body as json and store in the context
;; under the given key.
(defn parse-json [ctx key]
  (when (#{:put :post} (get-in ctx [:request :request-method]))
    (try
      (if-let [body (body-as-string ctx)]
        (let [data (json/read-str body)]
          [false {key data}])
        {:message "No body"})
      (catch Exception e
        (.printStackTrace e)
        {:message (format "IOException: %s" (.getMessage e))}))))

;; For PUT and POST check if the content type is json.
(defn check-content-type [ctx content-types]
  (if (#{:put :post} (get-in ctx [:request :request-method]))
    (or
     (some #{(get-in ctx [:request :headers "content-type"])}
           content-types)
     [false {:message "Unsupported Content-Type"}])
    true))

;; we hold a entries in this ref
(defonce entries (ref {}))

;; a helper to create a absolute url for the entry with the given id
(defn build-entry-url [request id]
  (URL. (format "%s://%s:%s%s/%s"
                (name (:scheme request))
                (:server-name request)
                (:server-port request)
                (:uri request)
                (str id))))


;; create and list entries
(defresource list-resource
  :available-media-types ["application/json"]
  :allowed-methods [:get :post]
  :known-content-type? #(check-content-type % ["application/json"])
  :malformed? #(parse-json % ::data)
  :post! #(let [id (str (inc (rand-int 100000)))]
            (dosync (alter entries assoc id (::data %)))
            {::id id})
  :post-redirect? true
  :location #(build-entry-url (get % :request) (get % ::id))
  :handle-ok #(map (fn [id] (str (build-entry-url (get % :request) id)))
                   (keys @entries)))

(defresource entry-resource [id]
  :allowed-methods [:get :put :delete]
  :known-content-type? #(check-content-type % ["application/json"])
  :existed? (fn [_] (nil? (get @entries id ::sentinel)))
  :available-media-types ["application/json"]
  :handle-ok ::entry
  :delete! (fn [_] (dosync (alter entries assoc id nil)))
  :malformed? #(parse-json % ::data)
  :can-put-to-missing? true
  :put! #(dosync (alter entries assoc id (::data %)))
  :new? (fn [_] (nil? (get @entries id ::sentinel)))
  )

(defroutes collection-example
    (ANY ["/collection/:id{[0-9]+}"] [id] (entry-resource id))
    (ANY "/collection" [] list-resource))

(comment
    :malformed? (fn [ctx] (try
              (invalid-schema? ctx)
              (catch Exception e
                (do (log/error e)
                    [true {:error (.getMessage e)}]))))
(fn [ctx] (log/debug "handle-ok") (let [customer (ctx :customer)] (log/debug "returning" customer) customer))
    )
