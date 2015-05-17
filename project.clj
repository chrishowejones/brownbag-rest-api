(defproject brownbag "0.1.0-SNAPSHOT"
  :description "Brown bag demo"
  :url "http://www.howe-jones.co.uk"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.4"]
                 [ring/ring-defaults "0.1.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j "1.2.17"]
                 [ring/ring-json "0.3.1"]
                 [liberator "0.13"]
                 [cheshire "5.4.0"]
                 [korma "0.4.1"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [com.h2database/h2 "1.4.186"]
                 [joplin.core "0.2.10"]
                 [joplin.jdbc "0.2.10"]]
  :source-paths ["src" "joplin"]
  :plugins [[lein-ring "0.8.13"]
            [joplin.lein "0.2.10"]
            [lein-cloverage "1.0.2"]]
  :ring {:handler brownbag.handler/app}
  :joplin {:migrators
             {:sql-mig "joplin/migrators/sql"}  ;; A path for a folder with migration files
           :databases
             {:sql {:type :jdbc, :url "jdbc:h2:./resources/db/customer;USER=sa;DATABASE_TO_UPPER=false"}}
           :environments
           {:dev [{:db :sql :migrator :sql-mig}]}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}
    :aliases
  {
   "test-dev" ["do" ["joplin" "reset" "dev" "sql"] ["test"]]})
