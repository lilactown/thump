(ns react.dom
  (:require [clojure.string :as str]
            [react :as r])
  (:import
   [clojure.lang IPersistentVector ISeq Named Numbers Ratio Keyword]))

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

(defprotocol IDomRenderable
  (^String render [type props sb]))

(def ^{:doc "A list of elements that must be rendered without a closing tag."
       :private true}
  void-tags
  #{"area" "base" "br" "col" "command" "embed" "hr" "img" "input" "keygen" "link"
    "meta" "param" "source" "track" "wbr"})


(defn escape-html [^String s]
  (let [len (count s)]
    (loop [^StringBuilder sb nil
           i                 (int 0)]
      (if (< i len)
        (let [char (.charAt s i)
              repl (case char
                     \& "&amp;"
                     \< "&lt;"
                     \> "&gt;"
                     \" "&quot;"
                     \' "&#x27;"
                     nil)]
          (if (nil? repl)
            (if (nil? sb)
              (recur nil (inc i))
              (recur (doto sb
                       (.append char))
                     (inc i)))
            (if (nil? sb)
              (recur (doto (StringBuilder.)
                       (.append s 0 i)
                       (.append repl))
                     (inc i))
              (recur (doto sb
                       (.append repl))
                     (inc i)))))
        (if (nil? sb) s (str sb))))))

(defn get-value [props]
  (or (:defaultValue props)
      (:value props)))



(defn render-element! [el sb]
  (if-not (r/isElement el)
    (append! sb (to-str el))
    (let [{:keys [type props]} el]
      (if (satisfies? IDomRenderable type)
        (render type props sb)
        (throw (ex-info "Element type is not DOM renderable." el))))))

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

(extend-protocol IDomRenderable
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

(renderToString (r/createElement "div" {:className "asdf"}
                                 (r/createElement "div" nil "foo")
                                 "bar"
                                 123))

