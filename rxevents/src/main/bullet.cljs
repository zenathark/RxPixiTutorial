(ns bullet
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]))

(defn create-bullet
  "Creates a new bullet game object"
  []
  {:pos {:x 0 :y 0}
   :speed 3
   :sprite (gobj/new-sprite :bullet00)
   :state :outscreen})

(defn render-bullet!
  "Updates the bullet sprite"
  [game-state]
  (let [{sprite :sprite pos :pos state :state} (get-in @game-state [:player :bullets])]
    (gobj/translate! sprite pos)))

(defn update-bullet!
  "Update bullet's position and state"
  [game-state]
  (let [{{x :x y :y} :pos
         state :state
         speed :speed} (get-in @game-state [:player :bullets])]
    nil))