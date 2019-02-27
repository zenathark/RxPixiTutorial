(ns game
  (:require [clopi.utils :refer [log]]
            [oops.core :refer [oget ocall oset!]]
            [clopi.core :as px]
            [clopi.pixi-engine :as eng]
            [clopi.game-object :as gobj]
            [clopi.resource-manager :as res]
            [clopi.events :as ev]))

(def loader res/pixi-resource-manager)

(defonce engine (atom nil))

(defonce game-state (atom nil))

(defonce resources-loaded false)

(defn reset-game-state!
  []
  (reset! game-state {:engine {:render nil
                               :observers []}
                      :player {:pos {:x 305 :y 440}
                               :speed 5
                               :state :alive
                               :sprite nil
                               :bullets []}
                      :enemies-global-pos {:x 0 :y 0 :dir 1 :minx -10 :miny 0 :maxx 13 :maxy 500 :speed 1}
                      :enemies []}))

(defn reset-engine!
  []
  (reset! engine (eng/new-pixi-engine {:width 640
                                       :height 480
                                       :antialias true
                                       :trasparent false
                                       :resolution 1})))


(defn check-player-state
  [st]
  (let [player-sprite (gobj/get-child @(:stage @engine) :ship00)
        background-sprite (gobj/get-child @(:stage @engine) :bg00)
        inside-playground (eng/inside? (gobj/calc-cb player-sprite
                                                     (get-in st [:player :pos]))
                                       background-sprite)]
        
    inside-playground))


(defn movement-left
  [ctx]
  (let [pos-x (get-in @game-state [:player :pos :x])
        next-x (- pos-x (get-in @game-state [:player :speed]))
        new-state (assoc-in @game-state [:player :pos :x] next-x)]
    (when (check-player-state new-state)
      (reset! game-state new-state))))

(defn movement-right
  [ctx]
  (let [pos-x (get-in @game-state [:player :pos :x])
        next-x (+ pos-x (get-in @game-state [:player :speed]))
        new-state (assoc-in @game-state [:player :pos :x] next-x)]
    (when (check-player-state new-state)
      (reset! game-state new-state))))

(defn shoot
  [ctx]
  (let [bullet (first (filter #(= (:state %) :freezed) (get-in @game-state [:player :bullets])))
        player-pos (get-in @game-state [:player :pos])]
    (when bullet
      (swap! game-state assoc-in [:player :bullets (:idx bullet) :state] :traveling)
      (swap! game-state assoc-in [:player :bullets (:idx bullet) :pos] player-pos))))

(defn update-stage
  [delta]
  (let [bg (gobj/get-child @(:stage @engine) :bg00)
        sh (gobj/get-child @(:stage @engine) :ship00)
        enemies (:enemies @game-state)]
    (when (and bg sh enemies)
      (gobj/translate! sh (get-in @game-state [:player :pos :x])
                       (get-in @game-state [:player :pos :y])))))

(defn render-bullets
  [delta]
  (let [bullets (get-in @game-state [:player :bullets])]
    (when @(:stage @engine)
      (doseq [e bullets]
        (cond
          (= (:state e) :traveling)  (gobj/translate! (gobj/get-child @(:stage @engine) (:id e)) (:pos e))
          (= (:state e) :freezed) (gobj/translate! (gobj/get-child @(:stage @engine) (:id e)) -50 -50))
          ))))

(defn update-bullets
  [delta]
  (let [bullets (get-in @game-state [:player :bullets])]
    (doseq [e bullets
            :when (= (:state e) :traveling)]
      (let [{x :x y :y} (:pos e)
            new-y (- y (:speed e))]
        (if (pos? new-y)
          (swap! game-state assoc-in [:player :bullets (:idx e) :pos] {:x x :y new-y})
          (swap! game-state assoc-in [:player :bullets (:idx e) :state] :freezed))))))

(defn render-enemies
  [delta]
  (when-let [enemies (:enemies @game-state)]
    (doseq [alien enemies]
      (gobj/translate! (:sprite alien) (get-in alien [:pos :x])
                       (get-in alien [:pos :y])))))

(defn update-enemies
  [delta]
  (when-let [enemies (:enemies @game-state)]
    (let [{x :x dr :dir minx :minx maxx :maxx speed :speed} (:enemies-global-pos @game-state)
          next-x (+ x (* dr speed))
          outside? (or (> next-x maxx) (< next-x minx))
          next-dr (if outside? (* -1 dr  ) dr)
          new-x (if outside? next-x x)
          plus-y (if outside? 3 0)]
      (doseq [alien enemies]
        (let [{ax :x ay :y} (:pos alien)
              idx (:idx alien)]
          (swap! game-state assoc-in [:enemies idx :pos] {:x (+ ax (* dr 5)) :y (+ plus-y ay)})))
      (swap! game-state assoc-in [:enemies-global-pos :dir] next-dr)
      (swap! game-state assoc-in [:enemies-global-pos :x] next-x))
    nil))

(defn create-enemy
  [key speed pos texture idx]
  {:id key
   :speed speed
   :pos pos
   :state :alive
   :idx idx
   :sprite (gobj/new-sprite texture key)})

(defn create-bullet
  [key speed pos texture idx]
  {:id key
   :speed speed
   :pos pos
   :state :freezed
   :sprite (gobj/new-sprite texture key)
   :idx idx})

(defn create-bullets
  [key amount speed]
  (into [] 
        (for [i (range amount)]
          (create-bullet (keyword (str key i)) 
                         speed
                         {:x 0 :y 0}
                         :bullet
                         i))))

(defn create-enemies-level
  [margin-x margin-y spacing-x spacing-y rows cols key speed]
  (into []
        (for [j (range rows)
              i (range cols)]
          (create-enemy (keyword (str key i j))
                        speed
                        {:x (+ margin-x (* i spacing-x))
                         :y (+ margin-y (* j spacing-y))}
                        :enemy00
                        (+ (* cols j) i)))))

(defn main-stage
  []
  (let [stage (gobj/new-stage)
        background (gobj/add-sprite! stage :bg00)
        player (gobj/add-sprite! stage :ship00)]
    (gobj/stretch-collision background)
    (gobj/stretch-collision player)
    (gobj/add-text! stage "Score 0" :play-text {:fontFamily "Arial"
                                                :fontSize 26
                                                :fill "#ffffffff"})
    (swap! game-state assoc :enemies (create-enemies-level 55 60 55 40 5 10 "alien" 5))
    (swap! game-state assoc-in [:player :bullets] (create-bullets "pew" 3 10))
    (doseq [e (:enemies @game-state)]
      (gobj/add-child! stage (:sprite e) (:key e)))
    (doseq [e (get-in @game-state [:player :bullets])]
      (gobj/add-child! stage (:sprite e) (:id e)))
    stage))

(defn reset-game!
  [])
  

(defn destroy
  []
  (when @game-state
    (log "Removing engine from DOM")
    (eng/detach! @engine)
    (log "Detaching update observer")
    ; (eng/render-update-unsubscribe @engine (get-in @game-state [:render :observers 0]))
    (doseq [o (get-in @game-state [:render :observers])]
      (ev/remove-observer o))
    ; (ev/remove-observer (get-in @game-state [:render :observers 1]))
    (log "Removing game state")
    (reset! game-state nil)))

(defn start
  []
  (log "Reseting game state")
  (reset-game-state!)
  (log "Creating engine")
  (reset-engine!)
  (log "Pairing stage with the engine")
  (eng/set-stage! @engine main-stage)
  (log "Attaching render to DOM")
  (eng/attach! @engine)
  (log "Loading assets")
  (res/load! loader "assets/" (fn [] (eng/start-game-loop! @engine)))
  (log "Attaching events")
  (swap! game-state update-in [:render :observers] conj (eng/render-update-subscribe @engine update-stage))
  (swap! game-state update-in [:render :observers] conj (eng/render-update-subscribe @engine update-bullets))
  (swap! game-state update-in [:render :observers] conj (eng/render-update-subscribe @engine render-bullets))
  (swap! game-state update-in [:render :observers] conj (eng/render-update-subscribe @engine update-enemies))
  (swap! game-state update-in [:render :observers] conj (eng/render-update-subscribe @engine render-enemies))
  (swap! game-state update-in [:render :observers] conj (ev/add-observer (get-in @engine [:events :kbdown]) movement-left (filter (fn [ctx]
                                                                                                                                    (= "a" (oget ctx "key"))))))
  (swap! game-state update-in [:render :observers] conj (ev/add-observer (get-in @engine [:events :kbdown]) movement-right (filter (fn [ctx]
                                                                                                                                     (= "d" (oget ctx "key"))))))
  (swap! game-state update-in [:render :observers] conj (ev/add-observer (get-in @engine [:events :kbdown]) shoot (filter (fn [ctx]
                                                                                                                            (= " " (oget ctx "key")))))))

(defn init
  "Initializes the game, intented to use when the page is reloaded"
  []
  (log "Setting resources")
  (res/add-assets! loader [[:bg00 "5.png"]
                           [:ship00 "2.png"]
                           [:enemy00 "10.png"]
                           [:bullet "4.png"]] "assets/")
  (start))