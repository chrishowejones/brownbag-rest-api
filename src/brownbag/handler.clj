(ns brownbag.handler
  (:require [brownbag
             [customer-handler :refer [customer-routes]]
             [middleware :refer [wrap-request-logger wrap-response-logger]]]
            [cheshire.core :refer :all]
            [compojure
             [core :refer :all]
             [handler :as handler]
             [route :as route]]
            [liberator.core :refer :all]
            [ring.middleware
             [defaults :refer [wrap-defaults api-defaults]]
             [multipart-params :refer [wrap-multipart-params]]]))

(defresource root
  :available-media-types ["application/json"]
  :allowed-methods [:options]
  :handle-options (generate-string {:version "SNAPSHOT-0.0.1"})
  :handle-method-not-allowed "method not allowed!")


(defroutes api-routes
  (context "/api" []
           (OPTIONS "/" [] root)
           (customer-routes))
  (route/not-found "Nothing to see here, move along."))

(def app
  (->
   api-routes
   (wrap-defaults api-defaults)
   (wrap-multipart-params)
   (wrap-request-logger)
   (wrap-response-logger)))
