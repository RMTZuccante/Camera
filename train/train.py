import cv2

from flask import Flask, Response, request, abort
from stream import Stream
from threading import Thread
from queue import Queue
from tensorflow import get_default_graph
from keras import models, layers, Sequential, utils
from numpy import array, expand_dims, amax, argmax
from time import sleep
from os import path, makedirs, _exit
from pickle import dump, load
import logging
import sys
import camera

app = Flask(__name__)


@app.before_request
def limit_connections():
    global client_IP

    if client_IP is None:
        if request.path is '/':
            print('Connection accepted')
            client_IP = request.remote_addr  # If the incoming connection is the first, save its IP address
        else:
            abort(403)
    elif request.path is '/' or request.remote_addr != client_IP:  # Limit requests to one single video stream and one single ip address
        abort(403)  # If a new video streaming is


@app.route('/')
# Start the video stream and other services
def start():
    mainthread.start()  # Start the main thread

    def readStream():
        # Send images to the web client
        while True:
            img = stream.get()
            if img is None:
                raise RuntimeError('closing')
            else:
                img = cv2.imencode('.jpg', img)[1]
                yield (b'--frame\r\n'b'Content-Type: image/jpeg\r\n\r\n' + img.tobytes() + b'\r\n\r\n')

    return Response(readStream(), mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/key')
# Capture the key press event from client
def recvkey():
    keys.put(request.args['k'])  # Put the key into a queue
    return '', 204  # Return a 204 no content error to page


# _____________________________________________________

def main():
    frame = None
    while True:
        # If any key is pressed call keyInt to interpret it
        if not keys.empty():
            keyInt(frame)
        else:
            try:
                frame = camera.Camera.read()
                stream.add(camera.Camera.read())
            except camera.Camera.CameraError as e:
                stream.addMex(str(e), (0, 0, 255))
                sys.stderr.write(str(e) + '\n')
                sleep(2)
                stop(-1)


def keyInt(frame):
    global images, labels

    k = keys.get()
    if k is 't':
        train()

    elif k is '':
        frame = frame.copy()
        shapes, gray = camera.Utils.findShapes(frame, (500, 6000))
        for c in shapes:
            img = frame.copy()
            cv2.drawContours(img, [c], -1, (0, 255, 0), 1)
            x, y, w, h = cv2.boundingRect(c)
            cv2.rectangle(img, (x, y), (x + w, y + h), (10, 71, 239), 2)
            for i in range(0, 2): stream.add(img)
            k = keys.get(block=True)
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
        stream.addMex('Exiting', (255, 0, 0))
        sleep(2)
        stop(0)


def train():
    global images, labels, model
    labels = utils.to_categorical(labels, num_classes=None)
    if len(labels) is 0:
        stream.addMex('No collected images', (0, 0, 255))
        sleep(3)
    else:
        with graph.as_default():
            stream.addMex('Training model', (5, 125, 255))
            model.fit(array(images) / 255.0, labels, epochs=10)
            stream.addMex('Model trained', (0, 255, 0))
            sleep(2)


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


def stop(code):
    camera.Camera.close()
    stream.addMex('Program exited', (0, 255, 0))
    sleep(.5)
    _exit(code)


p = path.dirname(__file__)
if len(p) is 0:
    p = '.'
sys.path.append(p + '/../')

try:
    camera.Camera.loadCameras()
except camera.Camera.CameraError as e:
    sys.stderr.write(str(e) + '\n')
    exit(-1)

graph = get_default_graph()
ref = ['h', 's', 'u']
stream = Stream()
mainthread = Thread(target=main)

keys = Queue(maxsize=5)
client_IP = None
images = []
labels = []

model = None

with graph.as_default():
    if path.exists(p + '/../model/'):
        try:
            model_file = open(p + '/../model/model.json', 'r')
            weights_file = open(p + '/../model/weights.bin', 'rb')

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

logging.getLogger('werkzeug').setLevel(logging.CRITICAL)

# Run Flask app

app.run(host='0.0.0.0', use_reloader=False, debug=False)
