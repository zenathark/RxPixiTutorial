(ns clopi.pixi-engine
  (:require 
   [clopi.utils :refer [log ???]]
   [oops.core :refer [oget oset! ocall]]
   [clopi.game-object :as gobj]
   [clopi.resource-manager :as l]
   [clojure.core.async :as rx]
   [clopi.events :as ev]))

(defprotocol GraphicsEngine
  (get-stage [t]
    "Returns the native stage object")
  (raw-engine [t]
    "Returns the js object reference to this engine if one")
  (raw-stage [t]
    "Returns the js object reference of the main container if one")
  (get-view [t]
    "Returns the HTML representantion of the main container")
  (destroy! [t]
    "Destroys the internal js object. If the engine is attached to the 
             DOM, it will also be dettached using `clopi.pixi-engine/detach!")
  (attach! [t]
    "Attach the engine to the DOM")
  (detach! [t]
    "Detach the engine to the DOM")
  (set-stage! 
    [t stage]
    "Sets a container as the main stage.")
  (game-loop!
    [t delta]
    "Invokes all lifetime events on a loop")
  (start-game-loop!
    [t]
    "Starts the game loop")
  (stop-game-loop!
    [t]
    "Stops the game looop")
  (render-update-subscribe
    [t handler]
    "Adds a handler to the fixed update event. Returns a multi chan")
  (render-update-unsubscribe
    [t ref]
    "Removes the ref to the fixed update event."))

  
(extend-protocol GraphicsEngine
  nil
  (get-stage [t] nil)
  (raw-engine [t] nil)
  (raw-stage [t] nil)
  (set-stage! [t stage] nil)
  (get-view [t] nil)
  (destroy! [t] nil)
  (attach! [t] nil)
  (detach! [t] nil))

(defrecord PixiEngine [application
                       html-anchor
                       stage
                       stage-gen
                       loader
                       render-update-chan
                       listeners
                       aabb-tree
                       -render-update-mult
                       -rx-render-update]
  GraphicsEngine
  (raw-engine [t]
    application)
  (get-stage [t]
    (:stage t))
  (raw-stage [t]
    (oget (:application t) "stage"))
  (set-stage! 
   [t stage-gen]
   (reset! (:stage-gen t) stage-gen)
   nil)
  (get-view [t]
    (oget (:application t) "view"))
  (destroy! [t]
    (detach! t)
    (ocall (:application t) "destroy")
    nil)
  (attach! [t]
    (let [dom-element (oget js/document "body")]
      (ocall dom-element "appendChild" (get-view t))
      nil))
  (detach! [t]
    (let [dom-element (oget js/document "body")]
      (ocall dom-element "removeChild" (get-view t))
      nil))
  (game-loop!
    [t delta]
    nil)
  (start-game-loop!
   [t]
    (let [stage (@(:stage-gen t) t)]
     (oset! (:application t) "stage" (:pixi-obj stage))
     (reset! (:stage t) stage)
     (ocall (raw-engine t) "ticker.add" (partial game-loop! t))
     (ocall (raw-engine t) "ticker.start")))
  (stop-game-loop!
    [t]
    (ocall (raw-engine t) "ticker.remove" (partial game-loop! t))
    (ocall (raw-engine t) "ticker.stop"))
  (render-update-subscribe
    [t handler]
    (let [out-chan (rx/chan)
          out-mchan (rx/tap (:-render-update-mult t) out-chan)]
      (rx/go-loop []
        (when-let [e (rx/<! out-mchan)]
          (handler e)
          (recur)))
      out-mchan))
  (render-update-unsubscribe
   [t ref]
   (rx/untap (:-render-update-mult t) ref))
  l/ResourceManager
  (add-asset!
    [t key path cb]
    (l/add-asset! (:loader t) key path cb))
  (add-asset!
    [t key path]
    (l/add-asset! (:loader t) key path nil))
  (add-assets!
    [t assets cb]
    (l/add-assets! (:loader t) assets cb))
  (add-assets!
    [t assets]
    (l/add-assets! (:loader t) assets nil))
  (load!
    [t base-url cb]
    (l/load! (:loader t) base-url cb))
  (load!
    [t base-url]
    (l/load! (:loader t) base-url nil))
  (load!
    [t]
    (l/load! (:loader t) "" nil)))

(defn new-pixi-engine
  [config]
  (let [app      (new js/PIXI.Application (clj->js config))
        stage    (oget app "stage")
        update-name (gensym "fixedUpdate")
        fixed-update-channel (rx/chan)
        rx-fixed-update-chan (fn [t] (rx/go 
                                      (rx/>! fixed-update-channel 
                                             {:deltaTime (oget app "ticker.deltaTime")})))
        pixi-app (map->PixiEngine
                  {:application app
                   :stage (atom (gobj/from-pixi stage))
                   :stage-gen (atom nil)
                   :html-anchor nil
                   :loader l/pixi-resource-manager
                   :render-update-channel fixed-update-channel
                   :events {:kbdown (ev/new-kbdown-observable)
                            :kbup   (ev/new-kbup-observable)
                            :kbpressed (ev/new-kbpressed-observable)
                            :mousemv (ev/new-mousemv-observable)
                            :mousedown (ev/new-mousedown-observable)
                            :mouseup   (ev/new-mouseup-observable)}
                   :-render-update-mult (rx/mult fixed-update-channel)
                   :-rx-render--update rx-fixed-update-chan
                   :aabb-tree nil})
        ]
    (ocall app "ticker.add" rx-fixed-update-chan)
    pixi-app))

;; TODO Collision Engine at 50 fps

(defn absolute-aabb
  "Calculates the AABB axis values from a relative position to its world's
  values."
  [] 
  ???)

(defn aabb-tree
  "Creates a simple aabb tree."
  []
  ???)

(defn remove-node
  "Removes a leaf node from an AABB tree."
  []
  ???)

(defn aabb-weight
  "Calculates the weight of an AABB node"
  []
  ???)

(defn joint-aabb
  "Takes two aabb and calculates the weight of the resultant AABB"
  []
  ???)

(defn push-node
  "Pushes a new AABB node into an AABB tree."
  []
  ???)

(defn collider-candidates
  "Returns a list of possible colliders with a gived object"
  []
  ???)

(defn check-collision
  "Filters a colliders candidates list and returns a new list with real
  colliders using AABB"
  []
  ???)

(defn rebuild-aabb-tree
  "Used on fixed update. Rebuilds the aabb tree using the updated information
  of each element of the given container."
  []
  ???)

(defn intersect?
  "Returns true if a given line intersects with a given point in a 1D Plane"
  [point line]
  (and (>= point (:o line))
       (<= point (+ (:o line) (:length line)))))


(defn overlap?
  "Returns true if the two AABB provided overlap eachother"
  [ob1 ob2]
  (let [a (gobj/get-cb ob1)
        b (gobj/get-cb ob2)]
    (and (or (intersect? (:x a) {:o (:x b) :length (:width b)})
             (intersect? (+ (:x a) (:width a)) {:o (:x b) :length (:width b)}))
         (or (intersect? (:y a) {:o (:y b) :length (:height b)})
             (intersect? (+ (:y a) (:height a)) {:o (:y b) :length (:height b)})))))

(defn inside?
  "Returns true if the AABB ob1 is inside AABB ob2"
  [ob1 ob2]
  (let [a ob1
        b (gobj/get-cb ob2)]
    (and (and (intersect? (:x a) {:o (:x b) :length (:width b)})
              (intersect? (+ (:x a) (:width a)) {:o (:x b) :length (:width b)}))
         (and (intersect? (:y a) {:o (:y b) :length (:height b)})
              (intersect? (+ (:y a) (:height a)) {:o (:y b) :length (:height b)})))))

(defn stretch-cb
  [obj]
  ())


(defn not-overlap?
  "Returns true if the two AAB provided don't overlap"
  [a b]
  (not (overlap? a b)))