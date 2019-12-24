(ns nuid.elliptic.curve
  #?(:clj
     (:import
      (org.bouncycastle.math.ec.custom.sec SecP256K1Curve)
      (org.bouncycastle.crypto.ec CustomNamedCurves)
      (org.bouncycastle.asn1.x9 X9ECParameters)))
  (:require
   #?@(:clj
       [[clojure.alpha.spec.gen :as gen]
        [clojure.alpha.spec :as s]]
       :cljs
       [[clojure.spec.gen.alpha :as gen]
        [clojure.test.check.generators]
        [clojure.spec.alpha :as s]
        ["elliptic" :as e]])))

(defprotocol Curveable
  (from [x]))

(defprotocol Curve
  (id    [c])
  (base  [c])
  (order [c]))

(s/def ::id #{"secp256k1"})
(s/def ::external (s/keys :req-un [::id]))
(s/def ::internal (fn [x] (satisfies? Curve x)))
(s/def ::representation
  (s/or
   ::external ::external
   ::internal ::internal))

(s/def ::curve
  (s/with-gen
    (s/conformer
     (fn [x]
       (let [c (s/conform ::representation x)]
         (cond
           (s/invalid? c)           ::s/invalid
           (= ::external (first c)) (from (:id (second c)))
           (= ::internal (first c)) (second c)
           :else                    ::s/invalid)))
     (fn [x]
       (let [c (s/conform ::representation x)]
         (cond
           (s/invalid? c)           ::s/invalid
           (= ::external (first c)) (second c)
           (= ::internal (first c)) {:id (id (second c))}
           :else                    ::s/invalid))))
    (fn []
      (->>
       (s/gen ::external)
       (gen/fmap (comp from :id))))))

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
     (id    [c] (id (from c)))
     (base  [c] (.getG c))
     (order [c] (.getN c))

     org.bouncycastle.math.ec.custom.sec.SecP256K1Curve
     (id    [c] "secp256k1")
     (base  [c] (base (from (id c))))
     (order [c] (order (from (id c))))))

#?(:cljs
   (defrecord Wrapped [id- curve]
     Curveable
     (from [_] (.-curve ^js curve))

     Curve
     (id    [_] id-)
     (base  [_] (.-g ^js curve))
     (order [_] (.-n ^js curve))))

#?(:cljs
   (extend-protocol Curveable
     string
     (from [x] (->Wrapped x (e/ec. x)))

     cljs.core.Keyword
     (from [x] (from (name x)))))

#?(:cljs (def exports #js {}))
