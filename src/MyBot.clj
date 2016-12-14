(ns MyBot
  (:require [game]
            [io])
  (:gen-class))

(def bot-name "MyBot")

(declare map-to-adjacent-sites get-direction-to-nearest-edge)

(defn single-piece-move "Should return a [site direction] pair for a single site"
  [game-map [my-site adjacent-sites]]
  (let  [best-direction-to-go (get-direction-to-nearest-edge game-map my-site)
         site-to-go-to (game/adjacent-site game-map my-site best-direction-to-go)
         i-own-best-site (= (:owner my-site) (:owner site-to-go-to))
         production-comparison (> (:strength my-site) (* 2 (:production my-site)))
         strength-test (>= (:strength my-site )
                           (:strength site-to-go-to))]
    
    (if (or strength-test (and i-own-best-site production-comparison))
      (do
        (spit (str (:owner my-site) "-event.log") (str best-direction-to-go "\n") :append true)
       [my-site best-direction-to-go])
      [my-site :still])))

(defn make-great-guess "Guess making function.
  my-sites is my sites associated the adjacent tiles.
  Should return a list of [site direction] pairs"
  [game-map my-sites]
  (map #(single-piece-move game-map %) my-sites))

(defn distance-to-edge-in-direction [game-map site direction]
  (let [me (:owner site)]
    (loop [distance 0 test-site site]
      (if (= me (:owner test-site))
        (recur (+ 1 distance) (game/adjacent-site game-map test-site direction))
        distance))))

(defn get-direction-to-nearest-edge [game-map site]
  (:direction
   (reduce (fn [closest-direction test-direction]
             (let [distance (distance-to-edge-in-direction game-map site test-direction)
                   is-closer (<= distance (:distance closest-direction))]
               (if is-closer
                 {:direction test-direction :distance distance}
                 closest-direction)))
           {:distance 100 :direction :north}
           game/cardinal-directions)))

(defn carefully-considered-moves
  "Takes a 2D vector of sites and returns a list of [site, direction] pairs"
  [my-id game-map]
  (let [my-sites (->> game-map
                      flatten
                      (filter #(= (:owner %) my-id)))]
    (make-great-guess game-map (map-to-adjacent-sites game-map my-sites))))

(defn -main []
  (let [{:keys [my-id productions width height game-map]} (io/get-init!)]

    ;; Do any initialization you want with the starting game-map before submitting the bot-name

    (println bot-name)

    (doseq [turn (range)]
      (let [game-map (io/create-game-map width height productions (io/read-ints!))]
        (io/send-moves! (carefully-considered-moves my-id game-map))))))

(defn map-to-adjacent-sites [game-map my-sites]
 (reduce (fn [acc site]
           (assoc acc site (vec (map #(game/adjacent-site game-map site %) game/cardinal-directions))))
         {}
         my-sites))

;; helper functions for the repl

(defn random-value-site [x y]
  (let [random-value (fn [] (-> (rand)
                                (* 10)
                                (+ 1)
                                int))]
   (game/->Site x y (random-value) (random-value) 123)))

(defn mock-adjacent-site [game-map site direction]
  (let [x (:x site)
        y (:y site)]
    (cond
      (= :still direction) site
      (= :north direction) (random-value-site x (+ 1 y))
      (= :south direction) (random-value-site x (- y 1))
      (= :west direction) (random-value-site (+ x 1) y)
      (= :east direction) (random-value-site (- x 1) y))))

(def my-sites  (let [rand-val (fn [] (-> (rand)
                                         (* 10)
                                         (+ 1)
                                         int))]
                 (take 5
                       (repeatedly #(random-value-site (rand-val) (rand-val)) ))))

