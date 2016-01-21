(ns pedestal.swagger.content-negotiation
  (:require [io.pedestal.http :as service]
            [pedestal.swagger.doc :as doc]))

(def json-body
  (doc/annotate
   {:produces ["application/json"]}
   service/json-response))

(def edn-response
  (doc/annotate
   {:produces ["application/edn"]}
   service/edn-response))
