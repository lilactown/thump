(ns hiccup-next.core
  (:refer-clojure :exclude [compile]))

(defn keyword->str [k]
  (let [kw-ns (namespace k)
        kw-name (name k)]
    (if (nil? kw-ns)
      kw-name

      (str kw-ns "/" kw-name))))

(declare parse*)

(defn maybe-parse-child [c]
  (if (vector? c)
    (parse* c)
    c))

(def ^:dynamic *hiccup-element*)

(defn parse* [vec]
  (if-not (vector? vec)
    (throw (ex-info (str vec " is not a valid hiccup vector.") {}))
    (let [[el props & children] vec
          el (if (keyword? el) (keyword->str el) el)
          props? (map? props)
          children (cond
                     (and props? (seq children)) children
                     (and (not props?) (seq children)) (cons props children)
                     (not props?) (list props)
                     true nil)
          props (if props?
                  props
                  nil)]
      #?(:clj `(~'hiccup-element ~el ~props ~(map maybe-parse-child children))
         :cljs (*hiccup-element* el props (map maybe-parse-child children))))))

(defmacro compile [vec]
  (parse* vec))

(defn from-reader [vec]
  (parse* vec))

#?(:cljs
   (do (cljs.reader/register-tag-parser! 'hiccup/next from-reader)
       (cljs.reader/register-tag-parser! 'h/n from-reader)))

(defn interpret [vec]
  (parse* vec))
