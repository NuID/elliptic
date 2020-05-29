(ns nuid.elliptic.curve.point
  (:require
   [nuid.bn :as bn]
   [nuid.elliptic.curve :as curve]
   [nuid.elliptic.curve.proto :as curve.proto]
   [nuid.elliptic.curve.point.proto :as point.proto]
   #?@(:clj
       [[clojure.alpha.spec.gen :as gen]
        [clojure.alpha.spec :as s]]
       :cljs
       [[clojure.spec.gen.alpha :as gen]
        [clojure.test.check.generators]
        [clojure.spec.alpha :as s]])))

(s/def ::parameters
  (s/keys
   :req
   [::curve/parameters
    ::curve/point]))

(s/def ::representation
  (s/or
   ::point.proto/point ::point.proto/point
   ::parameters        ::parameters))

(defmethod point.proto/point->parameters :default
  [point]
  {::curve/parameters (curve.proto/encode (curve.proto/from point))
   ::curve/point      (point.proto/encode point)})

(defmethod point.proto/parameters->point :default
  [{::curve/keys [parameters point]}]
  (->
   (curve.proto/parameters->curve parameters)
   (curve.proto/decode-point point)))

(s/def ::parameters<>point
  (s/conformer
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (point.proto/parameters->point (second c))
         (= ::point.proto/point (first c)) (second c)
         :else                             ::s/invalid)))
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (second c)
         (= ::point.proto/point (first c)) (point.proto/point->parameters (second c))
         :else                             ::s/invalid)))))

(s/def ::base
  (s/with-gen
    ::parameters<>point
    (fn []
      (->>
       (s/gen ::curve/curve)
       (gen/fmap curve.proto/base)))))

(s/def ::point
  (s/with-gen
    ::parameters<>point
    (fn []
      (->>
       (gen/tuple (s/gen ::base) (s/gen ::bn/bn))
       (gen/fmap (partial apply point.proto/mul))
       (gen/such-that (complement point.proto/inf?))))))

(defn add    [p q] (point.proto/add    p q))
(defn mul    [p k] (point.proto/mul    p k))
(defn eq?    [p q] (point.proto/eq?    p q))
(defn neg    [p]   (point.proto/neg    p))
(defn inf?   [p]   (point.proto/inf?   p))
(defn encode [p]   (point.proto/encode p))

(def point->parameters point.proto/point->parameters)
(def parameters->point point.proto/parameters->point)
