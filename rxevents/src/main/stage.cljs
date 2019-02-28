(ns stage
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]
            [player :as py]
            [bullet :as bl]
            [enemy :as alien]))


(defn functions-for-loop
  [game-state]
  [;update-player!
   (partial py/render-player! game-state)
   (partial alien/update-enemy! game-state)
   (partial alien/render-enemy! game-state)
   (partial bl/update-bullet! game-state)
   (partial bl/render-bullet! game-state)
   (partial bl/check-for-collision! game-state)]
)

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
        player (py/new-player)
        bullet (bl/create-bullet)
        enemy (alien/new-enemy-group 5 "alien" 20 20 30)]
    (gobj/add-sprite! stage :bg00)
    (gobj/add-child! stage (:sprite player) :ship00)
    (swap! game-state assoc :player player)
    (swap! game-state assoc :enemy enemy)
    (swap! game-state assoc-in [:player :bullets] bullet)
    (add-to-ticker! game-state engine)
    stage))

(defn destroy-main-stage!
  "Destroys the main engine with its associated resouces"
  [game-state engine]
  (remove-from-ticker! game-state engine)
  (eng/destroy! engine))
