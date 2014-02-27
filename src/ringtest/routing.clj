(ns ringtest.routing
  (:require [compojure.core :as compojure]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [ringtest.stack :refer [start! stop!]]))


(defn home []
  (response/response "Home Page"))

(defn foo []
  (response/response "Foo Page"))

(defn foo-n [n]
  (response/response (str "This is Foo#" n)))



(defn app1 [req]
  (condp re-matches (:uri req)
    #"/" (home)
    #"/foo" (foo)
    #"/foo/(.*)" :>> #(foo-n (second %))
    (response/not-found "Wat")))


(defn route-to [handler]
  (fn [match]
    (if (string? match)
      (handler)
      (apply handler (rest match)))))

(defn app2 [req]
  (condp re-matches (:uri req)
    #"/"
    :>> (route-to home)

    #"/foo"
    :>> (route-to foo)

    #"/foo/(.*)"
    :>> (route-to foo-n)

    (response/not-found "Wat")))


(defn my-route [pattern handler]
  (fn [req]
    (if-let [match (re-matches pattern (:uri req))]
      ((route-to handler) match))))

(defn app3 [req]
  (let [my-routes [(my-route #"/" home)
                   (my-route #"/foo" foo)
                   (my-route #"/foo/(.*)" foo-n)
                   (my-route #".*" #(response/not-found "Wat"))]]
    (some #(% req) my-routes)))


(defn app4 [req]
  (let [my-routes [(compojure/GET "/" [] (home))
                   (compojure/GET "/foo" [] (foo))
                   (compojure/GET "/foo/:id" [id] (foo-n id))
                   (route/not-found "Wat")]]
    (some #(% req) my-routes)))


(def app5
  (compojure/routes
   (compojure/GET "/" [] (home))
   (compojure/GET "/foo" [] (foo))
   (compojure/GET "/foo/:id" [id] (foo-n id))
   (route/not-found "Wat")))


(def foo-routes
  (compojure/routes
   (compojure/GET "/foox" [] (foo))
   (compojure/GET "/foo/:id" [id] (foo-n id))))

(def app6
  (compojure/routes
   (compojure/GET "/" [] (home))
   foo-routes
   (route/not-found "Wat")))


(defn foobar-routes [foobar-type]
  (compojure/routes
      (compojure/GET "/" [] (str foobar-type " Page"))
      (compojure/GET "/:id" [id] (str foobar-type "#" id))))

(def app7
  (compojure/routes
   (compojure/GET "/" [] (home))
   (compojure/context "/foo" []
                      (foobar-routes "Foo"))
   (compojure/context "/bar" []
                      (foobar-routes "Bar"))
   (route/not-found "Wat")))



(def handler (-> #'app7
                 handler/site
                 server/ring-stack))


(def app8
  (compojure/routes
   (compojure/GET "/" [] (home))
   (let [f (foobar-routes "Foo")]
     (compojure/context "/foo" [] f))
   (let [b (foobar-routes "Bar")]
     (compojure/context "/bar" [] b))
   (route/not-found "Wat")))


(comment
  (start! #'handler)
  (stop!)
)


(defn y [msg]
  (str "hello" msg))

(defmacro zebra [x]
  `(y ~x))
