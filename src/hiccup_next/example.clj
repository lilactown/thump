(ns hiccup-next.example
  (:require [hiccup-next.core :as hiccup]))


(defn hiccup-element [& xs] xs)

(hiccup/compile [:div {:baz {:asdf :jjkl}}
                 "foo" "bar"
                 [:span "baz"]])
