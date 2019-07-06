(ns thump.react
  (:require [clojure.string :as str]
            #?@(:cljs [["react" :as react]
                       [goog.object :as gobj]])
            [thump.core])
  #?(:cljs (:require-macros [thump.react]
                            [thump.core])))

(defn keyword->str [k]
  (let [kw-ns (namespace k)
        kw-name (name k)]
    (if (nil? kw-ns)
      kw-name

      (str kw-ns "/" kw-name))))

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
        (name s)
        (-> (map str/capitalize words)
            (conj first-word)
            str/join)))
    s))

(defn map-entry->obj-entry [[k v]]
  (case k
    :style ["style" #?(:clj `(~'clj->js ~v :keyword-fn camel-case*)
                       :cljs (clj->js v :keyword-fn camel-case*))]
    :class ["className" #?(:clj `(if (string? ~v) ~v
                                     (->> ~v (remove nil?) (str/join " ")))
                           :cljs (if (string? v)
                                   v
                                   (->> v (remove nil?) (str/join " "))))]
    :for ["htmlFor" v]
    [(-> k (keyword->str) (camel-case*)) v]))

#?(:cljs (defn merge-obj+map [obj m]
           (doseq [[k v] (map map-entry->obj-entry m)]
             (gobj/set obj k v))
           obj))

(defn props->obj [m]
  (if (contains? m '&)
    #?(:clj `(merge-obj+map (~'js-obj ~@(mapcat map-entry->obj-entry (dissoc m '&)))
                            ~(get m '&))
       :cljs (merge-obj+map (apply gobj/create (mapcat map-entry->obj-entry (dissoc m '&)))
                            (get m '&)))
    #?(:clj `(~'js-obj ~@(mapcat map-entry->obj-entry m))
       :cljs (apply gobj/create (mapcat map-entry->obj-entry m)))))

(def create-element
  #?(:clj (fn [& xs] xs)
     :cljs react/createElement))

#?(:cljs (def Fragment react/Fragment))

(def custom-els
  (atom {"<>" #?(:clj `Fragment
                 :cljs react/Fragment)}))

(defmacro register-element! [el component]
  (swap! custom-els assoc (keyword->str el) component)
  `(swap! custom-els assoc (keyword->str ~el) ~component))

(defmacro hiccup-element [el props children]
  `(create-element ~(get @custom-els el el) ~(props->obj props) ~@children))

#?(:cljs (defn hiccup-element [el props children]
           (apply react/createElement
                  (get @custom-els el el)
                  (props->obj props)
                  children)))

#?(:cljs (defn interpret [vec]
           (binding [thump.core/*hiccup-element* hiccup-element]
             (thump.core/interpret vec))))
