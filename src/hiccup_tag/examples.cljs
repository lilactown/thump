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

(dc/defcard more-nested
  ;; we don't need to tag static children
  #h/r [:div
        [:div {:style {:color "green"}}
         [:span "hi"]]
        " "
        [:div [:h4 "bye" [:span {:style {:color "red"}} "bye"]]]])

(dc/defcard basic-props
  #h/r [:div {:style {:background "purple"}}
        [:button {:on-click #(js/alert "hi")} "say hello"]])

(dc/defcard dynamic-props
  (let [props {:style {:background "red" :color "yellow"}}]
    #h/r [:div {:on-click #(js/alert "static")
                & props} "asdf"]))

(dc/defcard classes
  #h/r [:<>
        [:style ".a { color: green; } .b { background: purple; }"]
        [:div {:class "a"} "green"]
        [:div {:class "b"} "purple"]
        [:div {:class ["a" "b"]} "gross"]])

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

(dc/defcard from-read-string
  #h/r [:div
        (cljs.reader/read-string
         "#hiccup/react [:div {:style {:border \"1px solid #eee\"}}
                         [:span {:style {:color \"green\"}} \"from reader!\"]]")
        (cljs.reader/read-string
         "#h/r [:<>
                [:style \".a2 { color: green; } .b2 { background: purple; }\"]
                [:div {:class \"a2\"} \"green\"]
                [:div {:class \"b2\"} \"purple\"]
                [:div {:class [\"a2\" \"b2\"]} \"gross\"]]")
        (try (cljs.reader/read-string
              "#h/r [:div {& props} \"asdf\"]")
             (catch js/Error e
               #h/r [:div {:style {:color (if (= (ex-message e) "props is not ISeqable")
                                            "green"
                                            "red")}}
                     (str "Dynamic props doesn't work: " (ex-message e) " "
                          (if (= (ex-message e) "props is not ISeqable")
                            "✅"
                            "🚫"))]))])
