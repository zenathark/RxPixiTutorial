(ns clopi.resource-manager
  (:require [clopi.utils :refer [log]]
            [oops.core :refer [ocall oget oset!]]
            [pixi.js :as PIXI]))

(defprotocol ResourceManager
  (add-asset! 
   [t key path]
   [t key path base-url]
   [t key path base-url cb]
   "Adds a reference to an asset file with unique `key` and expected to be
    stored at `path`. It will be loaded later calling
    `clopi.resource-manager/load`. If a callback `cb` is provided, it will
    be invoked once this resource is loaded. The `path` argument can be
    relative")
  (add-assets! 
   [t assets]
   [t assets base-url]
   [t assets base-url cb]
   "Similar to add-asset! but for multiple assets. The assets are expected with
   the form `[:key0 path0 :key1 path1 ...]`. The callback will be invoked for each
   individuall resource.")
  (load! 
    [t]
    [t base-url]
    [t base-url cb]
    "Attemps to load all assets from its stored references. If a callback `cb`
   is provided, it will be invoked at the end of the loading process. The
   `base-url` argument provides the common path for all relative asset paths.")
  (set-base-url!
    [t base-url]
    "Sets the base url for resource retrieval")
  (get-base-url
    [t]
    "Returns the base url for resource retrieval"))

(defrecord PixiResourceManager [pixi-obj]
  ResourceManager
  (set-base-url!
    [t base-url]
    (oset! (:pixi-obj t) "baseUrl" base-url))
  (get-base-url
    [t]
    (oget (:pixi-obj t) "baseUrl"))
  (add-asset!
    [t key path base-url cb]
    (set-base-url! t base-url)
    (ocall (:pixi-obj t) "add" (clj->js {:name key
                                         :url path
                                         })))
  (add-asset!
    [t key path base-url]
    (add-asset! t key path base-url nil))
  (add-asset!
    [t key path]
    (add-asset! t key path (get-base-url t) nil))
  (add-assets!
    [t assets base-url cb]
    (doseq [[key path] assets]
      (add-asset! t key path base-url cb)))
  (add-assets!
    [t assets base-url]
    (add-assets! t assets base-url nil))
  (add-assets!
    [t assets]
    (add-assets! t assets (get-base-url t) nil))
  (load!
    [t base-url cb]
    (set-base-url! t base-url)
    (ocall (:pixi-obj t) "load" cb))
  (load!
    [t base-url]
    (load! t base-url nil))
  (load!
    [t]
    (load! t (get-base-url t) nil)))

(def pixi-resource-manager (->PixiResourceManager js/PIXI.loader))

  ; (defn load-assets
  ;   [assets base-url]
  ;   (let [old-url (oget js/PIXI.loader "baseUrl")
  ;         loader  (oset! js/PIXI.loader "baseUrl" base-url)]
  ;     (-> loader
  ;         ((fn [l] (reduce (fn [ld [nme url]]
  ;                           (ocall ld "add" (clj->js {:name nme
  ;                                                     :url  url
  ;                                                     :onComplete cb})))
  ;                         l
  ;                         assets)))
  ;         (ocall "load!" nil))
  ;     (oset! js/PIXI.loader "baseUrl" old-url)))
