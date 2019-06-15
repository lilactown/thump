(ns react.dom.dom-property)

(def types #{::reserved
             ::string
             ::boolean-ish
             ::boolean
             ::overloaded-boolean
             ::numeric
             ::positive-numeric})

(def root-attribute-name "data-reactroot")

(def id-attribute-name "data-reactid")

(def attribute-name-start-char
  ":A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD")

(def attribute-name-char (str attribute-name-start-char
                              "\\-.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040"))

(def validate-attr-re
  (re-pattern (str "^[" attribute-name-start-char "]"
                   "[" attribute-name-char "]*$")))


(def illegal-attr-name-cache (atom {}))

(def validated-attr-name-cache (atom {}))

(defn attribute-name-safe? [attr-name]
  (cond
    (contains? validated-attr-name-cache attr-name) true
    (contains? illegal-attr-name-cache attr-name) false
    (re-find validate-attr-re attr-name) (do (swap! validated-attr-name-cache assoc attr-name)
                                             true)
    :else (do (swap! illegal-attr-name-cache assoc attr-name)
              false)))

(defn ignore-attribute? [attr-name]
  )
