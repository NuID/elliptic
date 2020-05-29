(ns nuid.elliptic.curve
  (:require
   [nuid.base64 :as base64]
   [nuid.elliptic.curve.proto :as curve.proto]
   #?@(:clj
       [[clojure.alpha.spec.gen :as gen]
        [clojure.alpha.spec :as s]
        [nuid.elliptic.curve.impl.bouncycastle]]
       :cljs
       [[clojure.spec.gen.alpha :as gen]
        [clojure.test.check.generators]
        [clojure.spec.alpha :as s]
        [nuid.elliptic.curve.impl.ellipticjs]])))

(def ids
  #{::secp256k1})

(s/def ::id ids)

(s/def ::parameters
  (s/keys
   :req [::id]))

(s/def ::representation
  (s/or
   ::curve.proto/curve ::curve.proto/curve
   ::parameters        ::parameters))

(defmethod curve.proto/curve->parameters :default
  [c]
  (curve.proto/encode c))

(defmethod curve.proto/parameters->curve :default
  [{::keys [id]}]
  (curve.proto/from id))

(s/def ::parameters<>curve
  (s/conformer
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (curve.proto/parameters->curve (second c))
         (= ::curve.proto/curve (first c)) (second c)
         :else                             ::s/invalid)))
   (fn [x]
     (let [c (s/conform ::representation x)]
       (cond
         (s/invalid? c)                    ::s/invalid
         (= ::parameters        (first c)) (second c)
         (= ::curve.proto/curve (first c)) (curve.proto/curve->parameters (second c))
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

(defn from         [x]     (curve.proto/from x))
(defn id           [c]     (curve.proto/id c))
(defn base         [c]     (curve.proto/base c))
(defn order        [c]     (curve.proto/order c))
(defn encode       [c]     (curve.proto/encode c))
(defn decode-point [c enc] (curve.proto/decode-point c enc))

(def curve->parameters curve.proto/curve->parameters)
(def parameters->curve curve.proto/parameters->curve)
