(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [pixi.js :as pixi]))


(defn destroy
  "Removes all transtient data between hot reloads"
  []
  nil)

(defn start
  "Initializes the game, intented to use when the page is reloaded"
  []
  nil)

(defn init
  "Run when the page is first time loaded, creates initial environment"
  []
  (let [app (new pixi/Application (clj->js {:width 256
                                            :height 256
                                            :antialias true
                                            :transparent false
                                            :resolution 1}))]
    (log "Appending a pixi stage")
    (ocall js/document "body.appendChild" (oget app "view"))))