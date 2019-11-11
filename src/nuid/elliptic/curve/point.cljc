(ns nuid.elliptic.curve.point
  (:require
   [cognitect.transit :as t]
   [nuid.elliptic.curve :as curve]
   [nuid.transit :as transit]
   [nuid.base64 :as base64]
   [nuid.bn :as bn]
   #?@(:clj
       [[clojure.spec-alpha2.gen :as gen]
        [clojure.spec-alpha2 :as s]]
       :cljs
       [[clojure.spec.gen.alpha :as gen]
        [clojure.test.check.generators]
        [clojure.spec.alpha :as s]
        [goog.object :as obj]
        ["elliptic" :as e]]))
  #?@(:clj
      [(:import
        (org.bouncycastle.math.ec.custom.sec SecP256K1Point)
        (org.bouncycastle.math.ec ECPoint))]))

(defprotocol Point
  (add [p q])
  (mul [p q])
  (eq? [p q])
  (neg [p])
  (inf? [p])
  (encode [p]))

(defn- int->ext
  [point]
  {:curve (s/unform ::curve/curve (curve/from point))
   :point (encode point)})

#?(:clj
   (defn- ext->int
     [{:keys [curve point]}]
     (.decodePoint
      (curve/from curve)
      (base64/decode point))))

#?(:cljs
   (defn- ext->int
     [{:keys [curve point]}]
     (.decodePoint
      ^js (curve/from curve)
      (base64/decode point))))

;; TODO: clean up once cljs supports `s/select`
(s/def :nuid.elliptic.curve.point.encoded/point
  ::base64/encoded)

(s/def ::external
  (s/keys :req-un [::curve/curve
                   :nuid.elliptic.curve.point.encoded/point]))

(s/def ::internal
  (fn [x] (satisfies? Point x)))

(s/def ::representation
  (s/or
   ::external ::external
   ::internal ::internal))

(s/def ::point
  (s/with-gen
    (s/conformer
     (fn [x]
       (let [c (s/conform ::representation x)]
         (cond
           (s/invalid? c)           ::s/invalid
           (= ::external (first c)) (ext->int (second c))
           (= ::internal (first c)) (second c)
           :else                    ::s/invalid)))
     (fn [x]
       (let [c (s/conform ::representation x)]
         (cond
           (s/invalid? c)           ::s/invalid
           (= ::external (first c)) (second c)
           (= ::internal (first c)) (int->ext (second c))
           :else                    ::s/invalid))))
    (fn []
      (->>
       (gen/tuple (->>
                   (s/gen ::curve/curve)
                   (gen/fmap curve/base))
                  (s/gen ::bn/bn))
       (gen/fmap (partial apply mul))
       (gen/such-that (comp not inf?))))))

#?(:clj
   (extend-type ECPoint
     base64/Base64able
     (encode [x]
       (base64/encode
        (.getEncoded x true)))

     curve/Curveable
     (from [x] (.getCurve x))

     Point
     (add [p q] (.add p q))
     (mul [p k] (.multiply p k))
     (eq? [p q] (= p q))
     (neg [p] (.negate p))
     (inf? [p] (.isInfinity p))
     (encode [p] (base64/encode p))))

#?(:cljs
   (let [xf       (map (comp (juxt curve/base curve/id) curve/from))
         base->id (into {} xf (s/form ::curve/id))]
     (defn- identify-curve
       "Attempt to identify a curve by its base point"
       [c]
       (base->id (obj/get c "g")))))

#?(:cljs
   (extend-type e/curve.base.BasePoint
     base64/Base64able
     (encode [x]
       (base64/encode
        (.encodeCompressed x)))

     curve/Curveable
     (from [x]
       (curve/from
        (identify-curve
         (obj/get x "curve"))))

     Point
     (add [p q] (.add p q))
     (mul [p k] (.mul p k))
     (eq? [p q] (.eq p q))
     (neg [p] (.neg p))
     (inf? [p] (.isInfinity p))
     (encode [p] (base64/encode p))))

(def transit-tag "ec.pt")

(def transit-read-handler
  {transit-tag
   (t/read-handler
    (partial s/conform ::point))})

(def transit-write-handler
  (let [c #?(:clj ECPoint :cljs "default")]
    {c (t/write-handler
        (constantly transit-tag)
        (partial s/unform ::point))}))

#?(:cljs (def exports #js {}))
