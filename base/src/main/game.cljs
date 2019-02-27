(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]))


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
  nil)