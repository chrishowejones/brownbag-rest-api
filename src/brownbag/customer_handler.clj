(ns brownbag.customer-handler
  (:require [brownbag.service.customer :refer [get-customer create-customer]]
            [compojure
             [core :refer :all]
             [route :as route]]
            [ring.util.response :refer [header response created]]
            [compojure.core :refer [context]]))

(defn- find-customer
  [id]
  (let [customer (get-customer (read-string id))]
    (if-not (nil? customer)
      (->
       customer
       response
       (header "Allow" "GET")))))

(defn customer-routes []
  (routes
   (GET "/:id" [id]
        (find-customer id))
   (POST "/" [customer]
         (created (str "/api/customers/" (:id customer))
                  (create-customer customer)))))
