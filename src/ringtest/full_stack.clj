(ns ringtest.full-stack
  (:require [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ringtest.stack :as server]))



(def home (compojure/GET "*" [] "bar"))
(def foo (compojure/GET "/foo" [] "foo"))


(defn first-app [req]
  (home req))


(def handler (-> #'first-app
                 handler/site
                 server/ring-stack))




