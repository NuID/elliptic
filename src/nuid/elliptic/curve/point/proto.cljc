(ns nuid.elliptic.curve.point.proto
  (:require
   [clojure.spec.alpha :as s]))

(defprotocol Point
  (add    [p q])
  (mul    [p k])
  (eq?    [p q])
  (neg    [p])
  (inf?   [p])
  (encode [p]))

(s/def ::point
  (fn [x] (satisfies? Point x)))

(defmulti point->parameters type)
(defmulti parameters->point :nuid.elliptic.curve/parameters)
