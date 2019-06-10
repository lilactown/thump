(ns hiccup-tag.examples
  (:require [devcards.core :as dc :include-macros true]
            [hiccup-tag.react]
            ["react" :as react]))

;;
;; Boilerplate
;;

(defn ^:dev/after-load start []
  (dc/start-devcard-ui!))

(defn ^:export init [] (start))

(when (exists? js/Symbol)
  (extend-protocol IPrintWithWriter
    js/Symbol
    (-pr-writer [sym writer _]
      (-write writer (str "\"" (.toString sym) "\"")))))

;;
;; Examples
;;

(dc/defcard basic
  #hiccup/react [:div "hello"])

(dc/defcard basic-short
  ;; h/r is an alias of hiccup/react
  #h/r [:div "hi"])

(dc/defcard basic-nested
  ;; we don't need to tag static children
  #h/r [:div [:span "hi"] " " [:span "bye"]])

(dc/defcard basic-props
  #h/r [:div {:style {:background "purple"}}
        [:button {:on-click #(js/alert "hi")} "say hello"]])

(dc/defcard dynamic-props
  (let [props {:style {:background "red" :color "yellow"}}]
    #h/r [:div {;; :style {:background "purple"}
                & props} "asdf"]))

(dc/defcard lazy-seq-and-binding
  ;; we have to tag children that are bound dynamically
  (let [neg-1 #h/r [:li -1]]
    #h/r [:ul
          neg-1
          ;; no tagging needed, static child
          [:li 0]
          ;; we also have to tag children that are generated dynamically
          (for [n [1 2 3 4 5]]
            #h/r [:li {:key n} n])]))
