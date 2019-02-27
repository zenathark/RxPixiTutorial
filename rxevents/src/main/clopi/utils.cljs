(ns clopi.utils
  (:require [oops.core :refer [oget]]))


(defn ??? [] (throw (js/Error "Not Implemented")))

(def log (oget js/window "console" "log"))
