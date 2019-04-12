import RPi.GPIO as GPIO

resetPin = 20
checkpointPin = 21

GPIO.setmode(GPIO.BCM)
GPIO.setup(resetPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(checkpointPin, GPIO.IN, pull_up_down=GPIO.PUD_UP)
