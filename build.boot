(set-env!
 :source-paths #{"src/cljs" "src/clj"}
 :resource-paths #{"html"}

 :dependencies '[[org.clojure/clojure "1.10.2"]
                 [org.clojure/clojurescript "1.10.339"]
                 [adzerk/boot-cljs "2.1.5"]
                 [pandeiro/boot-http "0.8.3"]
                 [adzerk/boot-reload "0.6.0"]
                 [adzerk/boot-cljs-repl "0.4.0"]
                 ;; for cljs-repl
                 [cider/piggieback "0.5.2" :scope "test"]
                 [weasel "0.7.1" :scope "test"]
                 [nrepl "0.8.3" :scope "test"]

                 [reagent "1.0.0"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]]
         '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
         '[solver.importer :as importer])

(deftask gen-imports
  "Generates recipes.cljs"
  []
  (fn [next-handler]
    (fn [fileset]
      (importer/-main))))
