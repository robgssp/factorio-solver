(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[org.clojure/clojure "1.10.2"]
                 [org.clojure/clojurescript "1.10.764"]
                 [http-kit "2.5.3"]
                 [adzerk/boot-cljs "2.1.5"]])

(require '[adzerk.boot-cljs :refer [cljs]])
