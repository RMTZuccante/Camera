import cv2
import zmq
import base64
import numpy as np
from keras import utils, models, Sequential, layers
from numpy import array, expand_dims, amax, argmax
from time import sleep
from os import path, makedirs
from pickle import dump, load
import sys
import json


def getFrame(socket):
    frame = socket.recv_string()
    img = base64.b64decode(frame)
    npimg = np.fromstring(img, dtype=np.uint8)
    return cv2.imdecode(npimg, 1)


def findShapes(image, blackval, range=None):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)  # Convert the image from RGB to greyscale
    _, th = cv2.threshold(image, blackval, 255, cv2.THRESH_BINARY)  # Convert image into a bit matrix (black and white)
    contours = cv2.findContours(th, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)  # Find contours of shapes
    if len(contours) == 3:
        contours = contours[1]
    else:
        contours = contours[0]

    if range is not None:
        conts = []
        for c in contours:
            # If the shape area is between two values insert int in the array
            if range[0] <= cv2.contourArea(c) <= range[1]:
                conts.append(c)
        contours = conts
    return contours, image


def train(model):
    global labels
    labels = utils.to_categorical(labels, num_classes=None)
    if len(labels) is 0:
        sys.stderr.print('No collected images')
    else:
        model.fit(array(images) / 255.0, labels, epochs=10)


def test():
    while (cv2.waitKey(1) != ord('q')):
        frame = getFrame(footage_socket)
        conts, grey = findShapes(frame, thresh, (min, max))
        for c in conts:
            x, y, w, h = cv2.boundingRect(c)
            img = grey[y: y + h, x:x + w]
            img = cv2.resize(img, (80, 80))
            img = expand_dims(img, 0)
            pred = model.predict(img)
            if amax(pred[0]) > .8:
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                cv2.putText(frame, ref[argmax(pred[0])].upper(), (x + w + 10, y + h), 0, 0.8, (0, 255, 0))
        cv2.imshow(title, frame)


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
footage_socket = None
min = max = thresh = 0
labels = []
images = []

if __name__ == "__main__":
    configfile = open('config.json', 'r')
    config = json.load(configfile)
    configfile.close()

    thresh = int(config['THRESH'])
    min = int(config['MIN_AREA'])
    max = int(config['MAX_AREA'])
    ref = config['ref']

    context = zmq.Context()
    footage_socket = context.socket(zmq.SUB)
    footage_socket.bind('tcp://*:5555')  # Open socket @ port 5555
    footage_socket.setsockopt_string(zmq.SUBSCRIBE, np.unicode(''))

    model = None
    if path.exists('./model/'):
        try:
            model_file = open('./model/model.json', 'r')
            weights_file = open('./model/weights.bin', 'rb')

            if input('Old model files found, would you like to load the old model? [y/n] ').lower() == 'y':
                try:
                    model = models.model_from_json(model_file.read())
                    model.set_weights(load(weights_file))
                    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
                    print('Model loaded')
                except Exception as e:
                    print(str(e))
                    sys.stderr.write('Error loading module, creating a new one\n')
                    model = None

            model_file.close()
            weights_file.close()
        except FileNotFoundError:
            model = None

    if model is None:
        model = Sequential([
            layers.Flatten(input_shape=(80, 80)),
            layers.Dense(128, activation='relu'),
            layers.Dense(3, activation='softmax')
        ])

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

    while True:
        frame = getFrame(footage_socket)

        cv2.imshow(title, frame)
        k = cv2.waitKey(1)

        if k is ord('t'):
            train(model)

        elif k is ord(' '):
            shapes, gray = findShapes(frame, thresh, (min, max))
            for c in shapes:
                img = frame.copy()
                cv2.drawContours(img, [c], -1, (0, 255, 0), 1)
                x, y, w, h = cv2.boundingRect(c)
                cv2.rectangle(img, (x, y), (x + w, y + h), (10, 71, 239), 2)
                for i in range(0, 2): cv2.imshow(title, img)
                k = chr(cv2.waitKey()).upper()
                if k in ref:
                    lim = gray[y: y + h, x:x + w]
                    lim = cv2.resize(lim, (80, 80))
                    labels.append(ref.index(k))
                    images.append(lim)
                elif k is ord('q'):
                    break

        elif k is ord('c'):
            test()

        elif k is ord('s'):
            continue
            save()

        elif k is ord('q'):
            cv2.destroyAllWindows()
            break
