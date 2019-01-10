from threading import RLock
import cv2
import subprocess


class Camera:
    class CameraError(Exception):
        def __init__(self, mex, ):
            super().__init__(mex)

    _left = _right = None

    __cameras = []
    __lock = RLock()

    @staticmethod
    # Load n VideoCapture objects in the _camera array (default n value is 1)
    def loadCameras(n=1):
        with Camera.__lock:
            if len(Camera.__cameras) > 0:
                raise Camera.CameraError(str(len(Camera.__cameras)) + " cameras are already loaded")
            for i in range(n):
                vc = cv2.VideoCapture(i)
                if vc.isOpened():
                    Camera.__cameras.append(vc)
                else:
                    raise Camera.CameraError("Unable to open camera n " + str(i + 1))

    @staticmethod
    # Release all the cameras
    def close():
        with Camera.__lock:
            for c in Camera.__cameras:
                c.release()
            del Camera.__cameras[:]

    @staticmethod
    # Grab a frame from the n camera (default camera is 0, the first)
    def read(n=0):
        with Camera.__lock:
            success, frame = Camera.__cameras[n].read()
            if success:
                return frame
            else:
                raise Camera.CameraError("Error grabbing frame, the camera may be disconnected")

    @staticmethod
    # Lock access to the camera class, only the same thread can call methods
    def lock():
        Camera.__lock.acquire()

    @staticmethod
    # Release the camera class
    def unlock():
        Camera.__lock.release()

    @staticmethod
    def setCameraLR(left, right):
        l = r = None
        ls = subprocess.check_output(['lsusb'])
        dev = ls.splitlines()
        for line in dev:
            line = str(line)
            if left in line:
                l = int(line.split(maxsplit=4)[-2][:-1])
            elif right in line:
                r = int(line.split(maxsplit=4)[-2][:-1])
        Camera._right = int(l < r)
        Camera._left = int(not Camera._right)

    # Utilities for working with images
    class Utils:

        @staticmethod
        # Find all shapes with an area between range values, if range = None, alla shapes are considered
        def findShapes(image, range=None):
            image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)  # Convert the image from RGB to greyscale
            _, th = cv2.threshold(image, 100, 255,
                                  cv2.THRESH_BINARY)  # Convert image into a bit matrix (black and white)
            _, contours, _ = cv2.findContours(th, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)  # Find contours of shapes

            if range is not None:
                conts = []
                for c in contours:
                    # If the shape area is between two values insert int in the array
                    if range[0] <= cv2.contourArea(c) <= range[1]:
                        conts.append(c)
                contours = conts
            return contours, image
