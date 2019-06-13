(ns react
  (:require [clojure.string :as str])
  (:import
   [clojure.lang IPersistentVector ISeq Named Numbers Ratio Keyword])
  )

(defn createElement
  ([type props & children]
   ;; `[~el ~(when props `(react/props ~@props)) ~@children]
   {:type type
    :key (:key props)
    :props (-> props
             (dissoc :key)
             (assoc :children children))}))

(defn append!
  ([^StringBuilder sb s0] (.append sb s0))
  ([^StringBuilder sb s0 s1]
   (.append sb s0)
   (.append sb s1))
  ([^StringBuilder sb s0 s1 s2]
   (.append sb s0)
   (.append sb s1)
   (.append sb s2))
  ([^StringBuilder sb s0 s1 s2 s3]
   (.append sb s0)
   (.append sb s1)
   (.append sb s2)
   (.append sb s3))
  ([^StringBuilder sb s0 s1 s2 s3 s4]
   (.append sb s0)
   (.append sb s1)
   (.append sb s2)
   (.append sb s3)
   (.append sb s4)))

(defprotocol ToString
  (^String to-str [x] "Convert a value into a string."))

(extend-protocol ToString
  Keyword (to-str [k] (name k))
  Ratio   (to-str [r] (str (float r)))
  String  (to-str [s] s)
  Object  (to-str [x] (str x))
  nil     (to-str [_] ""))


(defprotocol IRenderable
  (^String render [type props sb]))

(defn isElement [x]
  ;; quack quack
  (and (map? x)
       (contains? x :type)
       (contains? x :props)
       (satisfies? IRenderable (:type x))))


(def ^{:doc "A list of elements that must be rendered without a closing tag."
       :private true}
  void-tags
  #{"area" "base" "br" "col" "command" "embed" "hr" "img" "input" "keygen" "link"
    "meta" "param" "source" "track" "wbr"})

(defn render-element! [el sb]
  (if-not (isElement el)
    (append! sb (to-str el))
    (let [{:keys [type props]} el]
      (render type props sb))))


(defn render-content! [tag attrs children sb]
  (append! sb "<" tag)
  (if (and (nil? children)
           (contains? void-tags tag))
    (append! sb "/>")
    (do
      (append! sb ">")
      ;; (or  (render-textarea-value! tag attrs sb)
      ;;      (render-inner-html! attrs children sb)
          (doseq [child children]
            (render-element! child sb));;)
      (append! sb "</" tag ">")))
  ;; (when (not= :state/static @*state)
  ;;   (vreset! *state :state/tag-close))
  )

(extend-protocol IRenderable
  java.lang.String
  (render [s props sb]
    (let [attrs (dissoc props :children)
          children (:children props)]
    (render-content! s attrs children sb)))

  clojure.lang.Fn
  (render [f props sb]
    (render-element! (f props) sb)))

(defn renderToString [element]
  (let [{:keys [type props]} element
        sb (StringBuilder.)]
    (render type props sb)
    (str sb)))

(renderToString (createElement "div" nil
                               (createElement "div" nil "foo")
                               "bar"
                               123))
