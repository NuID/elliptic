(ns nuid.elliptic.curve.impl.ellipticjs
  (:require
   [goog.object :as obj]
   [nuid.base64.proto :as base64.proto]
   [nuid.elliptic.curve.proto :as curve.proto]
   [nuid.elliptic.curve.point.proto :as point.proto]
   ["elliptic" :as e]))

(defrecord Wrapped [id ^js curve]
  curve.proto/Curveable
  (from [_] (obj/get curve "curve"))

  curve.proto/Curve
  (id           [_]     id)
  (base         [_]     (obj/get curve "g"))
  (order        [_]     (obj/get curve "n"))
  (encode       [_]     {:nuid.elliptic.curve/id id})
  (decode-point [_ enc] (.decodePoint
                         (obj/get curve "curve")
                         (base64.proto/decode enc))))

(extend-protocol curve.proto/Curveable
  cljs.core.Keyword
  (from [x] (->Wrapped x (e/ec. (name x))))

  string
  (from [x] (curve.proto/from
             (case x
               "nuid.elliptic.curve/secp256k1" :nuid.elliptic.curve/secp256k1
               "secp256k1"                     :nuid.elliptic.curve/secp256k1))))

(def base->id
  (into
   (hash-map)
   (map
    (comp
     (juxt curve.proto/base curve.proto/id)
     curve.proto/from))
   [:nuid.elliptic.curve/secp256k1]))

(defn identify-curve
  "Attempt to identify a curve by its base point"
  [c]
  (base->id (obj/get c "g")))

(extend-type e/curve.base.BasePoint
  base64.proto/Base64able
  (encode
    ([x]         (base64.proto/encode (.encodeCompressed x)))
    ([x charset] (base64.proto/encode (.encodeCompressed x) charset)))

  curve.proto/Curveable
  (from [x]
    (curve.proto/from
     (identify-curve
      (obj/get x "curve"))))

  point.proto/Point
  (add    [p q] (.add p q))
  (mul    [p k] (.mul p k))
  (eq?    [p q] (.eq p q))
  (neg    [p]   (.neg p))
  (inf?   [p]   (.isInfinity p))
  (encode [p]   (base64.proto/encode p)))
