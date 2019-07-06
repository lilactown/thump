(ns thump.examples
  (:require [devcards.core :as dc :include-macros true]
            [thump.react :as r :refer [hiccup-element]]))


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

(defn t []
  #hiccup/element [:div "hello"])

(dc/defcard basic
  #h/e [t])

(dc/defcard basic-short
  ;; h/e is an alias of hiccup/element
  #h/e [:div "hi"])

(dc/defcard basic-nested
  ;; we don't need to tag static children
  #h/e [:div [:span "hi"] " " [:span "bye"]])

(dc/defcard more-nested
  ;; we don't need to tag static children
  #h/e [:div
        [:div {:style {:color "green"}}
         [:span "hi"]]
        " "
        [:div [:h4 "bye" [:span {:style {:color "red"}} "bye"]]]])

(dc/defcard basic-props
  #h/e [:div {:style {:background "purple"}}
        [:button {:on-click #(js/alert "hi")} "say hello"]])

(dc/defcard dynamic-props
  (let [props {:style {:background "red" :color "yellow"}}]
    #h/e [:div {:on-click #(js/alert "static")
                & props} "asdf"]))


(dc/defcard classes
  #h/e [:<>
        [:style ".a { color: green; } .b { background: purple; }"]
        [:div {:class "a"} "green"]
        [:div {:class "b"} "purple"]
        [:div {:class ["a" "b"]} "gross"]])

(dc/defcard lazy-seq-and-binding
  ;; we have to tag children that are bound dynamically
  (let [neg-1 #h/e [:li -1]]
    #h/e [:ul
          neg-1
          ;; no tagging needed, static child
          [:li 0]
          ;; we also have to tag children that are generated dynamically
          (for [n [1 2 3 4 5]]
            #h/e [:li {:key n} n])]))

(dc/defcard from-read-string
  (binding [thump.core/*hiccup-element* hiccup-element]
    #h/e [:div
          (cljs.reader/read-string
           "#hiccup/element [:div {:style {:border \"1px solid #eee\"}}
                          [:span {:style {:color \"green\"}}
                          \"from reader!\"]]")
          (cljs.reader/read-string
           "#h/e [:div
                  [:style \".a2 { color: green; } .b2 { background: purple; }\"]
                  [:div {:class \"a2\"} \"green\"]
                  [:div {:class \"b2\"} \"purple\"]
                  [:div {:class [\"a2\" \"b2\"]} \"gross\"]]")
          (try (cljs.reader/read-string
                "#h/e [:div {& props} \"asdf\"]")
               (catch js/Error e
                 #h/e [:div {:style {:color (if (= (ex-message e) "props is not ISeqable")
                                              "green"
                                              "red")}}
                       (str "Dynamic props doesn't work: " (ex-message e) " "
                            (if (= (ex-message e) "props is not ISeqable")
                              "✅"
                              "🚫"))]))]))

(dc/defcard macro-compiler
  (thump.core/compile
   [:div
    "foo"
    [:button {:on-click #(js/alert "baz")} "bar"]]))

(dc/defcard runtime-interpreter
  (r/interpret [:div
                "foo"
                [:button {:on-click #(js/alert "baz")} "bar"]]))

(dc/defcard custom-element
  (do (r/register-element! :foo (fn [_] "foo"))
      #h/e [:foo]))
