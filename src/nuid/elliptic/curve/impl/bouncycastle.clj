(ns nuid.elliptic.curve.impl.bouncycastle
  (:require
   [nuid.base64.proto :as proto.base64]
   [nuid.base64 :as base64]
   [nuid.elliptic.curve.proto.curve :as proto.curve]
   [nuid.elliptic.curve.proto.point :as proto.point])
  (:import
   (org.bouncycastle.asn1.x9 X9ECParameters)
   (org.bouncycastle.crypto.ec CustomNamedCurves)
   (org.bouncycastle.math.ec ECPoint)
   (org.bouncycastle.math.ec.custom.sec SecP256K1Curve)))

(extend-protocol proto.curve/Curveable
  java.lang.String
  (from [x] (CustomNamedCurves/getByName x))

  clojure.lang.Keyword
  (from [x] (proto.curve/from (name x)))

  X9ECParameters
  (from [x] (.getCurve x)))

(extend-protocol proto.curve/Curve
  X9ECParameters
  (id           [c]     (proto.curve/id (proto.curve/from c)))
  (base         [c]     (.getG c))
  (order        [c]     (.getN c))
  (decode-point [c enc] (proto.curve/decode-point (proto.curve/from c) enc))
  (encode       [c]     (proto.curve/encode (proto.curve/from c)))

  SecP256K1Curve
  (id           [c]     :nuid.elliptic.curve/secp256k1)
  (base         [c]     (proto.curve/base  (proto.curve/from (proto.curve/id c))))
  (order        [c]     (proto.curve/order (proto.curve/from (proto.curve/id c))))
  (decode-point [c enc] (.decodePoint c (base64/decode enc)))
  (encode       [c]     {:nuid.elliptic.curve/id (proto.curve/id c)}))

(extend-type ECPoint
  proto.base64/Base64able
  (encode [x]
    (base64/encode
     (.getEncoded x true)))

  proto.curve/Curveable
  (from [x] (.getCurve x))

  proto.point/Point
  (add    [p q] (.add p q))
  (mul    [p k] (.multiply p k))
  (eq?    [p q] (= p q))
  (neg    [p]   (.negate p))
  (inf?   [p]   (.isInfinity p))
  (encode [p]   (base64/encode p)))
