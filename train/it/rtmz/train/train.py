import cv2
import zmq
import base64
import numpy as np
from tensorflow import get_default_graph
from keras import models, layers, Sequential, utils
from numpy import array, expand_dims, amax, argmax
from time import sleep
from os import path, makedirs, _exit
from pickle import dump, load
import sys


def getFrame(socket):
    frame = socket.recv_string()
    img = base64.b64decode(frame)
    npimg = np.fromstring(img, dtype=np.uint8)
    return cv2.imdecode(npimg, 1)


def findShapes(image, blackval, range=None):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)  # Convert the image from RGB to greyscale
    _, th = cv2.threshold(image, blackval, 255, cv2.THRESH_BINARY)  # Convert image into a bit matrix (black and white)
    _, contours, _ = cv2.findContours(th, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)  # Find contours of shapes

    if range is not None:
        conts = []
        for c in contours:
            # If the shape area is between two values insert int in the array
            if range[0] <= cv2.contourArea(c) <= range[1]:
                conts.append(c)
        contours = conts
    return contours, image


def train():
    global images, labels, model
    labels = utils.to_categorical(labels, num_classes=None)
    if len(labels) is 0:
        sys.stderr.print('No collected images')
    else:
        model.fit(array(images) / 255.0, labels, epochs=10)


def test():
    with graph.as_default():
        while (keys.empty() or keys.get() != 'q'):
            frame = camera.Camera.read()
            conts, grey = camera.Utils.findShapes(frame, (500, 6000))
            for c in conts:
                x, y, w, h = cv2.boundingRect(c)
                img = grey[y: y + h, x:x + w]
                img = cv2.resize(img, (80, 80))
                img = expand_dims(img, 0)
                pred = model.predict(img)
                if amax(pred[0]) > .8:
                    cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                    cv2.putText(frame, ref[argmax(pred[0])].upper(), (x + w + 10, y + h), 0, 0.8, (0, 255, 0))
            stream.add(frame)


def save():
    global model
    try:
        if not path.exists(p + '/../model/'):
            makedirs(p + '/../model/')

        if model is not None:
            model.save(p + '/../model/model.h5')
            model_file = open(p + '/../model/model.json', 'w')
            weights_file = open(p + '/../model/weights.bin', 'wb')

            model_file.write(model.to_json())
            model_file.close()

            dump(model.get_weights(), weights_file)
            weights_file.close()
            stream.addMex('Model saved', (0, 255, 0))
            sleep(2)
        else:
            stream.addMex('Cannot save an empty model!', (0, 0, 255))
            sleep(2)
    except FileNotFoundError:
        stream.addMex('Unable to save model, file not found', (0, 0, 255))


title = "Train"
ref = ['H', 'S', 'U']

if __name__ == "__main__":
    context = zmq.Context()
    footage_socket = context.socket(zmq.SUB)
    footage_socket.bind('tcp://*:5555')  # Open socket @ port 5555
    footage_socket.setsockopt_string(zmq.SUBSCRIBE, np.unicode(''))

    while True:
        frame = getFrame(footage_socket)

        cv2.imshow(title, frame)
        k = chr(cv2.waitKey(1))

        if k is 't':
            train()

        elif k is '':
            shapes, gray = findShapes(frame, 80, (500, 6000))
            for c in shapes:
                img = frame.copy()
                cv2.drawContours(img, [c], -1, (0, 255, 0), 1)
                x, y, w, h = cv2.boundingRect(c)
                cv2.rectangle(img, (x, y), (x + w, y + h), (10, 71, 239), 2)
                for i in range(0, 2): cv2.imshow(title, img)
                k = chr(cv2.waitKey())
                if k in ref:
                    lim = gray[y: y + h, x:x + w]
                    lim = cv2.resize(lim, (80, 80))
                    labels.append(ref.index(k))
                    images.append(lim)
                elif k is 'q':
                    break

        elif k is 'c':
            test()

        elif k is 's':
            save()

        elif k is 'q':
            cv2.destroyAllWindows()
            break
