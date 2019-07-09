# thump

## UNDER CONSTRUCTION

A library for parsing hiccup forms using reader tagged literals. Currently supports React.

```clojure
(ns my-app.core
  (:require [thump.core]
            [thump.react :refer [hiccup-element]]))

(defn Mycomponent [props]
  (let [name (goog.object/get props "name")]
    #h/e [:div {:style {:color "green"}}
          [:span "Hello, " name]
          [:ul
           (for [n (range 10)]
             #h/e [:li {:key n} n])]]))

(react-dom/render #h/e [MyComponent {:name "Sydney"}]
                  (. js/document getElementById "app"))
```


## Why reader tags?

Reader tags are excellent for succinctly writing code-as-data. They can be used
while writing code in our editor, as well as sent over the wire using the EDN 
reader.

Reader tags are also much more performant than doing the hiccup parsing at
runtime. Typically, runtime hiccup parsing involves:

1. Construct vectors representing hiccup data
2. Parse vectors and turn them into function calls.
3. Execute the functions to construct the target type (e.g. React elements)

If your entire app is written using hiccup, these steps will be done for every
single component in your tree.

Using reader tags allows us to move steps **1** and **2** to compile time, so
that our application only has to execute the functions to construct the target
type at runtime.

(Sidenote: for React developers, this is the exact same thing that JSX does!)


## Usage

`thump` exports two reader tags at the moment: `hiccup/element`, which parses
hiccup literals, and `h/e`, which is a shortened alias of `hiccup/element`.

In order to use it, you must require the `thump.core` namespace at the top
level of your application:

```clojure
(ns my-app.core
  (:require [thump.core]
            ...))
```

This will ensure the reader tags are registered with the ClojureScript compiler.

### With React

`thump` is meant to be a general purpose hiccup syntax parsing library. An
example implementation of a React extension is included with the library under
the `thump.react` namespace.

In order to use hiccup to create React elements, simply include the namespace
and **refer the `hiccup-element` var**:

```
(my-app.feature
  (:require [thump.react :refer [hiccup-element]]))
```

We can then start creating React elements:

```clojure
#hiccup/element [:div "foo"]
;; Executes => (react/createElement "div" nil "foo")
```

### Elements

Elements in the first position of a `hiccup/element` / `h/e`-tagged form are
expected to be one of the following:

- A keyword representing a DOM element: `:div`, `:span`, `:h1`, `:article`
- A vanilla React component or one of the special React components like `Fragment`
- A set of special keywords that `thump` exposes:
  - `:<>` as an alias for Fragments


### Props

Props are expected to be passed in as map literals with keywords as keys,
such as `{:key "value"}`.

The top-level map will be rewritten as a JS object at compile time. Any nested
Clojure data will be left alone. Keys are converted from kebab-case to camelCase.

Example:

```clojure
#h/e [:div {:id "thing-1" :some-prop {:foo #{'bar "baz"}}}]
;; => (react/createElement "div"
;;                         (js-obj "id" "thing-1"
;;                                 "someProp" {:foo #{'bar "baz"}}))
```

There are 3 exceptions to this:
- `:style` - will be recursively converted to a JS object via `clj->js`
- `:class` - will be renamed to `className` and (if its a collection) joined as a string
- `:for` - will be renamed to `htmlFor`

Example of special cases:

```clojure
#h/e [:div {:class ["foo" "bar"]
            :style {:color "green"}
            :for "thing"}]
;; => (react/createElement "div"
;;                         (js-obj "className" "foo bar"
;;                                 "style" (clj->js {:color "green"})
;;                                 "htmlFor" "thing"))
```

### Dynamic props

Using `thump`, props must _always_ be a literal map. For instance, the 
following **will throw a runtime error**:

```clojure
(let [props {:style {:color "red"}}]
  #h/e [:div props "foo"])
```

When the tag reader encounters `props` in the hiccup form, it assumes it is a
child element and passes it in to React's `createElement` function like so:

```clojure
(let [props {:style {:color "red"}}]
  (react/createElement "div" nil props "foo"))
```

Since `props` is a map, not a React element, when used it will cause React to throw an "unknown element type" error.

The only way to tell the tag reader to treat `props` as, well, props, is to
write it literally within the hiccup form:

```clojure
#h/e [:div {:style {:color "red"}} "foo"]
```

But **what if we want to assign them dynamically?** For example, we want to
set some data conditionally:

```clojure
(if condition
  {:style {:color "red"}}
  {:style {:color "green"}})
```

Then we can tell the reader to merge our dynamically created map with the `&` prop:

```clojure
(let [props (if condition
              {:style {:color "red"}}
              {:style {:color "green"}})]
  #h/e [:div {& props} "foo"])
```

The value at the key `&` will be merged into the resulting props object at 
runtime so that we can do this kind of dynamic props creation.

Keys are merged in such a way where the values in the map created statically
take precedence. For example:

```clojure
(let [props {:style {:color "red"}
             :on-click #(js/alert "hi")}]
  #h/e [:div {:style {:color "blue"} & props}
              "foo"])
```

Results in props `#js {:style #js {:color "blue"} :onClick #(js/alert)}` being
passed in to React.

### Nested hiccup

Often, our hiccup is not just one layer deep. We often want to write a tree of
elements like:

```html
<div>
  <div><label>Name: <input type="text" /></label></div>
  <div><button type="submit">Submit</button></div>
</div>
```

For convenience, if the reader encounters a nested vector literal within a hiccup
form, it will treat it as a child element and read it just like another hiccup
form. This means we can write the above without repeating the `#h/e` tag over and
over:

```clojure
#h/e [:div
      [:div [:label "Name: " [:input {:type "text"}]]]
      [:div [:button {:type "submit"} "Submit"]]]
```

However, the hiccup reader will not continue to walk inside of anything but a
vector. If we need to insert parens into the form in order to do something more
dynamic we'll have to ensure that we return a React element ourselves.

The following **will probably throw a runtime error**:

```clojure
#h/e [:div
      [:div "The condition is:"]
      (if condition
        [:div "TRUE"]
        [:div "FALSE"])]
```

To fix it, we make sure our dynamic children are read as hiccup as well by 
tagging them:

```clojure
#h/e [:div
      [:div "The condition is:"]
      (if condition
        #h/e [:div "TRUE"]
        #h/e [:div "FALSE"])]
```

This is the case for any other kind of form like `for`, `cond`, `map`, etc.



## License

EPL 2.0 Licensed. Copyright Will Acton.
