(ns react)

(defprotocol IElementType)

(defn createElement
  ([type props & children]
   {:type type
    :key (:key props)
    :props (-> props
               (dissoc :key)
               (assoc :children children))}))

(defn isElement [x]
  ;; quack quack
  (and (map? x)
       (contains? x :type)
       (contains? x :props)
       (satisfies? IElementType (:type x))))

(extend-protocol IElementType
  clojure.lang.Fn
  java.lang.String)
