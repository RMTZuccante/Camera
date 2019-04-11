import socket
import subprocess

import RPi.GPIO as GPIO

resetPin = 20
checkpointPin = 21

process = None
clientSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
clientSocket.connect(('localhost', 1042))
code = ('' + chr(42)).encode()


def reset(_):
    global process
    if process != None:
        print('rebooting')
        subprocess.call(['fuser', '-k', '/dev/ttyAMA0'])
    process = subprocess.Popen(("/home/pi/Robotics/robot.sh"), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)


def backToCheckpoint(_):
    clientSocket.send(code)


GPIO.setmode(GPIO.BCM)
GPIO.setup(resetPin, GPIO.IN)
GPIO.setmode(checkpointPin, GPIO.IN)

GPIO.add_event_detect(resetPin, GPIO.FALLING, callback=reset, bouncetime=500)
GPIO.add_event_detect(checkpointPin, GPIO.FALLING, callback=backToCheckpoint, bouncetime=500)

while True:
    pass

