(ns migrators.sql.20150515-sql-mig
  (:require [joplin.jdbc.database :refer :all]
            [clojure.java.jdbc :as sql]))

(defn create-customer-table
  "Create customer table"
  [db]
  (sql/db-do-commands db
                      (sql/create-table-ddl
                       :customers
                       [:id "integer PRIMARY KEY"]
                       [:name "varchar(256)"])))

(defn drop-customer-table
  "Drop customer table."
  [db]
  (sql/db-do-commands db
                      (sql/drop-table-ddl
                       :customers)))

(defn up
  "Up migration"
  [db]
  (create-customer-table db))

(defn down
  "Down migration"
  [db]
  (drop-customer-table db))
