(define arduino (firmata))

(let loop ()
  (arduino:digital-write 13 #t)
  (delay 1.0)

  (arduino:digital-write 13 #f)
  (delay 1.0)
  (loop))