(ns sim-worker.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.async :as a :refer [go thread]]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [sim-worker.sim :as sim]))

(defn start-threads
  "Starts all threads"
  [n]
  (loop [v n]
    (if (> v 1)
      (do
        (thread (sim/run))
        (recur (dec v))))))

(defn -main
  "Start simulation"
  [& args]
  (let [{:keys [options]} (parse-opts args [["-n" "--n NUMBER" :parse-fn #(Integer. %)]])]
    (doall (start-threads (:n options)))))
