(ns solver.importer
    (:require [clojure.data.json :as json]))


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

(defn convert []
  (clojure.pprint/pprint (read-files) (clojure.java.io/writer "recipes.clj-exp")))

(convert)
