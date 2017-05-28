(ns sim-worker.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.async :as a :refer [go thread]]
            [clj-http.client :as client]
            [clojure.data.json :as json])
  (:import (java.util UUID)))

(def session-url "http://gabrielgio.com.br:3000/api/session")
(def track-url "http://gabrielgio.com.br:3001/api/track")

(defn uuid
  "Get a new UUID"
  []
  (str (UUID/randomUUID)))

(defn post-session
  "Start a session"
  [body]
  (client/post session-url {:form-params  body
                            :content-type :json}))

(defn call-api
  "Call tracker api to register sim's location"
  [item]
  (client/post track-url {:form-params  (dissoc item :delay)
                          :content-type :json}))

(defn get-randon-info []
  {:lat (rand-int 30)
   :long (rand-int 30)
   :vel (rand-int 100)
   :gas-lvl (rand-int 30)
   :delay 1})

(defn parse-json
  "Reads information from a json file and parse it"
  [n]
  (loop [x 0 ar []]
    (if (< x n)
      (recur (inc x) (conj ar (get-randon-info)))
      ar)))

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
    (call-api (assoc sim :session-id session ))))

(defn run
  "Gathers informations, start simulation
  and start consuming api."
  []
  (let [hid (uuid)
        s (get-session (assoc {} :brand "sim 0001" :model "x01" :hid hid))]
    (run-sim (parse-json 100) s hid)))

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
