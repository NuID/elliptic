(ns nuid.ecc
  (:require
   [cognitect.transit :as t]
   [nuid.utils :as utils]
   [nuid.bn :as bn]
   #?@(:clj [[clojure.data.json :as json]]
       :cljs [["elliptic" :as e]
              ["buffer" :as b]]))
  #?@(:clj
      [(:import
        (org.bouncycastle.crypto.ec CustomNamedCurves)
        (org.bouncycastle.math.ec.custom.sec SecP256K1Curve))]))

(defrecord Point [p])

(defn base-point [c]
  (->Point #?(:clj (.getG c) :cljs (.-g c))))

(defn prime-order [c]
  (bn/->BN #?(:clj (.getN c) :cljs (.-n c))))

(defn neg [p]
  (->Point #?(:clj (.negate (.-p p))
              :cljs (.neg (.-p p)))))

(defn add [p q]
  (->Point (.add (.-p p) (.-p q))))

(defn mul [p k]
  (->Point #?(:clj (.multiply (.-p p) (.-n k))
              :cljs (.mul (.-p p) (.-n k)))))

(defn eq? [p q]
  #?(:clj (= (.-p p) (.-p q))
     :cljs (.eq (.-p p) (.-p q))))

(defn generate-curve [& [{:keys [id]}]]
  (let [id' (cond (keyword? id) (name id)
                  (string? id) id
                  (nil? id) "secp256k1")]
    #?(:clj (CustomNamedCurves/getByName id')
       :cljs (e/ec. id'))))

(def supported-curves
  {:secp256k1 (generate-curve {:id :secp256k1})})

(defn get-curve [pt-or-curve]
  (let [get-curve- #?@(:clj [#(.getCurve %)] :cljs [#(.-curve %)])]
    (if (instance? nuid.ecc.Point pt-or-curve)
      (get-curve- (.-p pt-or-curve))
      (get-curve- pt-or-curve))))

(defn get-curve-id [pt]
  #?(:clj (when (instance? SecP256K1Curve (get-curve pt)) :secp256k1)
     :cljs (let [g (base-point (get-curve pt))]
             (ffirst (filter #(eq? (base-point (second %)) g) supported-curves)))))

(defn pt->base64 [pt]
  (utils/str->base64 #?(:clj (.getEncoded (.-p pt) true)
                        :cljs (.encodeCompressed (.-p pt)))))

(defn pt->rep [pt]
  [(get-curve-id pt) (pt->base64 pt)])

(defn base64->pt [curve b64]
  (->Point (.decodePoint curve (utils/base64->bytes b64) #?@(:cljs [true]))))

(defn rep->pt [[curve-id encoded]]
  (base64->pt (get-curve (get supported-curves curve-id)) encoded))

(def point-tag "ecc.pt")

(def point-write-handler
  {Point (t/write-handler
          (constantly point-tag)
          pt->rep)})

(def point-read-handler
  {point-tag (t/read-handler rep->pt)})

(defn generate-keypair
  ([] (generate-keypair (generate-curve)))
  ([curve]
   #?(:cljs (.genKeyPair curve))))

(defn get-public [keypair]
  (->Point #?(:cljs (.getPublic keypair))))

(defn get-private [keypair]
  (bn/->BN (.getPrivate keypair)))

#?(:cljs (def exports
           #js {:point-write-handler point-write-handler
                :point-read-handler point-read-handler
                :supported-curves supported-curves
                :generate-keypair generate-keypair
                :generate-curve generate-curve
                :get-curve-id get-curve-id
                :get-private get-private
                :prime-order prime-order
                :get-public get-public
                :base-point base-point
                :pt->base64 pt->base64
                :base64->pt base64->pt
                :get-curve get-curve
                :point-tag point-tag
                :rep->pt rep->pt
                :pt->rep pt->rep
                :add add
                :mul mul
                :neg neg
                :eq? eq?}))
