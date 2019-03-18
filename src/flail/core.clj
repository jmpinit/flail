(ns flail.core
  (:require [flail.kuka-var-proxy-robot-sync :as rob-sync])
  (:gen-class))

(defn -main
  "Generate the KRL for the sync script to run on the robot"
  [& args]
     (println rob-sync/sync-script))

