(ns sim-worker.sim
  (:require [sim-worker.api :as api]
            [clj-http.client :as client]
            [clojure.core.async :as a])
  (:import (java.util UUID)))

(def places ["Rua Manoel Vieira Ribeiro Filho"
             "Av. Elias Maluf, 2013"
             "Av. Elias Maluf, 2207"
             "R. Manoel Soares da Silva, 32"
             "Av. Américo, 1855"
             "Av. Ipanema, 3885"
             "Av. Américo, 3075"
             "Av. Américo, 4073 - Júlio de Mesquita Filho, Sorocaba"
             "Av. Américo, 1051 - Jardim Simus, Sorocaba"
             "Av. Barão de Tatuí, 836 - Jardim Vergueiro, Sorocaba"
             "R. Dr. Júlio M. Guimarães, 189 - Parque Campolim, Sorocaba"
             "R. Cel. Nogueira Padilha, 612 - Vila Hortência, Sorocaba"
             "R. Francisco Moron Fernandes, 216 - Parque Campolim, Sorocaba"
             "R. Dr. Júlio M. Guimarães, 189 - Parque Campolim, Sorocaba"
             "R. Cel. Nogueira Padilha, 612 - Vila Hortência, Sorocaba"
             "R. Albertina Nascimento, 132 - Centro, Votorantim"
             "Rua Eudmira Almeida N. Rinaldo, 77 - Vila Nova Sorocaba, Sorocaba"
             "Rua Luiz Gabriotti, 213 - Jardim Wanel Ville II, Sorocaba"
             "R. José de Andrade, 200 - Jardim Ouro Fino, Sorocaba"
             "Av. Riusaku Kanizawa, 485 - Jardim Itapua, Sorocaba"
             "Estrada Ipatinga - s/n Faz Bom Retiro, Sorocaba"
             "Rua Eudmira Almeida N. Rinaldo, 77 - Vila Nova Sorocaba"
             "R. Manoel Lourenço Rodrigues, 591 - Vila Barao, Sorocaba"
             "R. Vítor Carone, 70 - Jardim das Flores, Sorocaba"
             "Av. Américo, 1081 - Jardim Simus, Sorocaba"
             "Av. Rio Claro, 350 - Vila Nova Sorocaba, Sorocaba"
             "R. Mário Soave, 559 - Jardim Arco Iris, Sorocaba"
             "Av. Salvador Milego, 400 - Jardim Vera Cruz, Sorocaba"
             "R. Jerônimo da Veiga, 83 - Jardim Ana Maria, Sorocaba"
             "Av. Independência, 210 - Éden, Sorocaba"
             "Edifício Madri - Avenida Rudolf Dafferner, Bloco 2, 400 - Boa Vista, Sorocaba"
             "Av. Gen. Osório, 35 - Vila Trujillo, Sorocaba"
             "Av. Dr. Eugênio Salerno, 100/140 - Centro, Sorocaba"
             "R. Dra. Ursulina Lopes Tôrres, 123 - Vergueiro, Sorocaba"])

(defn rand
  "Rand a place"
  []
  (let [c (count places)]
    (nth places (rand-int (dec c)))))

(defn uuid
  "Get a new UUID"
  []
  (str (UUID/randomUUID)))

(defn build-sesion []
  (let [id (uuid)]
    {:brand (str "sim-" (subs id 0 8))
     :model (str "0001-" (subs id 9 13))
     :hid   id}))

(defn run []
  (let [session (api/get-session (build-sesion))
        steps (-> (api/call-it (rand) (rand)) :routes first :legs first :steps)]
    (doseq [step steps]
      (api/post-track {:session-id session
                       :lat        (-> step :start_location :lat)
                       :long       (-> step :start_location :lng)
                       :vel        30
                       :gas-lvl    20})
      (Thread/sleep (* (-> step :duration :value) 1000))
      (api/post-track {:session-id session
                       :lat        (-> step :end_location :lat)
                       :long       (-> step :end_location :lng)
                       :vel        30
                       :gas-lvl    20}))))
