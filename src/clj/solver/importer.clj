(ns solver.importer
  (:require [clojure.data.json :as json]
            [clojure.pprint :refer [pprint]]))


(def imports ["assembling-machine"
              "boiler"
              "equipment-grid"
              "equipment"
              "fluid"
              "furnace"
              "generator"
              "inserter"
              "item"
              "lab"
              "mining-drill"
              "reactor"
              "recipe"
              "resource"
              "solar-panel"
              "technology"
              "tile"
              "transport-belt"])

(defn read-file [x]
  (json/read (java.io.FileReader. (clojure.java.io/file x))))

(defn read-files []
  (into {}
        (map (fn [import] [import (read-file (str "exported-recipes/" import ".json.fixed"))])
             imports)))

(defn -main []
  (with-open [out (clojure.java.io/writer "src/cljs/solver/recipes.cljs")]
    (pprint '(ns solver.recipes) out)
    (pprint `(def ~(symbol "all") ~(read-files)) out)
    (doseq [cat imports]
      (pprint `(def ~(symbol cat) (get ~(symbol "all") ~cat)) out))))
