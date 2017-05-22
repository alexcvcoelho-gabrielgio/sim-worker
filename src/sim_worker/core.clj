(ns sim-worker.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.async :as a :refer [go thread]]
            [clj-http.client :as client]
            [clojure.data.json :as json])
  (:import (java.util UUID)))

(def session-url "http://localhost:3000/api/session")
(def track-url "http://gabrielgio.com.br:3001/api/track")

(defn uuid
  "Get a new UUID"
  []
  (str (UUID/randomUUID)))

(defn post-session [body]
  (client/post session-url {:form-params  body
                            :content-type :json}))

(defn call-api
  "Call tracker api to register sim's location"
  [{:keys [loc vel gas-lvl session-id hid]}]
  (client/post))

(defn parse-json
  "Reads information from a json file and parse it"
  []
  )

(defn get-session
  "Starts a session from API"
  [{:keys [brand model hid]}]
  (let [res (post-session {:brand brand
                                :model model
                                :hd-id hid})]
    (:session-id (json/read-str (:body res) :key-fn keyword))))

(defn run-sim
  "Runs simulation"
  [sims session hid]
  (doseq [sim sims]
    (call-api (assoc sim :session-id session :hid hid))
    (Thread/sleep (:dalay sim))))

(defn run
  "Gathers informations, start simulation
  and start consuming api."
  []
  (let [hid (uuid)
        s (get-session (assoc {} :brand "sim 0001" :model "x01" :hid hid))]
    (println s)
    (run-sim (parse-json) s hid)))

(defn start-threads
  "Starts all threads"
  [n]
  (loop [v n]
    (if (> v 1)
      (do
        (go (run))
        (recur (dec v))))))

(defn -main
  "Start simulation"
  [& args]
  (let [{:keys [options]} (parse-opts args [["-n" "--n NUMBER" :parse-fn #(Integer. %)]])]
    (start-threads (:n options))))
