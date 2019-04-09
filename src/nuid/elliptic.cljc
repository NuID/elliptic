(ns nuid.elliptic
  (:require
   [nuid.elliptic.curve.point :as point]
   [nuid.elliptic.curve :as curve]))

#?(:cljs (def exports #js {:curve curve/exports
                           :point point/exports}))
