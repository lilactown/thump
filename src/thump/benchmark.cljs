(ns thump.benchmark
  (:require
   ["react" :as react :rename {createElement rce}]
   ["react-dom/server" :as rdom]
   ["benchmark" :as benchmark]
   [thump.react :as r :refer [hiccup-element]]
   [cljs.reader]))

(defn react-render [{:keys [title body]}]
  (rce "div" #js {:className "card"}
       (rce "div" #js {:className "card-title"} title)
       (rce "div" #js {:className "card-body"} body)
       (rce "div" #js {:className "card-footer"}
            (rce "div" #js {:className "card-actions"}
                 (rce "button" nil "ok")
                 (rce "button" nil "cancel")))))

(defn tag-render [{:keys [title body]}]
  #h/n [:div {:class "card"}
        [:div {:class "card-title"} title]
        [:div {:class "card-body"} body]
        [:div {:class "card-footer"}
         [:div {:class "card-actions"}
          [:button "ok"]
          [:button "cancel"]]]])

(defn macro-render [{:keys [title body]}]
  (thump.core/compile
   [:div {:class "card"}
    [:div {:class "card-title"} title]
    [:div {:class "card-body"} body]
    [:div {:class "card-footer"}
     [:div {:class "card-actions"}
      [:button "ok"]
      [:button "cancel"]]]]))

(defn runtime-render [{:keys [title body]}]
  (r/interpret
   [:div {:class "card"}
    [:div {:class "card-title"} title]
    [:div {:class "card-body"} body]
    [:div {:class "card-footer"}
     [:div {:class "card-actions"}
      [:button "ok"]
      [:button "cancel"]]]]))

(defn runtime-reader-render [{:keys [title body]}]
  (binding [thump.core/*hiccup-element* hiccup-element]
    (cljs.reader/read-string
     (str "#h/n [:div {:class \"card\"}
        [:div {:class \"card-title\"} \"" title "\"]
        [:div {:class \"card-body\"} \"" body "\"]
        [:div {:class \"card-footer\"}
         [:div {:class \"card-actions\"}
          [:button \"ok\"]
          [:button \"cancel\"]]]]"))))

(defn log-cycle [event]
  (println (.toString (.-target event))))

(defn log-complete [event]
  (this-as this
    (js/console.log this)))

(set! js/Benchmark benchmark)

(defn ^:export main [& args]
  (let [test-data {:title "hello world"
                   :body "body"}
        test-data-js #js {:title "hello world"
                          :body "body"}]
    (println (rdom/renderToString (react-render test-data)))
    (println (rdom/renderToString (tag-render test-data)))
    (println (rdom/renderToString (macro-render test-data)))
    (println (rdom/renderToString (runtime-render test-data)))
    (println (rdom/renderToString (runtime-reader-render test-data)))

    (when-not (= (rdom/renderToString (react-render test-data))
                 (rdom/renderToString (tag-render test-data))
                 (rdom/renderToString (macro-render test-data))
                 (rdom/renderToString (runtime-render test-data))
                 (rdom/renderToString (runtime-reader-render test-data))
                 )
      (throw (ex-info "not equal!" {})))

    (-> (benchmark/Suite.)
        (.add "react" #(react-render test-data))
        (.add "tag" #(tag-render test-data))
        (.add "macro" #(macro-render test-data))
        (.add "runtime" #(runtime-render test-data))
        (.add "runtime-reader" #(runtime-reader-render test-data))
        (.on "cycle" log-cycle)
        (.on "complete" log-complete)
        (.run))))
