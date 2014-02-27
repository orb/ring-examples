(ns ringtest.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ringtest.reload-me :as reload-me]
            [clojure.java.io :as io]
            [ringtest.stack :refer [start! stop!]]))


(defn handler-nil [req]
  {:body nil})

(defn handler-text [req]
  {:body "hello world"})

(defn handler-file [req]
  {:body (io/file "/etc/passwd")})

(defn handler-status [req]
  {:status 402
   :headers {"Location" "bitcoin:1G9TyAaKrfJn7q4Vrr15DscLXFSRPxBFaH?amount=.001"}})

(defn handler-helper1 [req]
  (response/response "You got it!"))

(defn handler-helper2 [req]
  (-> (response/response "")
      (response/status 302)
      (response/header "Location" "http://www.google.com")))

(defn handler-helper3 [req]
  (response/redirect "http://lmgtfy.com/?q=http+redirect"))

(defn handler-resource [req]
  (response/resource-response "hello.txt"))



;; ----------------------------------------

(defn handler-reload1 [req]
  (response/response (reload-me/some-work)))

(defn handler-reload2 [req]
  (require 'ringtest.reload-me :reload)
  (handler-reload1 req))

(defn wrap-reload [other-handler]
  (fn [req]
    (require 'ringtest.reload-me :reload)
    (other-handler req)))

(def handler-reload3 (wrap-reload #'handler-reload1))



(defn wrap-errors [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (let [error-body (str "There was a problem: " (.getMessage e))]
          (-> (response/response error-body)
              (response/status 500)))))))

(defn handler-problem [req]
  (throw (Exception. "Donkeys ate our server")))

(def handler-problem2 (wrap-errors handler-problem))

(def handler-no-problem
  (-> handler-reload1
      wrap-reload
      wrap-errors))


(defn my-stack [handler]
  (-> handler
      wrap-reload
      wrap-errors))

(def stack1
  (my-stack handler-reload1))


(def handler #'handler-reload1)


(comment
  (start!) #'handler
  (stop!)
)


