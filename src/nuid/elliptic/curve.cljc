(ns nuid.elliptic.curve
  (:require
   [nuid.base64 :as base64]
   [nuid.elliptic.curve.proto.curve :as proto.curve]
   #?@(:clj
       [[clojure.alpha.spec.gen :as gen]
        [clojure.alpha.spec :as s]
        [nuid.elliptic.curve.impl.bouncycastle]]
       :cljs
       [[clojure.spec.gen.alpha :as gen]
        [clojure.test.check.generators]
        [clojure.spec.alpha :as s]
        [nuid.elliptic.curve.impl.ellipticjs]])))

(s/def ::id
  #{::secp256k1})

(s/def ::parameters
  (s/keys
   :req [::id]))

(s/def ::representation
  (s/or
   ::proto.curve/curve ::proto.curve/curve
   ::parameters        ::parameters))

(defmethod proto.curve/curve->parameters :default
  [c]
  (proto.curve/encode c))

(defmethod proto.curve/parameters->curve :default
  [{::keys [id]}]
  (proto.curve/from id))

(s/def ::parameters<>curve
  (s/conformer
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (proto.curve/parameters->curve (second c))
         (= ::proto.curve/curve (first c)) (second c)
         :else                             ::s/invalid)))
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (second c)
         (= ::proto.curve/curve (first c)) (proto.curve/curve->parameters (second c))
         :else                             ::s/invalid)))))

(s/def ::curve
  (s/with-gen
    ::parameters<>curve
    (fn []
      (->>
       (s/gen ::parameters)
       (gen/fmap (partial s/conform ::parameters<>curve))))))

(s/def ::point
  ::base64/encoded)

(def from              proto.curve/from)
(def id                proto.curve/id)
(def base              proto.curve/base)
(def order             proto.curve/order)
(def decode-point      proto.curve/decode-point)
(def encode            proto.curve/encode)
(def curve->parameters proto.curve/curve->parameters)
(def parameters->curve proto.curve/parameters->curve)
