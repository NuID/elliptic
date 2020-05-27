(ns nuid.elliptic.curve.impl.ellipticjs
  (:require
   [goog.object :as obj]
   [nuid.base64.proto :as proto.base64]
   [nuid.base64 :as base64]
   [nuid.elliptic.curve.proto.curve :as proto.curve]
   [nuid.elliptic.curve.proto.point :as proto.point]
   ["elliptic" :as e]))

(defrecord Wrapped [id ^js curve]
  proto.curve/Curveable
  (from [_] (obj/get curve "curve"))

  proto.curve/Curve
  (id           [_]     id)
  (base         [_]     (obj/get curve "g"))
  (order        [_]     (obj/get curve "n"))
  (decode-point [_ enc] (.decodePoint (obj/get curve "curve") (base64/decode enc)))
  (encode       [_]     {:nuid.elliptic.curve/id id}))

(extend-protocol proto.curve/Curveable
  cljs.core.Keyword
  (from [x] (->Wrapped x (e/ec. (name x))))

  string
  (from [x] (proto.curve/from
             (case x
               "nuid.elliptic.curve/secp256k1" :nuid.elliptic.curve/secp256k1
               "secp256k1"                     :nuid.elliptic.curve/secp256k1))))

(def base->id
  (into
   (hash-map)
   (map
    (comp
     (juxt proto.curve/base proto.curve/id)
     proto.curve/from))
   [:nuid.elliptic.curve/secp256k1]))

(defn identify-curve
  "Attempt to identify a curve by its base point"
  [c]
  (base->id (obj/get c "g")))

(extend-type e/curve.base.BasePoint
  proto.base64/Base64able
  (encode [x]
    (base64/encode
     (.encodeCompressed x)))

  proto.curve/Curveable
  (from [x]
    (proto.curve/from
     (identify-curve
      (obj/get x "curve"))))

  proto.point/Point
  (add    [p q] (.add p q))
  (mul    [p k] (.mul p k))
  (eq?    [p q] (.eq p q))
  (neg    [p]   (.neg p))
  (inf?   [p]   (.isInfinity p))
  (encode [p]   (base64/encode p)))
