(defproject brownbag "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [ring-middleware-format "0.5.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j "1.2.17"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/tools.nrepl "0.2.11"]]
  :main brownbag.main
  :aot  [brownbag.main]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler brownbag.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
