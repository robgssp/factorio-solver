(ns solver.solver)

;; categories:
;; "boiler"
;; "equipment-grid"
;; "equipment"
;; "fluid"
;; "furnace"
;; "generator"
;; "inserter"
;; "item"
;; "lab"
;; "mining-drill"
;; "reactor"
;; "recipe"
;; "resource"
;; "solar-panel"
;; "technology"
;; "tile"
;; "transport-belt"

(def all-data
  (with-open [f (java.io.PushbackReader. (clojure.java.io/reader "recipes.clj-exp"))]
    (read f)))
  
(def recipes (get all-data "recipe"))

(defn ingredients [rec]
  (get recipes rec ingredients))
