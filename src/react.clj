(ns react)

(defn createElement
  ([el props & children]
   ;; `[~el ~(when props `(react/props ~@props)) ~@children]
   `{"$$typeof" "Symbol(react.element)"
     :type ~el
     :key ~(:key props)
     :props ~props}))
