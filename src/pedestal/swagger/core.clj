(ns pedestal.swagger.core
  (:require [pedestal.swagger.doc :as doc]
            [pedestal.swagger.schema :as schema]
            [io.pedestal.http.route.definition :refer [expand-routes]]
            [io.pedestal.interceptor :as interceptor]
            [ring.util.response :refer [response]]))

;; base path can see what is in common ?
;; if you specify resource-path helps ?
(def swagger-docs
  {:title "Swagger Sample App"
   :description "This is a sample Petstore server."
   :apis {::pets {:description "Operations about pets"
                  :ops [{:path [:clojure.core/+ :swagger] ; string, keyword or vector
                         :summary "Update an existing per"
                         :notes "Works 50% of the times"}
                        {:path [:clojure.core/- :swagger]
                         :summary "Delete an existing per"
                         :notes "Works 90% of the times"}]}
          ::users {:description "Operations about users"
                   :ops [{:path [:clojure.core/* :swagger]
                          :summary "Update an existing us"
                          :notes "Works 50% of the times"}
                         {:path [:clojure.core// :swagger]
                          :summary "Delete an existing us"
                          :notes "Works 90% of the times"}]}}})


(interceptor/definterceptorfn params
  "Expect a map in the form of {:query-params S1 :path-params
  S2 :headers S3 :json-body S4} etc. Will assoc them back into request map
  if coercion is successful. Errors are stored under :errors key"
  [{:as params-schema}]
  (with-meta
   (interceptor/on-request
    (fn [req]
      (merge req
             (schema/coerce-params params-schema req))))
   {::doc/params params-schema}))

(interceptor/definterceptorfn returns
  ""
  [returns-schema]
  (with-meta
   (interceptor/on-response
    (fn [{:keys [body] :as resp}]
      (assert (schema/validate returns-schema body))
      resp))
   {::doc/returns returns-schema}))

;; response messages?

;;

(def docs (atom {}))

(defn- make-handler [key]
  (interceptor/handler key (fn [req]
                             (response (get @docs key)))))

(defn api-declaration [route-name]
  (make-handler route-name))

(def resource-listing
  (make-handler ::doc/api-docs))

(def swagger-ui ;; Nope this is different!
  (make-handler ::swagger-ui))

(defmacro defroutes [name doc-spec route-spec]
  `(let [route-table# (expand-routes (quote ~route-spec))]
     (def ~name route-table#)
     (reset! docs (doc/expand-docs ~doc-spec route-table#))))
