(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]
            [player :as py]
            [stage :as st]))


(defonce game-state (atom {:observers []}))

;;; -------- Begin Input Section -----------------------------------------

(defn input-player-movement
  [update-fn]
  (let [x (get-in @game-state [:player :pos :x])
        speed (get-in @game-state [:player :speed])
        maybe-x (update-fn x speed)
        new-x (if (eng/intersect? maybe-x {:o 0 :length 478}) maybe-x x)]
    (swap! game-state assoc-in [:player :pos :x] new-x)))

(defn a-input
  [e]
  (input-player-movement #(- %1 %2)))

(defn d-input
  [e]
  (input-player-movement #(+ %1 %2)))

(defn space-input
  [e]
  (let [{x :x y :y} (get-in @game-state [:player :pos])
        state (get-in @game-state [:player :bullets :state])]
    (case state
      :outscreen 
      (do
        (swap! game-state assoc-in [:player :bullets :pos] {:x (+ 14 x)
                                                            :y (- y 6)})
        (swap! game-state assoc-in [:player :bullets :state] :onscreen))
      nil)))

(defn create-keyboard-listener
  "Adds a new listener to the js document event"
  [engine]
  (let [ob1 (rx/add-observer (get-in engine [:events :kbdown])
                             a-input
                             (filter #(= "a" (oget % "key"))))
        ob2 (rx/add-observer (get-in engine [:events :kbdown])
                             d-input
                             (filter #(= "d" (oget % "key"))))
        ob3 (rx/add-observer (get-in engine [:events :kbdown])
                             space-input
                             (filter #(= " " (oget % "key"))))]
    (swap! game-state update :observers conj ob1 ob2 ob3)))

(defn destroy-listeners
  "Destroys all listeners from the engine"
  []
  (doseq [e (:observers @game-state)]
    (rx/remove-observer e))
  (swap! game-state assoc :observers []))

;;; -------- End Input Section -----------------------------------------

;;; -------- Beging Setup Section -----------------------------------

(defn new-engine!
  "Creates a new engine for rendering and sets it to the global state"
  [game-state]
  (swap! game-state assoc :engine (eng/new-pixi-engine {:width 512
                                                        :height 512
                                                        :antialias true
                                                        :transparent false
                                                        :resolution 1})))

(defn destroy
  "Removes all transtient data between hot reloads"
  []
  (eng/stop-game-loop! (:engine @game-state))
  (st/destroy-main-stage! game-state (:engine @game-state))
  (destroy-listeners))
 
(defn start
  "Initializes the game, intented to use when the page is reloaded"
  []
  (log "Creating engine")
  (new-engine! game-state)
  (log "Creating main stage")
  (eng/set-stage! (:engine @game-state) (partial st/new-main-stage! game-state (:engine @game-state)))
  (log "Appending a pixi stage")
  (eng/attach! (:engine @game-state))
  (eng/start-game-loop! (:engine @game-state))
  (create-keyboard-listener (:engine @game-state)))

(defn init
  "Run when the page is first time loaded, creates initial environment"
  []
  (log "Load resources")
  (res/add-assets! res/pixi-resource-manager [[:ship00 "2.png"]
                                              [:bg00 "5.png"]
                                              [:bullet00 "4.png"]
                                              [:enemy00 "10.png"]] "assets/")
  (res/load! res/pixi-resource-manager "assets/" start))

;;; -------- End Setup Section -----------------------------------