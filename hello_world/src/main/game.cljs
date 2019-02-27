(ns game
  (:require [clopi.utils :refer [log ???]]
            [oops.core   :refer [oget ocall oset!]]))

;;; A global state, defined only when the page is first loaded
(defonce message (atom ""))

(defn destroy
  "Removes all transtient data between hot reloads"
  []
  nil)

(defn start
  "Initializes the game, intented to use when the page is reloaded."
  
  []
  (let [dom-element (ocall js/document "getElementById" "app")]
    (reset! message "He!!")
    (oset! dom-element "innerHTML" @message)))

(defn init
  "Run when the page is first time loaded, creates initial environment It also 
  calls `start`"
  []
  (reset! message "Hello, World!")
  (start))
