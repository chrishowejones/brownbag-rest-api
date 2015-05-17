(ns brownbag.handler-test
  (:require [clojure.test :refer :all]
            [brownbag.handler :refer :all]
            [ring.mock.request :as mock]
            [migrators.sql.20150515-sql-mig :refer :all]
            [clojure.java.jdbc :as sql]
            [brownbag.models.customer :refer :all]
            [korma.db :refer :all]))

(defn clean-customers [spec]
  (sql/delete! spec :customers []))

(defn seed-customers [spec]
  (sql/insert! spec :customers {:id 1 :name "Chris"}))

(defn database-fixture [f]
  (clean-customers db)
  (seed-customers db)
  (f))

(use-fixtures :once database-fixture)

(deftest test-api
  (testing "Options"
    (let [response (app (mock/request :options "/api/"))]
      (is (= (:status response) 200))
      (is (= (-> response
                 :body)
             "{\"version\":\"SNAPSHOT-0.0.1\"}"))
      (is (= ((:headers response) "Allow")
             "OPTIONS"))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/api/invalid"))]
      (is (= (:status response) 404)))))

(deftest test-root
  (testing "not found on root"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 404)))))

(deftest test-get-customer
  (testing "user id 1 found"
    (let [response (app (mock/request :get "/api/customers/1"))
          json-str (-> response
                       :body)]
      (is (= (:status response) 200))
      (println "response=" response ", body=" json-str)
      (is (= (->> json-str
                  (re-find #"\{\"customer\":\{.*(\"name\":\"Chris\").*\}\}")
                  second)
             "\"name\":\"Chris\""))
      (is (= (->> json-str
                  (re-find #"\{\"customer\":\{.*(\"id\":1).*\}\}")
                  second) "\"id\":1"))))
  (testing "user id 3 not found"
    (let [response (app (mock/request :get "/api/customers/3"))]
      (is (= (:status response) 404)))))

(deftest test-post-customer
  (testing "post a customer"
    (with-redefs [brownbag.models.customer/add-customer (fn [_] {(keyword "scope_identity()") 99})]
      (let [response (app (mock/content-type (mock/request :post "/api/customers"
                                                           "{\"customer\":{\"name\":\"Bill\"}}")
                                             "application/json"))
            status (:status response)
            location (get-in response [:headers "Location"])]
        (is (= location "/api/customers/99"))
        (is (= status 303))))))

(deftest test-post-invalid-customer
  (testing "post an invalid customer"
    (let [response (app (mock/content-type (mock/request :post "/api/customers"
                                                         "{\"customer\":{\"invalid\": \"invalid value\", \"name\":\"Bill\"}}")
                                           "application/json"))
          status (:status response)]
      (println response)
      (is (= status 400)))))
