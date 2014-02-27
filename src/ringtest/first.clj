(ns ringtest.first
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ringtest.stack :refer [start! stop!]]))

(defroutes app-routes
  (GET "/" [] "Hello World!")
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(comment
  (start! #'app)
  (stop!)


  (start! (ringtest.stack/ring-stack #'app))
  )
