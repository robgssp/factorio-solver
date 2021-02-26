(ns solver.core
  (:require [solver.solver :as s]
            [cljs.pprint :refer [pprint cl-format]]
            [reagent.core :as r]
            [reagent.dom :as rd]))

(enable-console-print!)

(println "test three")

(defn timer-component []
  (let [seconds (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds inc) 1000)
      [:div "Seconds elapsed: " @seconds])))

(def target-component (r/atom "processing-unit"))
(def target-rate (r/atom 1))
(def config (r/atom {:bases #{"sulfuric-acid"}}))

(defn str2bool [v]
  (cond (= (js->clj v) "true") true
        (= (js->clj v) "false") false
        :else (throw (js/Error. (str "Non-truthy string encountered: " v)))))

(defn condisj [set v on?]
  (if on? (conj set v) (disj set v)))

(defn display-rates []
  (let [rates (s/all-rates @target-component @target-rate @config)]
    (cl-format true "rates: ~a~%" rates)
    [:table
     [:tbody
      [:tr [:td "Component:"] [:td "Rate:"] [:td "Base?"]]
      (doall
       (map (fn [[component rate]]
              ^{:key component}
              [:tr
               [:td component] [:td rate]
               [:td [:input
                     {:type "checkbox",
                      :checked (boolean (get-in @config [:bases component])),
                      :on-change
                      (fn [e]
                        (swap! config
                               (fn [cfg]
                                 (update cfg :bases
                                         #(condisj % component
                                                   (.. e -target -checked))))))}]]])
            (into [] rates)))]]))


(defn calc-rates []
  [:div
   [:span "Target component: "]
   [:input {:type "search",
            :value @target-component,
            :on-change #(do
                          (cl-format true "Here!~%")
                          (reset! target-component (-> % .-target .-value)))}]
   [:br]
   [:span "Rate: "]
   [:input {:type "number",
            :value @target-rate,
            :on-change #(reset! target-rate (-> % .-target .-value))}]
   [display-rates target-component target-rate config]])

(rd/render [calc-rates] (.getElementById js/document "content"))
