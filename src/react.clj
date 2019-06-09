(ns react
  "This namespace is used for debugging at a JVM REPL")

(defn createElement
  ([el props & children]
   `[~el ~(when props `(react/props ~@props)) ~@children]))
