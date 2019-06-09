(ns hiccup-tag.examples
  (:require [devcards.core :as dc :include-macros true]
            [hiccup-tag.react]
            ["react" :as react]))

(defn ^:dev/after-load start []
  (dc/start-devcard-ui!))

(defn ^:export init [] (start))

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
