(ns nuid.ecc
  (:require
   [cognitect.transit :as t]
   [nuid.bn :as bn]
   #?@(:clj [[clojure.data.json :as json]]
       :cljs [["elliptic" :as e]
              ["buffer" :as b]]))
  #?@(:clj
      [(:import
        (org.bouncycastle.crypto.ec
         CustomNamedCurves))]))

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

(defn generate-curve [& [id]]
  (let [id' (cond (keyword? id) (name id)
                  (string? id) id
                  (nil? id) "secp256k1")]
    #?(:clj (CustomNamedCurves/getByName id')
       :cljs (e/ec. id'))))

(def supported-curves
  {:secp256k1 (generate-curve :secp256k1)})

(defn get-curve [pt-or-curve]
  #?(:clj (.getCurve (.-p pt-or-curve))
     :cljs (if-let [p (.-p pt-or-curve)]
             (.-curve p)
             (.-curve pt-or-curve))))

(defn get-curve-id [pt]
  (let [g (base-point (get-curve pt))]
    (ffirst (filter #(eq? (base-point (second %)) g) supported-curves))))

(defn pt->hex [pt]
  (bn/bn->str
   (bn/str->bn
    #?(:cljs (.encode (.-p pt) "hex")
       :clj (.getEncoded (.-p pt))) 16)
    16))

(defn pt->rep [pt]
  [(get-curve-id pt) (pt->hex pt)])

(defn rep->pt [[c encoded]]
  (let [curve (get-curve (get supported-curves c))]
    (->Point #?(:cljs (.decodePoint curve encoded "hex")))))

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
                :get-curve get-curve
                :point-tag point-tag
                :rep->pt rep->pt
                :pt->rep pt->rep
                :pt->hex pt->hex
                :add add
                :mul mul
                :neg neg
                :eq? eq?}))
