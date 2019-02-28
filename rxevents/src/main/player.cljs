(ns player
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]))

(defn new-player
  "Creates a new player data"
  []
  {:pos {:x 230 :y 470}
   :direction {:x 2 :y 1}
   :speed 3
   :sprite (gobj/new-sprite :ship00)})

(defn render-player!
  "Updates the sprite with the player data"
  [game-state]
  (let [{sprite :sprite pos :pos} (:player @game-state)]
    (gobj/translate! sprite pos)))

(defn update-player!
  "Updates player position. If hits a wall, the direction is reversed"
  [game-state]
  (let [{{x :x y :y} :pos
         {dx :x dy :y} :direction} (:player @game-state)
        new-x (+ dx x)
        new-y (+ dy y)
        new-dx (if (eng/intersect? new-x {:o 0 :length 222}) dx (* -1 dx))
        new-dy (if (eng/intersect? new-y {:o 0 :length 222}) dy (* -1 dy))]
    (swap! game-state assoc :player (merge (:player @game-state) {:pos {:x (+ new-dx x)
                                                                        :y (+ new-dy y)}
                                                                  :direction {:x new-dx
                                                                              :y new-dy}}))))