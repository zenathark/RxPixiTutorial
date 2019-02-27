(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [pixi.js :as pixi]))


(defonce app (atom nil))
(defonce game-state (atom nil))

(defn init-game-state
  "Initialize the state of the game"
  []
  (reset! game-state {:player {:pos {:x 20 :y 20}}}))

(defn destroy
  "Removes all transtient data between hot reloads"
  []
  nil)

(defn start
  "Initializes the game, intented to use when the page is reloaded"
  [])

(defn setup
  []
  (let [sprite (new pixi/Sprite (oget pixi/loader "resources.ship00.texture"))]
    (init-game-state)
    (ocall @app "stage.addChild" sprite)
    (oset! sprite "x" (get-in @game-state [:player :pos :x]))
    (oset! sprite "y" (get-in @game-state [:player :pos :y]))))

(defn init
  "Run when the page is first time loaded, creates initial environment"
  []
  (let [new-app (new pixi/Application (clj->js {:width 256
                                                :height 256
                                                :antialias true
                                                :transparent false
                                                :resolution 1}))]
    (reset! app new-app)
    (log "Appending a pixi stage")
    (ocall js/document "body.appendChild" (oget @app "view"))
    (log "Load resources")
    (ocall pixi/loader "add" (clj->js {:name :ship00
                                       :url "assets/2.png"}))
    (ocall pixi/loader "load" setup)))