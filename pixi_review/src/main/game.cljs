(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]
            [pixi.js :as pixi]))


(defn destroy
  "Removes all transtient data between hot reloads"
  []
  nil)

(defn start
  "Run when the page is first time loaded, creates initial environment"
  []
  nil)

(defn init
  "Initializes the game, intented to use when the page is reloaded"
  []
  (if (ocall pixi/utils "isWebGLSupported")
    (log "WebGL")
    (log "Canvas")))