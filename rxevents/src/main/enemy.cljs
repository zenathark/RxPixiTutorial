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
   :direction 1
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
  (let [{{x :x y :y} :pos
         state :state
         direction :direction
         speed :speed} (get @game-state :enemy)
        maybe-x (+ (* direction speed) x)
        out? (not (eng/intersect? maybe-x {:o 0 :length 488}))
        new-x (if out? x maybe-x)
        new-y (if out? (+ y speed) y)
        new-dir (if out? (* -1 direction) direction)]
     (when (= state :alive)
      (swap! game-state assoc-in [:enemy] (merge (:enemy @game-state)
                                                 {:pos {:x new-x
                                                        :y new-y}
                                                  :direction new-dir})))))