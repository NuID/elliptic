(ns nuid.elliptic.curve.point
  (:require
   [nuid.elliptic.curve :as curve]
   [nuid.transit :as transit]
   [cognitect.transit :as t]
   [nuid.base64 :as base64]
   #?@(:cljs
       [["elliptic" :as e]]))
  #?@(:clj
      [(:import
        (org.bouncycastle.math.ec ECPoint))]))

(defprotocol Pointable
  (from [x]))

(defprotocol Point
  (add [p q])
  (mul [p q])
  (eq? [p q])
  (neg [p]))

#?(:clj
   (extend-protocol Pointable
     clojure.lang.PersistentVector
     (from [[id b64]]
       (.decodePoint
        (curve/from (curve/from id))
        (base64/decode b64)))))

#?(:clj
   (extend-type org.bouncycastle.math.ec.ECPoint
     Point
     (add [p q] (.add p q))
     (mul [p k] (.multiply p k))
     (eq? [p q] (= p q))
     (neg [p] (.negate p))

     curve/Curveable
     (from [x] (.getCurve x))

     base64/Base64able
     (encode [x]
       (base64/encode
        (.getEncoded x true)))

     transit/TransitWritable
     (rep [x]
       [(curve/id (curve/from x))
        (base64/encode x)])))

#?(:cljs
   (extend-protocol Pointable
     js/Array
     (from [[id b64]]
       (.decodePoint
        (curve/from (curve/from id))
        (base64/decode b64)
        true))))

#?(:cljs
   (defrecord WritablePoint [p]
     transit/TransitWritable
     (rep [_]
       [(curve/id (curve/from p))
        (base64/encode p)])))

#?(:cljs
   (defn- identify-curve
     "Attempts to fingerprint a raw curve
     when `curve/id` is unavailable."
     [c]
     (ffirst
      (filter
       #(eq? (curve/base (second %)) (.-g c))
       curve/supported))))

#?(:cljs
   (extend-type e/curve.base.BasePoint
     Point
     (add [p q] (.add p q))
     (mul [p k] (.mul p k))
     (eq? [p q] (.eq p q))
     (neg [p] (.neg p))

     curve/Curveable
     (from [x]
       (let [c (.-curve x) id (identify-curve c)]
         (curve/->Wrapped id c)))

     base64/Base64able
     (encode [x]
       (base64/encode
        (.encodeCompressed x)))

     transit/Wrappable
     (wrap [x] (->WritablePoint x))))

(def tag "ec.pt")

(def read-handler
  {tag (t/read-handler #(from %))})

(def write-handler
  (let [h (t/write-handler (constantly tag) #(transit/rep %))]
    #?(:clj {org.bouncycastle.math.ec.ECPoint h}
       :cljs {WritablePoint h})))

#?(:cljs
   (def exports
     #js {:writeHandler write-handler
          :readHandler read-handler
          :from from
          :tag tag
          :rep rep
          :mul mul
          :add add
          :neg neg
          :eq eq?}))
