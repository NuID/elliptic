(ns nuid.elliptic.curve.impl.bouncycastle
  (:require
   [nuid.base64.proto :as base64.proto]
   [nuid.elliptic.curve.proto :as curve.proto]
   [nuid.elliptic.curve.point.proto :as point.proto])
  (:import
   (org.bouncycastle.asn1.x9 X9ECParameters)
   (org.bouncycastle.crypto.ec CustomNamedCurves)
   (org.bouncycastle.math.ec ECPoint)
   (org.bouncycastle.math.ec.custom.sec SecP256K1Curve)))

(extend-protocol curve.proto/Curveable
  java.lang.String
  (from [x] (CustomNamedCurves/getByName x))

  clojure.lang.Keyword
  (from [x] (curve.proto/from (name x)))

  X9ECParameters
  (from [x] (.getCurve x)))

(extend-protocol curve.proto/Curve
  X9ECParameters
  (id           [c]     (curve.proto/id (curve.proto/from c)))
  (base         [c]     (.getG c))
  (order        [c]     (.getN c))
  (encode       [c]     (curve.proto/encode (curve.proto/from c)))
  (decode-point [c enc] (curve.proto/decode-point (curve.proto/from c) enc))

  SecP256K1Curve
  (id           [c]     :nuid.elliptic.curve/secp256k1)
  (base         [c]     (curve.proto/base  (curve.proto/from (curve.proto/id c))))
  (order        [c]     (curve.proto/order (curve.proto/from (curve.proto/id c))))
  (encode       [c]     {:nuid.elliptic.curve/id (curve.proto/id c)})
  (decode-point [c enc] (.decodePoint c (base64.proto/decode enc))))

(extend-type ECPoint
  base64.proto/Base64able
  (encode
    ([x]         (base64.proto/encode (.getEncoded x true)))
    ([x charset] (base64.proto/encode (.getEncoded x true) charset)))

  curve.proto/Curveable
  (from [x] (.getCurve x))

  point.proto/Point
  (add    [p q] (.add p q))
  (mul    [p k] (.multiply p k))
  (eq?    [p q] (= p q))
  (neg    [p]   (.negate p))
  (inf?   [p]   (.isInfinity p))
  (encode [p]   (base64.proto/encode p)))
