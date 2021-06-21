(ns solver.core
  (:require [solver.solver :as s]
            [cljs.pprint :refer [pprint cl-format]]
            [reagent.core :as r]
            [reagent.dom :as rd]))

(enable-console-print!)

(defn timer-component []
  (let [seconds (r/atom 0)]
    (fn []
      (js/setTimeout #(swap! seconds inc) 1000)
      [:div "Seconds elapsed: " @seconds])))

(def target-component (r/atom "processing-unit"))
(def target-rate (r/atom 1))
(def config (r/atom {:bases #{"sulfuric-acid"},
                     :best-assemblers ["assembling-machine-2"]}))

(defn str2bool [v]
  (cond (= (js->clj v) "true") true
        (= (js->clj v) "false") false
        :else (throw (js/Error. (str "Non-truthy string encountered: " v)))))

(defn condisj [set v on?]
  (if on? (conj set v) (disj set v)))

(defn display-rates []
  (let [rates1 (s/all-rates @target-component @config)
        rates (s/scale-rates rates1 @target-rate)
        producers1 (s/analyze-rates rates1 @config) ;; TODO: calculate then scale, don't calculate twice. duh.
        producers (s/analyze-rates rates @config)]
    (cl-format true "rates: ~a~%" rates)
    [:table
     [:tbody
      [:tr [:td "Component:"] [:td "Rate:"] [:td "Base?"] [:td "# Factories:"] [:td "Type:"]]
      (doall
       (map (fn [[component rate]]
              ^{:key component}
              [:tr
               [:td component]
               [:td [:input {:type "number",
                             :step "any",
                             :value rate,
                             :on-change
                             (fn [e]
                               (let [rate1 (rates1 component)
                                     new-rate (.. e -target -value)]
                                 (reset! target-rate (* (/ new-rate rate1)))))}]]
               [:td [:input
                     {:type "checkbox",
                      :checked (boolean (get-in @config [:bases component])),
                      :on-change
                      (fn [e]
                        (swap! config
                               (fn [cfg]
                                 (update cfg :bases
                                         #(condisj % component
                                                   (.. e -target -checked))))))}]]
               [:td
                [:input
                 {:type "number",
                  :step "any",
                  :value (get-in producers [component :count]),
                  :on-change
                  (fn [e]
                    (let [count1 (get-in producers1 [component :count])
                          new-count (.. e -target -value)]
                      (reset! target-rate (/ new-count count1))))}]]
               [:td (get-in producers [component :assembler])]])
            (into [] rates)))]]))

(defn calc-rates []
  [:div
   [:label "Target component: "]
   [:input {:type "search",
            :list "components",
            :value @target-component,
            :on-change #(reset! target-component (.. % -target -value))}]
   [:datalist {:id "components"}
    (doall (map (fn [[recipe data]]
                  [:option {:value recipe} recipe])
                s/recipes))]
   [:label "Default assembler: "]
   [:input {:list "default-asms"
            :on-change (fn [e]
                         (let [new-best (.. e -target -value)]
                           (swap! config
                                  (fn [cfg]
                                    (update cfg :best-assemblers
                                            (fn [best]
                                              [new-best]))))))}]
   [:datalist {:id "default-asms"}
    (doall (map (fn [name] [:option {:value name} name])
                ["assembling-machine-1" "assembling-machine-2" "assembling-machine-3"]))]
   [:br]
   [:span "Rate: "]
   [:input {:type "number",
            :step "any",
            :value @target-rate,
            :on-change #(reset! target-rate (-> % .-target .-value))}]
   [display-rates]
   [:p "Handy notes:"]
   [:p "Yellow belt: 7.5 items / sec / side (15 both)"]
   [:p "Red belt: 15 items / sec / side ( 30 both)"]
   [:p "Blue belt: 22.5 items / sec / side (45 both)"]])

(rd/render [calc-rates] (.getElementById js/document "content"))
