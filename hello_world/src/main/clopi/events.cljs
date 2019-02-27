(ns clopi.events
  (:require [oops.core :refer [oget ocall oset!]]
            [clopi.utils :refer [??? log]]
            [clojure.core.async :as rx]))

(defn new-observable
  "Creates a new multicast observable for a given event over a diven DOM element.
   It is intended for Reacive Programming using the Clojure.Async library.
  + Args:
    - dom-ele DOM object selector
    - event-string Event string ID on js notation
  + Returns:
    A mult channel.
  "
  [dom-ele event-string]
  (let [out-chan (rx/chan)]
    (letfn [(update-chan [event] (rx/go (rx/>! out-chan event)))]
      (ocall dom-ele "addEventListener" event-string update-chan))
    (rx/mult out-chan)))

(defn new-kbdown-observable
  "Creates a new multicast observable for keydown events.
   If no DOM element is given, the observable is attached to js/document 
   See `new-observable`"
  ([] (new-kbdown-observable js/document))
  ([dom-ele]
   (new-observable dom-ele "keydown")))

(defn new-kbup-observable
  "Creates a new multicast observable for keyup events.
   If no DOM element is given, the observable is attached to js/document 
   See `new-observable`"
  ([] (new-kbup-observable js/document))
  ([dom-ele]
   (new-observable dom-ele "keyup")))

(defn new-kbpressed-observable
  "Creates a new multicast observable for keypressed events.
   If no DOM element is given, the observable is attached to js/document 
   See `new-observable`"
  ([] (new-kbpressed-observable js/document))
  ([dom-ele]
   (new-observable dom-ele "keypressed")))

(defn new-mousemv-observable
  "Creates a new multicast observable for mouse move events.
   If no DOM element is given, the observable is attached to js/document 
   See `new-observable`"
  ([] (new-mousemv-observable js/document))
  ([dom-ele]
   (new-observable dom-ele "mousemove")))

(defn new-mousedown-observable
  "Creates a new multicast observable for mouse down events.
   If no DOM element is given, the observable is attached to js/document 
   See `new-observable`"
  ([] (new-mousedown-observable js/document))
  ([dom-ele]
   (new-observable dom-ele "mousedown")))

(defn new-mouseup-observable
  "Creates a new multicast observable for mouse down events.
   If no DOM element is given, the observable is attached to js/document 
   See `new-observable`"
  ([] (new-mousemv-observable js/document))
  ([dom-ele]
   (new-observable dom-ele "mouseup")))

(defn add-observer
  "Creates a new observer using the given multi chan and a handler with an 
   optional transducer.
  + Args:
    - mult-chan A multi channel (observable)
    - handler A function with one argument. The argument will carry on the 
      message.
    - xform An optional transducer.
  + Returns:
    A new mult-chan that is an observable of the new event"
  ([mult-chan handler]
   (add-observer mult-chan handler nil))
  ([mult-chan handler xform]
   (let [out-chan (rx/chan 1 xform)
         out-tap  (rx/tap mult-chan out-chan)]
     (rx/go-loop []
       (when-let [e (rx/<! out-tap)]
         (handler e)
         (recur)))
     out-tap)))

(defn remove-observer
  "Stops an async observer"
  [mult-chan]
  (rx/close! mult-chan))

(defn chain-observer
  "Creates a new observer as add observer but it doesn't start a new async 
   listener. Useful for chaining functional operations on channels."
  [mult-chan xform]
  (let [out-chan (rx/chan xform)
        out-tap (rx/tap mult-chan out-chan)]
    out-tap))

;;; TODO
; (defn combine-observers
;   "Creates a new observer from a combination of two or more observers.
;   + Args:
;      mult-chan-list. A list of multi channels with is xform.
;   + Returns:
;     A new mult-chan from the combination."
;   [mult-chan-list])