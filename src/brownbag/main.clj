(ns brownbag.main
  (require [brownbag.handler :refer :all]
           [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def server (atom nil))

(defn -main
  "Main function invoked when the application is run from command line."
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server (run-server app {:port port}))
    (println (format "You can view the site on //localhost:%s" port))))
