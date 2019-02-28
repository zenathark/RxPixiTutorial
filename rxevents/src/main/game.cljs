(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as rx]))


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

(defn create-keyboard-listener
  "Adds a new listener to the js document event"
  [engine]
  (let [ob1 (rx/add-observer (get-in engine [:events :kbdown])
                             a-input
                             (filter #(= "a" (oget % "key"))))
        ob2 (rx/add-observer (get-in engine [:events :kbdown])
                             d-input
                             (filter #(= "d" (oget % "key"))))]
    (swap! game-state update :observers conj ob1 ob2)))

(defn destroy-listeners
  "Destroys all listeners from the engine"
  []
  (doseq [e (:observers @game-state)]
    (rx/remove-observer e))
  (swap! game-state assoc :observers []))

;;; -------- End Input Section -----------------------------------------

;;; -------- Begin Player Section -----------------------------------------

(defn new-player
  "Creates a new player data"
  []
  {:pos {:x 230 :y 470}
   :direction {:x 2 :y 1}
   :speed 3
   :sprite (gobj/new-sprite :ship00)})

(defn render-player!
  "Updates the sprite with the player data"
  []
  (let [{sprite :sprite pos :pos} (:player @game-state)]
    (gobj/translate! sprite pos)))

(defn update-player!
  "Updates player position. If hits a wall, the direction is reversed"
  []
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
;;; -------- End Player Section -------------------------------------

;;; -------- Beging Main Stage Section ------------------------------

(def functions-for-loop
  [;update-player!
   render-player!])

(defn add-to-ticker!
  "Adds the required events to the ticker"
  [en]
  (let [events functions-for-loop]
    (doseq [e events]
      (eng/render-update-subscribe en e))))

(defn remove-from-ticker!
  "Adds the required events to the ticker"
  [en]
  (let [events functions-for-loop]
    (doseq [e events]
      (eng/render-update-unsubscribe en e))))

(defn new-main-stage!
  "Creates a main pixi stage"
  [engine]
  (let [stage (gobj/new-stage)
        player (new-player)]
    (gobj/add-sprite! stage :bg00)
    (gobj/add-child! stage (:sprite player) :ship00)
    (swap! game-state assoc :player player)
    (add-to-ticker! engine)
    stage))

(defn destroy-main-stage!
  "Destroys the main engine with its associated resouces"
  [engine]
  (remove-from-ticker! engine)
  (eng/destroy! engine))

(defn new-engine!
  "Creates a new engine for rendering and sets it to the global state"
  []
  (swap! game-state assoc :engine (eng/new-pixi-engine {:width 512
                                                        :height 512
                                                        :antialias true
                                                        :transparent false
                                                        :resolution 1})))
;;; -------- End Main Stage Section ---------------------------------

;;; -------- Beging Setup Section -----------------------------------
(defn destroy
  "Removes all transtient data between hot reloads"
  []
  (eng/stop-game-loop! (:engine @game-state))
  (destroy-main-stage! (:engine @game-state))
  (destroy-listeners))

(defn start
  "Initializes the game, intented to use when the page is reloaded"
  []
  (log "Creating engine")
  (new-engine!)
  (log "Creating main stage")
  (eng/set-stage! (:engine @game-state) new-main-stage!)
  (log "Appending a pixi stage")
  (eng/attach! (:engine @game-state))
  (eng/start-game-loop! (:engine @game-state))
  (create-keyboard-listener (:engine @game-state)))

(defn init
  "Run when the page is first time loaded, creates initial environment"
  []
  (log "Load resources")
  (res/add-assets! res/pixi-resource-manager [[:ship00 "2.png"]
                                              [:bg00 "5.png"]] "assets/")
  (res/load! res/pixi-resource-manager "assets/" start))

;;; -------- End Setup Section -----------------------------------