(ns brownbag.main
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [brownbag.handler :refer [app]]
            [clojure.tools.nrepl.server :refer [start-server stop-server]])
  (:gen-class))

(defonce server (atom nil))
(defonce repl-server (atom nil))

(defn -main [& args]
  (doall
   (reset! repl-server (start-server :port 7888))
   (reset! server (run-jetty app {:port 8080}))))
