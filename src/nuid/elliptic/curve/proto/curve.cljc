(ns nuid.elliptic.curve.proto.curve
  (:require
   #?@(:clj  [[clojure.alpha.spec :as s]]
       :cljs [[clojure.spec.alpha :as s]])))

(defprotocol Curveable
  (from [x]))

(defprotocol Curve
  (id           [c])
  (base         [c])
  (order        [c])
  (decode-point [c encoded-point])
  (encode       [c]))

(s/def ::curve
  (fn [x] (satisfies? Curve x)))

(defmulti curve->parameters type)
(defmulti parameters->curve :nuid.elliptic.curve/id)
