import cv2

from flask import Flask, Response, request, abort
from stream import Stream
from threading import Thread, Timer, current_thread
from queue import Queue
from tensorflow import keras, nn, get_default_graph
from numpy import array, expand_dims, amax, argmax
from time import sleep
from os import path, makedirs, _exit
from pickle import dump, load
from camera import Camera
import logging
from sys import stderr

app = Flask(__name__)


@app.before_request
def limit_connections():
    global client_IP

    if client_IP is None:
        print('Connection accepted')
        client_IP = request.remote_addr  # If the incoming connection is the first, save its IP address

    elif request.path is '/' or request.remote_addr != client_IP:  # Limit requests to one single video stream and one single ip address
        abort(403)  # If a new video streaming is


@app.route('/')
# Start the video stream and other services
def start():
    mainthread.start()  # Start the main thread

    # timer.start()

    def readStream():
        # While the thread is alive send images to the web client
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
                frame = Camera.read()
                stream.add(Camera.read())
            except Camera.CameraError as e:
                stream.addMex(str(e), (0, 0, 255))
                stderr.write(str(e))
                sleep(2)
                stop(-1)


def keyInt(frame):
    global images, labels

    k = keys.get()
    if k is 't':
        train()

    elif k is '':
        frame = frame.copy()
        shapes, gray = Camera.Utils.findShapes(frame, (500, 6000))
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
        while keys.empty() or keys.get() != 'q':
            frame = Camera.read()
            conts, grey = Camera.Utils.findShapes(frame, (500, 6000))
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
    p = path.dirname(__file__)
    global model
    try:
        if not path.exists(p + '/../model/'):
            makedirs(p + '/../model/')

        if model is not None:
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
    if current_thread() is not mainthread:
        for i in range(2): keys.put('q')

    Camera.close()
    stream.addMex('Program exited', (0, 255, 0))
    sleep(.5)
    _exit(code)


try:
    Camera.loadCameras()
except Camera.CameraError as e:
    stderr.write(str(e))
    exit(-1)

ref = ['h', 's', 'u']
graph = get_default_graph()

stream = Stream()
mainthread = Thread(target=main)

keys = Queue(maxsize=5)
client_IP = None
images = []
labels = []
alive = True

model = None

with graph.as_default():
    p = path.dirname(__file__)
    if path.exists(p + '/../model/'):
        model_file = open(p + '/../model/model.json', 'r')
        weights_file = open(p + '/../model/weights.bin', 'rb')

        if input('Old model files found, would you like to load the old model? [y/n] ').lower() is 'y':
            model = keras.models.model_from_json(model.read())
            model.set_weights(load(weights_file))
            model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])
            print('Model loaded')

        model_file.close()
        weights_file.close()
        print('Model imported')
    if model is None:
        model = keras.Sequential([
            keras.layers.Flatten(input_shape=(80, 80)),
            keras.layers.Dense(128, activation=nn.relu),
            keras.layers.Dense(3, activation=nn.softmax)
        ])

    model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

timer = Timer(7.0, lambda: stop(0))

logging.getLogger('werkzeug').setLevel(logging.CRITICAL)

# Run Flask app

app.run(host='0.0.0.0', use_reloader=False, debug=False)
