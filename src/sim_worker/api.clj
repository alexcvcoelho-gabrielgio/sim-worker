(ns sim-worker.api
  (:require [clj-http.client :as client]
            [clojure.data.json :as json])
  (:import (clojure.lang PersistentArrayMap)))

(def google-api-url "https://maps.googleapis.com/maps/api/directions/json")
(def session-url "http://gabrielgio.com.br:3000/api/session")
(def track-url "http://gabrielgio.com.br:3000/api/track")
(def google-api-key "{API-KEY}")

(defn query-url
  "Transform an hash-map into a query url string"
  [^PersistentArrayMap m]
  (if (empty? m)
    ""
    (loop [s "?"
           col m]
      (let [v (first col)]
        (if (empty? col)
          s
          (recur (str s "&" (name (first v)) "=" (last v))
                 (dissoc col (first v))))))))

(defn call-it
  "Call google api to get route"
  [^String or ^String des]
  (json/read-str (:body (client/get (str google-api-url
                                         (query-url {:origin      or
                                                     :destination des
                                                     :key         google-api-key}))))
                 :key-fn keyword))

(defn post-session
  "Post sesion a session info"
  [body]
  (client/post session-url {:form-params  body
                            :content-type :json}))

(defn get-session
  "Starts a session from API"
  [{:keys [brand model hid]}]
  (let [res (post-session {:brand brand
                           :model model
                           :hd-id hid})]
    (:session-id (json/read-str (:body res) :key-fn keyword))))

(defn post-track [session]
  "Post track info into track service"
  (client/post track-url {:form-params  session
                          :content-type :json}))
