import socket
import subprocess

import RPi.GPIO as GPIO

import pins

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
    print('Back to checkpoint')
    clientSocket.send(code)


GPIO.add_event_detect(pins.resetPin, GPIO.FALLING, callback=reset, bouncetime=500)
GPIO.add_event_detect(pins.checkpointPin, GPIO.FALLING, callback=backToCheckpoint, bouncetime=500)

try:
    while True:
        pass
except KeyboardInterrupt:
    subprocess.call(['fuser', '-k', '/dev/ttyAMA0'])
