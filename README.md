<p align="right"><a href="https://nuid.io"><img src="https://nuid.io/svg/logo.svg" width="20%"></a></p>

# nuid.elliptic

Cross-platform elliptic curve arithmetic.

Git issues and other communications are warmly welcomed. [dev@nuid.io](mailto:dev@nuid.io)

## Requirements

[`jvm`](https://www.java.com/en/download/), [`node + npm`](https://nodejs.org/en/download/), [`clj`](https://clojure.org/guides/getting_started), [`shadow-cljs`](https://shadow-cljs.github.io/docs/UsersGuide.html#_installation)

## Clojure and ClojureScript

### tools.deps:

`{nuid/elliptic {:git/url "https://github.com/nuid/elliptic" :sha "..."}}`

### usage:

```
$ clj # or shadow-cljs node-repl
=> (require '[nuid.elliptic.curve.point :as point])
=> (require '[nuid.elliptic.curve :as curve])
=> (require '[nuid.transit :as transit])
=> (require '[nuid.bn :as bn])
=> (def c (curve/from ::curve/secp256k1))
=> (def g (curve/base c))
=> (def n (curve/order c))
=> (def k (bn/from "123333333333333333333333333333333333333333321"))
=> (def p (point/mul g k))
=> (def q (point/add g p))
```

## Notes

`nuid.elliptic` primarily exists to abstract over platform-specific differences
and provide a common interface to the provided functionality across host
platforms. `nuid.elliptic` delegates directly to host implementions (e.g.
`bouncycastle` on the `jvm`, and `elliptic`, etc., in `node` and the browser).

## Licensing

Apache v2.0 or MIT

## ⚠️  Disclaimer

This library is [property tested](https://github.com/clojure/test.check#testcheck)
to help verify implementation, but has not yet been audited by an independent
third party.
