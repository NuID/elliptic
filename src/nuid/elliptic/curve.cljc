(ns nuid.elliptic.curve
  (:require
   [nuid.bn :as bn]
   #?@(:cljs [["elliptic" :as e]]))
  #?@(:clj [(:import (org.bouncycastle.crypto.ec CustomNamedCurves))]))

(defrecord Point [p])

(defn curve
  "Naive function to access the underlying curve implementation of either a
  nuid.elliptic.curve.Point, a host-native point, or a host-native curve.
  This is useful for calling host-specific curve functions."
  [pt-or-curve]
  (let [get-curve- #?@(:clj [#(.getCurve %)] :cljs [#(.-curve %)])
        x (if (instance? nuid.elliptic.curve.Point pt-or-curve)
            (.-p pt-or-curve)
            pt-or-curve)]
    (get-curve- x)))

(defn named [id]
  (let [id (cond (keyword? id) (name id)
                 (string? id) id)]
    #?(:clj (CustomNamedCurves/getByName id)
       :cljs (e/ec. id))))

(def supported (into {} (map (juxt identity named)) [:secp256k1]))

(defn base [c]
  (->Point
   #?(:clj (.getG c)
      :cljs (.-g c))))

(defn order [c]
  (bn/->BN
   #?(:clj (.getN c)
      :cljs (.-n c))))

#?(:cljs (def exports #js {:supported (clj->js supported)
                           :Point ->Point
                           :curve curve
                           :named named
                           :order order
                           :base base}))
