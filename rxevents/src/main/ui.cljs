(ns ui
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]))

(defn new-score-text
  "Creates a new player data"
  []
  {:pos {:x 10 :y 10}
   :score 0
   :text (gobj/new-text "Score: 0" :score-ui {:fontFamily "Press Start 2P"
                                              :fontSize 16
                                              :fill "#ffffffff"})})

(defn render-score!
  "Updates the sprite with the player data"
  [game-state]
  (let [{score :score text :text} (:score-ui @game-state)]
    (oset! (gobj/raw text) "text" (str "Score: " score))))

(defn update-score!
  "Updates score ui"
  [game-state]
  (let [{score :score} (:player @game-state)]
    (swap! game-state assoc-in [:score-ui :score] (get-in @game-state [:player :score]))))