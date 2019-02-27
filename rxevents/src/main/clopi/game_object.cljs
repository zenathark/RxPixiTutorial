(ns clopi.game-object
  (:require [oops.core :refer [oget oset! ocall oget+]]
            [goog.object :as g]
            [clopi.utils :refer [log]]))

(declare from-pixi)

(defn new-container
  [key]
  (let [container (new js/PIXI.Container)]
    (oset! container "name" (name key))
    (from-pixi container)))

(defn new-stage
  []
  (from-pixi (new js/PIXI.Container)))

(defn new-sprite
  ([texture-key] (new-sprite texture-key (name texture-key)))
  ([texture-key key]
   (let [sprite (new js/PIXI.Sprite (oget+ js/PIXI.loader
                                           (str "resources."
                                                (name texture-key)
                                                ".texture")))]
     (oset! sprite "name" key)
     (from-pixi sprite))))

(defn new-tiled-sprite
  [texture-key x y width height]
  (let [sprite (new js/PIXI.Sprite (oset! (g/get js/PIXI.utils.TextureCache
                                                 (name texture-key))
                                          "frame"
                                          (new js/PIXI.Rectangle
                                               x
                                               y
                                               width
                                               height)))]
    (oset! sprite "name" (name texture-key))
    (from-pixi sprite)))

(defn new-text
  [text key style]
  (let [ptext (new js/PIXI.Text text (clj->js style))]
    (oset! ptext "name" (name key))
    (from-pixi ptext)))

(defn new-collision-box
  [x y width height]
  {:x x
   :y y
   :width width
   :height height})

(defn stretch-collision-box
  "Creates a collision box using the texture size."
  [sprite]
  )

(defn pixi-remove-child
  ([pixi-container sprite]
   (when sprite
     (ocall pixi-container "removeChild" sprite))))

(defprotocol PGameObject
  (raw 
    [t]
    "Returns the js-object baking this instance")
  (add-sprite! 
    [t texture-key]
    "Adds a new sprite to this container using an individual texture, the 
   texture's key will become the sprite's key also.")
  (get-child
    [t key]
    "Returns the child with the given key")
  (add-tiled-sprite! 
    [t texture-key x y width height]
    "Adds a new sprite using a tiled texture. The sprite's key will be the same
   as the texture's one")
  (add-text! 
    [t text key]
    [t text key style]
    "Adds a new text object on this container with an optional style argument")
  (add-child!
   [t obj key]
   "Adds a new child to this container")
  (remove-child! 
    [t child-key]
    "Removes a child from this container using its key as identifier")
  (add-collisionbox
    [t aabb]
    [t aabb debug]
    [t x y width height]
    [t x y width height debug]
    "Adds a simple AABB to this object. When an AABB is present, it will be
    added to world's physic engine if one is present. It can be build by
    providing an AABB or the parameters of the AABB x y width and height. This
    positions are relative to this object. If debug is set to true, an aditional
    visual guide to the bounding box will be drawn on the screen.")
  (stretch-collision
    [t]
    "Creates an AABB using the information of the texture of this object")
  (translate!
   [t p]
   [t x y]
   "Translate this object to the x y relative position")
  (rotate!
    [t tdeta-x theta-y]
    "A rotation of with two degrees of freedom")
  (get-cb
    [t]
    "returns the collision box with the global transform applied")
  (calc-cb
    [t position]
    "returns a collision box with the given transform applied")
  (pos
    [t]
    "returns the global position of thi object"))

(declare from-pixi)

(defrecord GameObject [pixi-obj
                       cb
                       childs]
  PGameObject
  (raw [t] (:pixi-obj t))
  (add-sprite! 
    [t texture-key]
    (let [sprite (new-sprite texture-key)]
      (ocall (:pixi-obj t) "addChild" (raw sprite))
      (swap! (:childs t) assoc texture-key sprite)
      sprite))
  (add-tiled-sprite!
    [t texture-key x y width height]
    (let [sprite (new-tiled-sprite texture-key x y width height)]
      (ocall (raw t) "addChild" (raw sprite))
      (swap! (:childs t) assoc texture-key sprite)
      sprite))
  (add-text!
    [t text key style]
    (let [ptext (new-text text key style)]
      (ocall (raw t) "addChild" (raw ptext))
      (swap! (:childs t) assoc key ptext)
      ptext))
  (get-child
    [t key]
    (key @(:childs t)))
  (add-text!
    [t text key]
    (add-text! t text key nil))
  (add-child!
    [t child key]
    (ocall (raw t) "addChild" (raw child))
    (swap! (:childs t) assoc key child)
    nil)
  (remove-child!
   [t child-key]
   (pixi-remove-child (raw t) (raw (child-key @(:childs t))))
   nil)
  (translate!
    [t p]
    (translate! t (:x p) (:y p)))
  (translate!
   [t x y]
   (oset! (:pixi-obj t) "x" x)
   (oset! (:pixi-obj t) "y" y))
  (pos
    [t]
    [(oget (:pixi-obj t) "x") (oget (:pixi-obj t) "y")])
  (add-collisionbox
    [t aabb]
    (reset! (:cb t) aabb))
  (stretch-collision
    [t]
    (add-collisionbox t (new-collision-box 0
                                           0
                                           (oget (raw t) "width")
                                           (oget (raw t) "height"))))
  (get-cb
    [t]
    (let [pcb @(:cb t)
          [x y] (pos t)]
      (new-collision-box (+ x (:x pcb))
                         (+ y (:y pcb))
                         (:width pcb)
                         (:height pcb))))
  (calc-cb
    [t position]
    (let [pcb @(:cb t)
          {x :x y :y} position]
      (new-collision-box (+ x (:x pcb))
                         (+ y (:y pcb))
                         (:width pcb)
                         (:height pcb))))
  )

(defn from-pixi
  [pixi-obj]
  (map->GameObject {:pixi-obj pixi-obj
                    :cb (atom nil)
                    :childs (atom {})}))