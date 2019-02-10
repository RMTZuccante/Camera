import cv2
import zmq
import base64
import numpy as np
from appJar import gui
import socket


def getFrame(socket):
    frame = socket.recv_string()
    img = base64.b64decode(frame)
    npimg = np.fromstring(img, dtype=np.uint8)
    return cv2.imdecode(npimg, 1)


def findShapes(image, range=None):
    _, contours, _ = cv2.findContours(image, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)  # Find contours of shapes

    if range is not None:
        conts = []
        for c in contours:
            # If the shape area is between two values insert int in the array
            if range[0] <= cv2.contourArea(c) <= range[1]:
                conts.append(c)
        contours = conts
    return contours


def showStream(black, min, max, socket):
    while True:
        frame = cv2.cvtColor(getFrame(socket), cv2.COLOR_RGB2GRAY)
        _, frame = cv2.threshold(frame, app.getScale(black), 255, cv2.THRESH_BINARY)
        range = (int(app.getSpinBox(min)), int(app.getSpinBox(max)))
        if range[0] == -1 or range[1] == -1:
            range = None

        conts = findShapes(frame, range)
        frame = cv2.cvtColor(frame, cv2.COLOR_GRAY2BGR)
        cv2.drawContours(frame, conts, -1, (0, 255, 0), 3)
        cv2.imshow("Camera", frame)
        k = cv2.waitKey(1)
        if k == ord('q'):
            cv2.destroyAllWindows()
            app.stop()
            break

def save(black):
    print(app.getScale(black))

if __name__ == "__main__":
    print('Your IPs are:')
    for ip in reversed([i[4][0] for i in socket.getaddrinfo(socket.gethostname(), None)]):
        print('\t', ip)
    print()

    context = zmq.Context()
    footage_socket = context.socket(zmq.SUB)
    footage_socket.bind('tcp://*:5555')  # Open socket @ port 5555
    footage_socket.setsockopt_string(zmq.SUBSCRIBE, np.unicode(''))

    app = gui('Camera tools', "500x200")

    slider1 = "Black"
    app.addLabelScale(slider1)
    app.setScaleRange(slider1, 0, 255, 80)
    app.showScaleValue("Black", 1)

    minArea = "Min area"
    app.addLabelSpinBoxRange(minArea, -1, 921600)
    app.setSpinBox(minArea, 500)

    maxArea = "Max area"
    app.addLabelSpinBoxRange(maxArea, -1, 921600)
    app.setSpinBox(maxArea, 6000)

    save = "Save"
    app.addButton(save, save)

    app.thread(showStream, slider1, minArea, maxArea, footage_socket)
    app.go()
