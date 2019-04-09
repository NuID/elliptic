(ns nuid.elliptic.curve.point
  (:require
   [nuid.elliptic.curve :as curve]
   [cognitect.transit :as t]
   [nuid.base64 :as base64]
   #?@(:cljs [["elliptic" :as e]]))
  #?@(:clj [(:import (org.bouncycastle.math.ec.custom.sec SecP256K1Point))]))

(defn neg [p]
  (curve/->Point
   #?(:clj (.negate (.-p p))
      :cljs (.neg (.-p p)))))

(defn add [p q]
  (curve/->Point
   (.add (.-p p) (.-p q))))

(defn mul [p k]
  (curve/->Point
   #?(:clj (.multiply (.-p p) (.-n k))
      :cljs (.mul (.-p p) (.-n k)))))

(defn eq? [p q]
  (let [p (.-p p) q (.-p q)]
    #?(:clj (= p q)
       :cljs (.eq p q))))

(defn base64 [pt]
  (base64/encode
   #?(:clj (.getEncoded (.-p pt) true)
      :cljs (.encodeCompressed (.-p pt)))))

(defn from-base64 [curve b64]
  (curve/->Point
   (.decodePoint
    (curve/curve curve)
    (base64/decode b64)
    #?@(:cljs [true]))))

(defn curve-id
  "Naive mechanism for identifying the curve `pt` is associated with.
  Needed primarily for {en,de}coding purposes."
  [pt]
  #?(:clj (when (instance? SecP256K1Point (.-p pt)) :secp256k1)
     :cljs (ffirst (filter #(eq? (curve/base (second %))
                                 (curve/base (curve/curve pt)))
                           curve/supported))))

(defn rep [pt]
  [(curve-id pt)
   (base64 pt)])

(defn from-rep [[curve-id b64]]
  (from-base64
   (get curve/supported curve-id)
   b64))

(def tag "ec.pt")

(def write-handler
  {nuid.elliptic.curve.Point
   (t/write-handler
    (constantly tag)
    rep)})

(def read-handler
  {tag (t/read-handler from-rep)})

#?(:cljs (def exports #js {:writeHandler write-handler
                           :readHandler read-handler
                           :fromBase64 from-base64
                           :curveId curve-id
                           :fromRep from-rep
                           :base64 base64
                           :rep rep
                           :tag tag
                           :mul mul
                           :add add
                           :neg neg
                           :eq eq?}))
