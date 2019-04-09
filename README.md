# nuid.elliptic

Cross-platform elliptic curve arithmetic.

## ⚠️  This library has not been independently audited.

`nuid.elliptic` primarily exists to abstract over platform-specific differences and provide a common interface to the provided functionality across host platforms. In most cases, `nuid.elliptic` delegates directly to a host implemention (e.g. `bouncycastle` on the `jvm`, and `elliptic`, etc. in `node` and the browser).

## Git issues and other communications are warmly welcomed. [dev@nuid.io](mailto:dev@nuid.io)

## Requirements

[`jvm`](https://www.java.com/en/download/), [`node + npm`](https://nodejs.org/en/download/), [`clj`](https://clojure.org/guides/getting_started), [`shadow-cljs`](https://shadow-cljs.github.io/docs/UsersGuide.html#_installation)

## From Clojure and ClojureScript

### tools.deps:

`{nuid/elliptic {:git/url "https://github.com/nuid/elliptic" :sha "..."}`

### usage:

```
$ clj # or shadow-cljs node-repl
=> (require '[nuid.elliptic.curve.point :as point])
=> (require '[nuid.elliptic.curve :as curve])
=> (require '[nuid.bn :as bn])
=> (def c (curve/named :secp256k1))
=> (def g (curve/base c))
=> (def n (curve/order c))
=> (point/mul g (bn/from "123333333333333333333333333333333333321"))
=> (point/add g *1)

;; These will need to be added to the classpath, e.g. by using nuid.deps
=> (require '[nuid.cryptography :as crypt])
=> (require '[nuid.transit :as transit])

=> (def r (crypt/secure-random-bn-lt 32 n))
=> (point/mul g r)
=> (transit/write {:handlers point/write-handler} g)
```

## From JavaScript

This library aims to be usable from JavaScript. More work is necessary to establish the most convient consumption patterns, which will likely involve [`transit-js`](https://github.com/cognitect/transit-js).

### node:

```
$ shadow-cljs release node
$ node
> var E = require('./target/node/nuid_elliptic');

;; NOTE: many nuid.elliptic functions return clojure types in node
> var c = E.curve.named("secp256k1");
> var g = E.curve.base(c);
> var n = E.curve.order(c);
> E.point.curveId(g);
> E.point.mul(g, n); // Infinity
> E.point.add(g, g);
> var b64 = E.point.base64(g);
> E.point.from-base64(c, b64);
```

### browser:

```
$ shadow-cljs release browser
## go use ./target/browser/nuid_elliptic.js in a browser script
```

## From Java

To call `nuid.elliptic` from Java or other JVM languages, use one of the recommended interop strategies ([var/IFn](https://clojure.org/reference/java_interop#_calling_clojure_from_java) or [uberjar/aot](https://push-language.hampshire.edu/t/calling-clojure-code-from-java/865)). Doing so may require modifications or additions to the API for convenience.

## From CLR

[Coming soon](https://github.com/bcgit/bc-csharp)

## Notes

The purpose of `nuid.elliptic` and sibling `nuid` libraries (e.g. [`nuid.bn`](https://github.com/nuid/bn)) is to abstract over platform-specific differences and provide a common interface to fundamental dependencies. This allows us to express dependent logic (e.g. [`nuid.zk`](https://github.com/nuid/zk)) once in pure Clojure(Script), and use it from each of the host platforms (Java, JavaScript, CLR). This is particularly useful for generating and verifying proofs across service boundaries. Along with [`tools.deps`](https://clojure.org/guides/deps_and_cli), this approach yields the code-sharing, circular-dependency avoidance, and local development benefits of a monorepo, with the modularity and orthogonality of an isolated library.

## Contributing

Install [`git-hooks`](https://github.com/icefox/git-hooks) and fire away. Make sure not to get bitten by [`externs`](https://clojurescript.org/guides/externs) if modifying `npm` dependencies.

### formatting:

```
$ clojure -A:cljfmt            # check
$ clojure -A:cljfmt:cljfmt/fix # fix
```

### dependencies:

```
## check
$ npm outdated
$ clojure -A:depot

## update
$ npm upgrade -s
$ clojure -A:depot:depot/update
```
