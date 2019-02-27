(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [pixi.js :as pixi]))


(defonce game-state (atom nil))

;;; -------- Begin Player Section -----------------------------------------

(defn new-player
  "Creates a new player data"
  []
  {:pos {:x 0 :y 0}
   :direction {:x 2 :y 1}
   :sprite (new pixi/Sprite
                (oget pixi/loader "resources.ship00.texture"))})

(defn render-player!
  "Updates the sprite with the player data"
  []
  (let [{sprite :sprite {x :x y :y} :pos} (:player @game-state)]
    (oset! sprite "x" x)
    (oset! sprite "y" y)))

(defn update-player!
  "Updates player position. If hits a wall, the direction is reversed"
  []
  (let [{{x :x y :y} :pos
         {dx :x dy :y} :direction} (:player @game-state)
        new-x (+ dx x)
        new-y (+ dy y)
        new-dx (if (or (neg? new-x) (> new-x 222)) (* -1 dx) dx)
        new-dy (if (or (neg? new-y) (> new-y 222)) (* -1 dy) dy)]
    (swap! game-state assoc :player (merge (:player @game-state) {:pos {:x (+ new-dx x)
                                                                        :y (+ new-dy y)}
                                                                  :direction {:x new-dx
                                                                              :y new-dy}}))))
;;; -------- End Player Section -------------------------------------

;;; -------- Beging Main Stage Section ------------------------------

(def functions-for-loop
  [update-player!
   render-player!])

(defn add-to-ticker!
  "Adds the required events to the ticker"
  []
  (let [eng (:engine @game-state)
        events functions-for-loop]
    (doseq [e events]
      (ocall eng "ticker.add" e))))

(defn remove-from-ticker!
  "Adds the required events to the ticker"
  []
  (let [eng (:engine @game-state)
        events functions-for-loop]
    (doseq [e events]
      (ocall eng "ticker.remove" e))))

(defn new-main-stage!
  "Creates a main pixi stage"
  []
  (let [new-app (new pixi/Application (clj->js {:width 256
                                                :height 256
                                                :antialias true
                                                :transparent false
                                                :resolution 1}))
        player (new-player)]
    (ocall new-app "stage.addChild" (:sprite player))
    (reset! game-state {:player player
                        :engine new-app})
    (add-to-ticker!)))

(defn destroy-main-stage!
  "Destroys the main engine with its associated resouces"
  []
  (ocall js/document "body.removeChild" (oget (:engine @game-state) "view"))
  (remove-from-ticker!))
;;; -------- End Main Stage Section ---------------------------------

;;; -------- Beging Setup Section -----------------------------------
(defn destroy
  "Removes all transtient data between hot reloads"
  []
  (destroy-main-stage!))

(defn start
  "Initializes the game, intented to use when the page is reloaded"
  []
  (new-main-stage!)
  (log "Appending a pixi stage")
  (ocall js/document "body.appendChild" (oget (:engine @game-state) "view")))

(defn init
  "Run when the page is first time loaded, creates initial environment"
  []
  (log "Load resources")
  (ocall pixi/loader "add" (clj->js {:name :ship00
                                     :url "assets/2.png"}))
  (ocall pixi/loader "load" start))

;;; -------- End Setup Section -----------------------------------