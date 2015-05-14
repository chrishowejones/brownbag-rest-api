(ns brownbag.handler-test
  (:require [clojure.test :refer :all]
            [brownbag.handler :refer :all]
            [ring.mock.request :as mock]))

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
      (is (= (->> json-str
                  (re-find #"\{\"customer\":\{.*(\"name\":\"Chris\").*\}\}")
                  second)
             "\"name\":\"Chris\""))
      (is (= (->> json-str
                  (re-find #"\{\"customer\":\{.*(\"id\":1).*\}\}")
                  second) "\"id\":1"))))
  (testing "user id 2 not found"
    (let [response (app (mock/request :get "/api/customers/3"))]
      (is (= (:status response) 404)))))

(deftest test-post-customer
  (testing "post a user"
    (let [response (app (mock/content-type (mock/request :post "/api/customers"
                                                         "{\"customer\":{\"id\":\"2\",\"name\":\"Fred\"}}")
                                      "application/json"))]
      (is true))))

(app (mock/content-type (mock/request :get "/api/customers/1") "application/json"))
