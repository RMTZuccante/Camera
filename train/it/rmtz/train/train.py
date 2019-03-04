import cv2
import zmq
import base64
import numpy as np
from keras import utils, models, Sequential, layers
from numpy import array, expand_dims, amax, argmax
from os import path, makedirs
import sys
import json
from socket import gethostname, getaddrinfo


def errprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


def getFrame(socket):
    frame = socket.recv_string()
    img = base64.b64decode(frame)
    npimg = np.frombuffer(img, dtype=np.uint8)
    return cv2.rotate(cv2.imdecode(npimg, 1), cv2.ROTATE_180)


def findShapes(image, blackval, range=None):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)  # Convert the image from RGB to greyscale
    _, th = cv2.threshold(image, blackval, 255, cv2.THRESH_BINARY)  # Convert image into a bit matrix (black and white)
    contours = cv2.findContours(th, cv2.RETR_TREE,
                                cv2.CHAIN_APPROX_SIMPLE)  # Find contours of shapes (it returns other values like the success)
    contours = contours[len(contours) - 2]  # Take only contours array from cv2.finContours, the index isn't fixed for compatibility purpose

    if range is not None:
        conts = []
        for c in contours:
            # If the shape is taller than wide and its area is between two values insert int in the array
            _, _, h, w = cv2.boundingRect(c)
            if h > w and range[0] <= cv2.contourArea(c) <= range[1]:
                conts.append(c)
        contours = conts
    return contours, image


def train(model):
    global labels, images
    if len(labels) is 0:
        errprint('No collected images')
    else:
        lbls = utils.to_categorical(labels, num_classes=None)
        try:
            model.fit(array(images), lbls, epochs=10)
            labels = []
            images = []
        except ValueError:
            errprint('Cannot train model with collected images, at least 1 image per character is needed')


def test():
    print('Testing')
    while (cv2.waitKey(1) != ord('q')):
        frame = getFrame(footage_socket)
        conts, grey = findShapes(frame, thresh, (min, max))
        for c in conts:
            x, y, w, h = cv2.boundingRect(c)
            img = grey[y - offset: y + h + offset, x:x + w + offset]
            try:
                img = cv2.resize(img, (80, 80))
            except cv2.error:
                print('sucz')
                img = grey[y: y + h, x:x + w]
                img = cv2.resize(img, (80, 80))
            _, img = cv2.threshold(img, thresh, 255, cv2.THRESH_BINARY)
            img = expand_dims(img, 0)
            pred = model.predict(img / 255.0)
            if amax(pred[0]) > precision:
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                cv2.putText(frame, ref[argmax(pred[0])].upper() + ' ' + str("%.3f" % (amax(pred[0]) * 100)) + '%',
                            (x + w + 10, y + h), 0, 0.8, (0, 255, 0))
        cv2.imshow(title, frame)
    print('Quit testing')


def save():
    global model
    try:
        if not path.exists('../model/'):
            makedirs('../model/')

        if model is not None:
            model.save('../model/model.h5')
            '''model_file = open('../model/model.json', 'w')
            weights_file = open('../model/weights.bin', 'wb')

            model_file.write(model.to_json())
            model_file.close()

            dump(model.get_weights(), weights_file)
            weights_file.close()'''
            print('Model saved')
        else:
            errprint('Cannot save an empty model!')
    except FileNotFoundError:
        errprint('Unable to save model, file not found')


def deflect(img):
    newimg = cv2.bitwise_not(img)
    thresh = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]
    coords = np.column_stack(np.where(thresh > 0))
    angle = cv2.minAreaRect(coords)[-1]
    if angle < -45:
        angle = -(90 + angle)
    else:
        angle = -angle


title = "Train"
footage_socket = None
min = 0
max = 0
thresh = 0
labels = []
images = []
precision = 0

if __name__ == "__main__":
    configfile = open('../config.json', 'r')
    config = json.load(configfile)
    configfile.close()

    thresh = int(config['THRESH'])
    min = int(config['MIN_AREA'])
    max = int(config['MAX_AREA'])
    ref = config['ref']
    offset = int(config['OFFSET'])
    precision = float(config['PRECISION'])

    context = zmq.Context()
    footage_socket = context.socket(zmq.SUB)
    footage_socket.setsockopt(zmq.CONFLATE, 1)
    footage_socket.bind('tcp://*:2626')  # Open socket @ port 2626
    footage_socket.setsockopt_string(zmq.SUBSCRIBE, np.unicode(''))

    print('Hostname is: ', gethostname())
    print('Your IPs are:')
    for ip in reversed([i[4][0] for i in getaddrinfo(gethostname(), None)]):
        print('\t', ip)
    print()

    model = None
    if path.exists('../model/'):
        if input('Old model files found, would you like to load the old model? [y/n] ').lower() == 'y':
            try:
                model = models.load_model('../model/model.h5')
                print('Model loaded')
            except Exception as e:
                print(str(e))
                errprint('Error loading module, creating a new one\n')
                model = None

    if model is None:
        model = Sequential([
            layers.Flatten(input_shape=(80, 80)),
            layers.Dense(128, activation='relu'),
            layers.Dense(3, activation='softmax')
        ])

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

    print('Started')

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
                cv2.rectangle(img, (x - offset, y - offset), (x + w + offset, y + h + offset), (10, 71, 239), 2)
                for i in range(0, 2): cv2.imshow(title, img)
                k = chr(cv2.waitKey()).upper()
                if k in ref:
                    lim = gray[y - offset: y + h + offset, x - offset:x + w + offset]

                    lim = cv2.resize(lim, (80, 80))
                    labels.append(ref.index(k))
                    images.append(lim / 255.0)
                elif k is ord('q'):
                    break

        elif k is ord('c'):
            test()

        elif k is ord('s'):
            save()

        elif k is ord('q'):
            cv2.destroyAllWindows()
            break
