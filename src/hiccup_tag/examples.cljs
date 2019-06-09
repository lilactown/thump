(ns hiccup-tag.examples
  (:require [devcards.core :as dc :include-macros true]
            [hiccup-tag.react]
            ["react" :as react]))

(defn ^:dev/after-load start []
  (dc/start-devcard-ui!))

(defn ^:export init [] (start))


(dc/defcard basic
  #hiccup/react [:div "hi"])

(dc/defcard basic-nested
  #h/r [:div [:span "hi"] " " [:span "bye"]])

(dc/defcard basic-props
  #h/r [:div {:style {:background "purple"}} [:button {:on-click #(js/alert "hi")} "say hello"]])


(dc/defcard lazy-seq-and-binding
  (let [neg-1 #h/r [:li -1]]
    #h/r [:ul
          neg-1
          [:li 0]
          (for [n [1 2 3 4 5]]
            #h/r [:li {:key n} n])]))
