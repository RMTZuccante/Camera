import socket
import subprocess

import RPi.GPIO as GPIO

import pins

process = None
clientSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
code = ('' + chr(42)).encode()


def reset(_):
    global process
    if process != None:
        print('Killing process')
        subprocess.call(['fuser', '-k', '/dev/ttyAMA0'])
    print('Starting')
    process = subprocess.Popen(("/home/pi/Robotics/robot.sh"), stdout=subprocess.PIPE, universal_newlines=True)


def backToCheckpoint(_):
    print('Back to checkpoint')
    clientSocket.sendto(code, ('localhost', 1042))


GPIO.add_event_detect(pins.resetPin, GPIO.FALLING, callback=reset, bouncetime=500)
GPIO.add_event_detect(pins.checkpointPin, GPIO.FALLING, callback=backToCheckpoint, bouncetime=500)

try:
    while True:
        pass
except KeyboardInterrupt:
    subprocess.call(['fuser', '-k', '/dev/ttyAMA0'])
