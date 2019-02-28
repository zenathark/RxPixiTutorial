(ns enemy
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]))

(defn new-enemy
  "Creates a new enemy data"
  []
  {:pos {:x 230 :y 30}
   :speed 3
   :sprite (gobj/new-sprite :enemy00)
   :state :alive})

(defn render-enemy!
  "Updates the sprite with the enemy data"
  [game-state]
  (let [{sprite :sprite pos :pos state :state} (:enemy @game-state)
        stage @(eng/get-stage (:engine @game-state))
        in-stage? (gobj/get-child stage :enemy00)]
    (case state
      :alive (gobj/translate! sprite pos)
      :dead (when in-stage?
              (gobj/remove-child! stage :enemy00)))))

(defn update-enemy!
  "Updates enemy position. If hits a wall, the direction is reversed"
  [game-state]
  nil)