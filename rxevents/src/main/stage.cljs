(ns stage
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]
            [player :as py]))


(defn functions-for-loop
  [game-state]
  [;update-player!
   (partial py/render-player! game-state)])

(defn add-to-ticker!
  "Adds the required events to the ticker"
  [game-state en]
  (let [events (functions-for-loop game-state)]
    (doseq [e events]
      (eng/render-update-subscribe en e))))

(defn remove-from-ticker!
  "Adds the required events to the ticker"
  [game-state en]
  (let [events (functions-for-loop game-state)]
    (doseq [e events]
      (eng/render-update-unsubscribe en e))))

(defn new-main-stage!
  "Creates a main pixi stage"
  [game-state engine]
  (let [stage (gobj/new-stage)
        player (py/new-player)]
    (gobj/add-sprite! stage :bg00)
    (gobj/add-child! stage (:sprite player) :ship00)
    (swap! game-state assoc :player player)
    (add-to-ticker! game-state engine)
    stage))

(defn destroy-main-stage!
  "Destroys the main engine with its associated resouces"
  [game-state engine]
  (remove-from-ticker! game-state engine)
  (eng/destroy! engine))
