(ns solver.solver
  (:require [solver.recipes :as recipes]
            [cljs.pprint :refer [pprint cl-format]]))

;; example:
;; "lab"
;;  {"products"
;;   [{"type" "item", "name" "lab", "probability" 1, "amount" 1}],
;;   "energy" 2,
;;   "group" {"name" "production", "type" "item-group"},
;;   "enabled" true,
;;   "emissions_multiplier" 1,
;;   "subgroup" {"name" "production-machine", "type" "item-subgroup"},
;;   "localised_name" ["entity-name.lab"],
;;   "name" "lab",
;;   "ingredients"
;;   [{"type" "item", "name" "iron-gear-wheel", "amount" 10}
;;    {"type" "item", "name" "electronic-circuit", "amount" 10}
;;    {"type" "item", "name" "transport-belt", "amount" 4}],
;;   "order" "g[lab]",
;;   "main_product"
;;   {"type" "item", "name" "lab", "probability" 1, "amount" 1},
;;   "category" "crafting"}

(def recipes recipes/recipe)
(def assembling-machines recipes/assembling-machine)

;; producers: {"name" {:speed n, :categories #{cat}}, ...}
(def producers
  (merge (into (sorted-map)
               (map (fn [[name
                          {speed "crafting_speed",
                           categories "crafting_categories"}]]
                      [name {:speed speed, :categories (set (keys categories))}])
                    recipes/assembling-machine))
         (into (sorted-map)
               (map (fn [[name
                          {speed "crafting_speed",
                           categories "crafting_categories"}]]
                      [name {:speed speed, :categories (set (keys categories))}])
                    recipes/furnace))
         (into (sorted-map)
               (map (fn [[name
                          {speed "mining_speed",
                           categories "resource_categories"}]]
                      [name {:speed speed, :categories (set (keys categories))}])
                    recipes/mining-drill))))

;; Recipes are handled by-name

;; recipe -> [{"type" type, "name" name, "amount" amount}]
(defn ingredients [recipe]
  (get-in recipes [recipe "ingredients"]))

;; What input rates are required to make /rate/ of /recipe/?
;; recipe -> {name rate, ...}
(defn required-rates [recipe rate]
  (let [output-amount (get-in recipes [recipe "main_product" "amount"])]
    (reduce (fn [rates {name "name", amount "amount"}]
              (assoc rates name
                     (/ (* rate amount) output-amount)))
            {}
            (ingredients recipe))))

;; :best-assemblers is a priority list of which assemblers to
;; prefer. If no preferred assemblers can build the recipe, the
;; random "first" one found will be used. Only a factor for
;; assembling-machine-[123].
;;
;; config: {:bases #{recipe...} :best-assemblers [assembler...]}

;; recipe rate config -> {name rate, ...}
(defn all-rates [recipe config]
  (loop [unsatisfied {recipe 1},
         satisfied (sorted-map)]
    (if (= unsatisfied {})
      satisfied
      (let [[next-recipe next-rate] (first unsatisfied)
            unsatisfied_ (dissoc unsatisfied next-recipe)
            satisfied_ (merge-with + satisfied {next-recipe next-rate})]
        (if (contains? (config :bases) next-recipe)
          (recur unsatisfied_ satisfied_)
          (let [new-rates (required-rates next-recipe next-rate)]
            (recur (merge-with + unsatisfied_ new-rates)
                   satisfied_)))))))

(defn scale-rates [rates n]
  (into (sorted-map)
        (map (fn [[name rate]] [name (* rate n)])
             rates)))

(defn assembler-can-make? [asm recipe]
  (let [cat (get-in recipes [recipe "category"])]
    (boolean (get-in producers [asm :categories cat]))))

;; what assemblers can build this recipe?
;;
;; recipe -> (asms...)
(defn assemblers-for [recipe]
  (filter (fn [asm] (assembler-can-make? asm recipe))
          (keys producers)))

(defn best-assembler [recipe config]
  (or (first (filter #(assembler-can-make? % recipe) (config :best-assemblers)))
      (first (assemblers-for recipe))))

(defn time-for [recipe]
  (get-in recipes [recipe "energy"]))

(defn speed-of [asm]
  (get-in producers [asm :speed]))
    
;; The assembler to use is decided by the recipe's `category`, to be
;; matched against the assembling-machine's available
;; `crafting-categories`. In case multiple assemblers are applicable,
;; the default will be decided by config.
;; 
;; {name rate, ...} config -> {name {:assembler asm, :count n}, ...}
(defn analyze-rates [rates config]
  (into (sorted-map)
        (map (fn [[recipe rate]]
               (let [asm (best-assembler recipe config)
                     rate-per-asm
                     (/ (* (get-in recipes [recipe "main_product" "amount"])
                           (speed-of asm))
                        (time-for recipe))]
                 [recipe {:assembler asm,
                          :count (/ rate rate-per-asm)}]))
             rates)))
