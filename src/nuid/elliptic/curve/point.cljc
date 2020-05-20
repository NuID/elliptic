(ns nuid.elliptic.curve.point
  (:require
   [nuid.bn :as bn]
   [nuid.elliptic.curve :as curve]
   [nuid.elliptic.curve.proto.curve :as proto.curve]
   [nuid.elliptic.curve.proto.point :as proto.point]
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
   ::proto.point/point ::proto.point/point
   ::parameters        ::parameters))

(defmethod proto.point/point->parameters :default
  [point]
  {::curve/parameters (proto.curve/encode (proto.curve/from point))
   ::curve/point      (proto.point/encode point)})

(defmethod proto.point/parameters->point :default
  [{::curve/keys [parameters point]}]
  (->
   (proto.curve/parameters->curve parameters)
   (proto.curve/decode-point point)))

(s/def ::parameters<>point
  (s/conformer
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (proto.point/parameters->point (second c))
         (= ::proto.point/point (first c)) (second c)
         :else                             ::s/invalid)))
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (second c)
         (= ::proto.point/point (first c)) (proto.point/point->parameters (second c))
         :else                             ::s/invalid)))))

(s/def ::base
  (s/with-gen
    ::parameters<>point
    (fn []
      (->>
       (s/gen ::curve/curve)
       (gen/fmap proto.curve/base)))))

(s/def ::point
  (s/with-gen
    ::parameters<>point
    (fn []
      (->>
       (gen/tuple (s/gen ::base) (s/gen ::bn/bn))
       (gen/fmap (partial apply proto.point/mul))
       (gen/such-that (complement proto.point/inf?))))))

(def add               proto.point/add)
(def mul               proto.point/mul)
(def eq?               proto.point/eq?)
(def neg               proto.point/neg)
(def inf?              proto.point/inf?)
(def encode            proto.point/encode)
(def point->parameters proto.point/point->parameters)
(def parameters->point proto.point/parameters->point)
