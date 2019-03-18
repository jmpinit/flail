(defproject flail "0.1.0-SNAPSHOT"
  :description "Creative coding environment for KUKA industrial robot arms"
  :url "https://github.com/haploid/flail"
  :license {:name "GPL-3.0-or-later"
            :url "https://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot flail.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
