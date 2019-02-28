(ns enemy
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]))

(defn new-enemy
  "Creates a new enemy data"
  [key idx x y]
  {:pos {:x x :y y}
   :speed 3
   :direction 1
   :idx idx
   :id key
   :sprite (gobj/new-sprite :enemy00)
   :state :alive})

(defn new-enemy-group
  "Creates n enemies"
  [n base-key margin-x margin-y spacing-x]
  (into [] (for [i (range n)
                 :let [x (+ margin-x (* i spacing-x))
                       key (keyword (str base-key i))]]
             (new-enemy key i x margin-y))))
 
(defn render-enemy!
  "Updates the sprite with the enemy data"
  [game-state]
  (let [stage @(eng/get-stage (:engine @game-state))]
    (doseq [{sprite :sprite pos :pos state :state id :id} (:enemy @game-state)
            :let [in-stage? (gobj/get-child stage id)]]
      (case state
        :alive (do
                 (when (not in-stage?)
                   (gobj/add-child! stage sprite id)) 
                 (gobj/translate! sprite pos))
        :dead  (when in-stage?
                 (gobj/remove-child! stage id))))))

(defn update-an-enemy!
  "Updates enemy position. If hits a wall, the direction is reversed"
  [enemy]
  (let [{{x :x y :y} :pos
         state :state
         direction :direction
         speed :speed} enemy
        maybe-x (+ (* direction speed) x)
        out? (not (eng/intersect? maybe-x {:o 0 :length 488}))
        new-x (if out? x maybe-x)
        new-y (if out? (+ y speed) y)
        new-dir (if out? (* -1 direction) direction)]
     (if (= state :alive)
       (merge enemy  {:pos {:x new-x
                            :y new-y}
                      :direction new-dir})
       nil)))

(defn update-enemy!
  [game-state]
  (doseq [e (map update-an-enemy! (:enemy @game-state))
          :when e]
    (swap! game-state assoc-in [:enemy (:idx e)] e)))