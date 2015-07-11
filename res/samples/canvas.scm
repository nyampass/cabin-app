(define radius 50.0)
(define c (canvas))
(define x (/ (c:width) 2))
(define y (/ (c:height) 2))
(define nx x)
(define ny y)

(define d 16)

(define (mouse-moved x y)
  (set! nx x)
  (set! ny y))

(c:strokeWeight 10)

(define frameCount 0)

(let loop ()
  (set! radius (+ radius (sin (/ frameCount 4))))

  (set! x (+ x (/ (- nx x) d)))
  (set! y (+ y (/ (- ny y) d)))

  (c:background (color "#707070"))

  (c:fill (color 0 121 184 ))

  (c:stroke (color "white"))

  (c:ellipse x y radius radius )

  (set! frameCount (+ frameCount 1))
  (delay 0.2)
  (loop))

