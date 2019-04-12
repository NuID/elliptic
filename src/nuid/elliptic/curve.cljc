(ns nuid.elliptic.curve
  #?@(:clj
      [(:import
        (org.bouncycastle.math.ec.custom.sec SecP256K1Curve)
        (org.bouncycastle.crypto.ec CustomNamedCurves)
        (org.bouncycastle.asn1.x9 X9ECParameters))])
  #?@(:cljs
      [(:require ["elliptic" :as e])]))

(defprotocol Curveable
  (from [x]))

(defprotocol Curve
  (id [c])
  (base [c])
  (order [c]))

#?(:clj
   (extend-protocol Curveable
     java.lang.String
     (from [x] (CustomNamedCurves/getByName x))

     clojure.lang.Keyword
     (from [x] (from (name x)))

     org.bouncycastle.asn1.x9.X9ECParameters
     (from [x] (.getCurve x))))

#?(:clj
   (extend-protocol Curve
     org.bouncycastle.asn1.x9.X9ECParameters
     (id [c] (id (from c)))
     (base [c] (.getG c))
     (order [c] (.getN c))

     org.bouncycastle.math.ec.custom.sec.SecP256K1Curve
     (id [c] :secp256k1)
     (base [c] (base (from (id c))))
     (order [c] (order (from (id c))))))

#?(:cljs
   (defrecord Wrapped [id curve]
     Curveable
     (from [_] (.-curve curve))

     Curve
     (id [_] (keyword id))
     (base [_] (.-g curve))
     (order [_] (.-n curve))))

#?(:cljs
   (extend-protocol Curveable
     string
     (from [x] (->Wrapped (keyword x) (e/ec. x)))

     cljs.core.Keyword
     (from [x] (->Wrapped x (e/ec. (name x))))))

(def supported (into {} (map (juxt identity from)) [:secp256k1]))

#?(:cljs
   (def exports
     #js {:wrap ->Wrapped
          :order order
          :base base
          :from from
          :id id}))
