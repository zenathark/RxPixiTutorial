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
   :sprite (new pixi/Sprite
                (oget pixi/loader "resources.ship00.texture"))})

(defn render-player!
  "Updates the sprite with the player data"
  []
  (let [{sprite :sprite {x :x y :y} :pos} (:player @game-state)]
    (oset! sprite "x" x)
    (oset! sprite "y" y)))

(defn update-player!
  "Updates player data"
  []
  (let [{{x :x y :y} :pos} (:player @game-state)]
    (swap! game-state assoc-in [:player :pos] {:x (inc x) :y (inc y)})))
;;; -------- End Player Section -------------------------------------

;;; -------- Beging Main Stage Section ------------------------------

(defn add-to-ticker!
  "Adds the required events to the ticker"
  []
  (let [eng (:engine @game-state)
        events [update-player!
                render-player!]]
    (doseq [e events]
      (ocall eng "ticker.add" e))))

(defn new-main-stage!
  "Creates a main pixi stage"
  []
  (let [new-app (new pixi/Application (clj->js {:width 256
                                                :height 256
                                                :antialias true
                                                :transparent false
                                                :resolution 1}))
        player (new-player)]
    (log new-app)
    (ocall new-app "stage.addChild" (:sprite player))
    (reset! game-state {:player player
                        :engine new-app})
    (add-to-ticker!)))

(defn destroy-main-stage!
  "Destroys the main engine with its associated resouces"
  []
  (ocall js/document "body.removeChild" (oget (:engine @game-state) "view")))
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