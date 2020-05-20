(ns nuid.elliptic.curve.proto.point
  (:require
   #?@(:clj  [[clojure.alpha.spec :as s]]
       :cljs [[clojure.spec.alpha :as s]])))

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
