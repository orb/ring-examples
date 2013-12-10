(ns ringtest.stack
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.refresh :refer [wrap-refresh]]))


(defn ring-stack [handler]
  (-> handler
      #_(wrap-refresh)
      (wrap-reload)
      (wrap-stacktrace)))

(defonce server-atom (atom nil))

(defn start [handler]
  (swap! server-atom
         (fn [server]
           (when server (.stop server))
           (jetty/run-jetty handler
                            {:port 8080 :join? false}))))

(defn stop []
  (swap! server-atom
         (fn [server]
           (when server (.stop server))
           nil)))

