(ns brownbag.handler
  (:require [brownbag
             [customer-handler :refer [customer-routes]]
             [middleware :refer [wrap-request-logger wrap-response-logger]]]
            [compojure
             [core :refer :all]
             [handler :as handler]
             [route :as route]]
            [cheshire.core :refer :all]
            [ring.middleware
             [json :refer :all]]
            [ring.util.response :refer [header response status]]
            [liberator.core :refer :all]))

(defresource root
  :available-media-types ["applicaton/json"]
  :allowed-methods [:options]
  :handle-options (fn [_]
                    (generate-string {:version "SNAPSHOT-0.0.1"}))
  :handle-method-not-allowed "method not allowed!")


(defroutes api-routes
  (context "/api" []
           (OPTIONS "/" [] root)
           (context "/customers" []
                    (customer-routes)))
  (route/not-found "Nothing to see here, move along."))

(def app
  (->
   (handler/api api-routes)
   (wrap-json-params)
   (wrap-json-response)
   (wrap-request-logger)
   (wrap-response-logger)))
