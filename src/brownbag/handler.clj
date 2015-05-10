(ns brownbag.handler
  (:require [brownbag
             [customer-handler :refer [customer-routes]]
             [middleware :refer [wrap-request-logger wrap-response-logger]]]
            [compojure
             [core :refer :all]
             [handler :as handler]
             [route :as route]]
            [ring.middleware
             [json :refer :all]]
            [ring.util.response :refer [header response status]]))

(defroutes api-routes
  (context "/api" []
           (OPTIONS "/" []
                    (->
                     (response {:version "SNAPSHOT-0.0.1"})
                     (header "Allow" "OPTIONS")))
           (context "/customers" []
                    (customer-routes))
           (ANY "/" []
                (->
                 (response nil)
                 (status 405)
                 (header "Allow" "OPTIONS"))))
  (route/not-found "Nothing to see here, move along."))

(def app
  (->
   (handler/api api-routes)
   (wrap-json-params)
   (wrap-json-response)
   (wrap-request-logger)
   (wrap-response-logger)))
