(defproject flail "0.1.0-SNAPSHOT"
  :description "Creative coding environment for KUKA industrial robot arms"
  :url "https://github.com/haploid/flail"
  :license {:name "GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.gnu.org/software/classpath/license.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot flail.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
