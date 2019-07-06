(ns thump.core
  #?(:cljs (:require [cljs.reader]))
  (:refer-clojure :exclude [compile]))

(defn keyword->str [k]
  (let [kw-ns (namespace k)
        kw-name (name k)]
    (if (nil? kw-ns)
      kw-name

      (str kw-ns "/" kw-name))))

(defn ^:dynamic *hiccup-element* [el props children]
  `(~'hiccup-element ~el ~props ~children))

(declare parse)

(defn maybe-parse-child [c]
  (if (vector? c)
    (parse c)
    c))

(defn parse [vec]
  (if-not (vector? vec)
    (throw (ex-info (str vec " is not a valid hiccup vector.") {}))
    (let [[el props & children] vec

          ;; parse
          el (if (keyword? el) (keyword->str el) el)
          props? (map? props)
          children? (not (nil? (seq children)))
          children (cond
                     (and props? children?) children
                     children? (cons props children)
                     props? '()
                     true (list props))
          props (if props?
                  props
                  nil)]
      (*hiccup-element* el props (map maybe-parse-child children)))))

(defmacro compile [vec]
  (parse vec))

(defn interpret [vec]
  (parse vec))

#?(:cljs
   (do (cljs.reader/register-tag-parser! 'hiccup/element parse)
       (cljs.reader/register-tag-parser! 'h/e parse)))
