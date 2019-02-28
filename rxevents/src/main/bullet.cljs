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
   :speed 10
   :sprite (gobj/new-sprite :bullet00)
   :state :outscreen})

(defn render-bullet!
  "Updates the bullet sprite"
  [game-state]
  (let [{sprite :sprite pos :pos state :state} (get-in @game-state [:player :bullets])
        stage @(eng/get-stage (:engine @game-state))
        in-stage? (gobj/get-child stage :bullet00)]

    (case state
      :outscreen (when in-stage? 
                   (gobj/remove-child! stage :bullet00))
      :onscreen (do
                  (when (not in-stage?)
                    (gobj/add-child! stage sprite :bullet00))
                  (gobj/translate! sprite pos))
      nil)))

(defn update-bullet!
  "Update bullet's position and state"
  [game-state]
  (let [{{x :x y :y} :pos
         state :state
         speed :speed} (get-in @game-state [:player :bullets])
        new-y (- y speed)]
    (case state
      :onscreen (if (neg? new-y)
                  (swap! game-state assoc-in [:player :bullets :state] :outscreen)
                  (swap! game-state assoc-in [:player :bullets :pos :y] new-y))
      nil)))