(ns nuid.elliptic.curve.point
  (:require
   [nuid.elliptic.curve :as curve]
   [nuid.transit :as transit]
   [cognitect.transit :as t]
   [nuid.cbor :as nuid.cbor]
   [nuid.base64 :as base64]
   #?@(:cljs
       [["elliptic" :as e]]))
  #?@(:clj
      [(:import
        (org.bouncycastle.math.ec.custom.sec SecP256K1Point)
        (org.bouncycastle.math.ec ECPoint))]))

(defprotocol Point
  (add [p q])
  (mul [p q])
  (eq? [p q])
  (neg [p]))

(defn rep
  [point]
  {"curve" (name (curve/id (curve/from point)))
   "point" (base64/encode point)})

(defn from-rep
  [{:strs [curve point compressed]}]
  (.decodePoint
   (curve/from (curve/from curve))
   (base64/decode point)
   #?@(:cljs [true])))

#?(:clj
   (extend-type ECPoint
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
        (.getEncoded x true)))))

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
        (.encodeCompressed x)))))

(def transit-tag "ec.pt")

(def transit-read-handler
  {transit-tag (t/read-handler #(from-rep %))})

(def transit-write-handler
  (let [c #?(:clj ECPoint :cljs "default")]
    {c (t/write-handler
        (constantly transit-tag)
        #(rep %))}))

(def cbor-tag (symbol transit-tag))

#?(:clj
   (nuid.cbor/register-tagged-literal-read-handler!
    {cbor-tag #(from-rep %)}))

#?(:clj
   (def cbor-write-handler
     ;; `clj-cbor` doesn't support inheritance-aware dispatch yet.
     (let [c #?(:clj SecP256K1Point)]
       {c #(tagged-literal
            cbor-tag
            (rep %))})))

#?(:cljs
   (def exports
     #js {:transitWriteHandler transit-write-handler
          :transitReadHandler transit-read-handler
          :transitTag transit-tag
          :fromRep from-rep
          :rep rep
          :mul mul
          :add add
          :neg neg
          :eq eq?}))
