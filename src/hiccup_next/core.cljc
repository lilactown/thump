(ns hiccup-next.core
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

(declare interpret)

(defn maybe-parse-child [c]
  (if (vector? c)
    (interpret c)
    c))

(defn interpret [vec]
  (if-not (vector? vec)
    (throw (ex-info (str vec " is not a valid hiccup vector.") {}))
    (let [[el props & children] vec

          ;; interpret
          el (if (keyword? el) (keyword->str el) el)
          props? (map? props)
          children? (not (nil? (seq children)))
          children (cond
                     (and props? children?) children
                     children? (cons props children)
                     true nil)
          props (if props?
                  props
                  nil)]
      (*hiccup-element* el props (map maybe-parse-child children)))))

(defmacro compile [vec]
  (interpret vec))

#?(:cljs
   (do (cljs.reader/register-tag-parser! 'hiccup/next interpret)
       (cljs.reader/register-tag-parser! 'h/n interpret)))
