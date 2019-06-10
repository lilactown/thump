(ns hiccup-tag.react
  #?(:cljs (:require [goog.object :as gobj]
                     ["react" :as react]
                     [clojure.string :as str])
     :clj (:require [clojure.string :as str])))

(defn- camel-case*
  "Returns camel case version of the string, e.g. \"http-equiv\" becomes \"httpEquiv\"."
  [s]
  (if (or (keyword? s)
          (string? s)
          (symbol? s))
    (let [[first-word & words] (str/split (name s) #"-")]
      (if (or (empty? words)
              (= "aria" first-word)
              (= "data" first-word))
        s
        (-> (map str/capitalize words)
            (conj first-word)
            str/join)))
    s))

(defn keyword->str [k]
  (let [kw-ns (namespace k)
        kw-name (name k)]
    (if (nil? kw-ns)
      kw-name

      (str kw-ns "/" kw-name))))

(defn map-entry->obj-entry [[k v]]
  (case k
    :style ["style" #?(:clj `(~'clj->js ~v) ;; could maybe do this recursively at read
                       :cljs (clj->js v))]
    :class ["className" #?(:clj `(->> ~v (remove nil?) (str/join " "))
                           :cljs (->> v (remove nil?) (str/join " ")))]
    [(-> k (keyword->str) (camel-case*)) v]))

#?(:cljs (defn merge-obj+map [obj m]
           (doseq [[k v] (map map-entry->obj-entry m)]
             (gobj/set obj k v))
           obj))

(defn props->obj [m]
  ;; need to special case `style` as well
  (if (contains? m '&)
    #?(:clj `(merge-obj+map (~'js-obj ~@(mapcat map-entry->obj-entry (dissoc m '&)))
                            ~(get m '&))
       :cljs (merge-obj+map (apply js-obj (mapcat map-entry->obj-entry (dissoc m '&)))
                            (get m '&)))
  #?(:clj `(~'js-obj ~@(mapcat map-entry->obj-entry m))
     :cljs (apply js-obj (mapcat map-entry->obj-entry m)))))

(comment
  (props->obj {:foo "bar" :baz 123}))

(declare react-from-reader)

(defn maybe-read-child [c]
  (if (vector? c)
    (react-from-reader c)
    c))

(defn create-element [el props children]
  #?(:clj `(react/createElement ~el ~props ~@(map maybe-read-child children))
     :cljs (react/createElement el props (if (= 0 (count children))
                                           (first children)
                                           (apply array children)))))

(defn react-from-reader [vec]
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
          props (if props? (props->obj props) nil)]
      (create-element el props children))))

(comment
  (react-from-reader [:div "foo" "bar"])

  (react-from-reader [:div {} "foo" "bar"])

  (react-from-reader [:div {:style {:color "green"}} "foo" "bar"])

  (react-from-reader '[:div {& props} "foo" "bar"])

  (def r #'react-from-reader)

  (r [:div (r [:span {:asdf "jkl"} "hi"])])
  )
