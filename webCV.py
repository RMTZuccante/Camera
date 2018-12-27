import cv2
from flask import Flask, Response, request, abort
from stream import Stream
from threading import Thread
from queue import Queue

app = Flask(__name__)


class utils(object):
    @staticmethod
    def getShapes(frame):
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        _, th = cv2.threshold(frame, 50, 255, cv2.THRESH_BINARY)
        _, contours, _ = cv2.findContours(th, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

        conts = []

        for c in contours:
            if 1500 <= cv2.contourArea(c) <= 5000:
                conts.append(c)

        return conts


@app.before_request
def limit():
    if request.path is '/' and connected:
        abort(403)


@app.route('/')
def start():
    global connected
    connected = True
    thread.start()
    return Response(vstream(), mimetype='multipart/x-mixed-replace; boundary=frame')


def vstream():
    while thread.isAlive():
        img = cv2.imencode('.jpg', stream.get())[1]
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + img.tobytes() + b'\r\n\r\n')


def main():
    global frame
    while not stop:
        if not kq.empty():
            keyInt()
        _, frame = cap.read()
        stream.add(frame)


def keyInt():
    global images, labels, frame
    k = kq.get()
    if k is 't':
        if len(labels) is 0:
            stream.addMex('No collected images', (0, 0, 255))
            kq.get(block=True)
    if k is '':
        for c in utils.getShapes(frame):
            img = frame.copy()
            cv2.drawContours(img, [c], -1, (0, 255, 0), 1)
            x, y, w, h = cv2.boundingRect(c)
            cv2.rectangle(img, (x, y), (x + w, y + h), (10, 71, 239), 2)
            for i in range(0, 2): stream.add(img)
            k = kq.get(block=True)
            if k in ref:
                lim = cv2.cvtColor(frame[y: y + h, x:x + w], cv2.COLOR_BGR2GRAY)
                lim = cv2.resize(lim, (80, 80))
                labels.append(ref.index(k))
                images.append(lim)


@app.route('/key')
def key():
    kq.put(request.args['k'])
    return ('', 204)


def load():
    global stream, thread, kq, stop, connected, frame, images, labels
    stream = Stream()
    thread = Thread(target=main)
    frame = None
    kq = Queue(maxsize=5)
    stop = False
    connected = False
    images = []
    labels = []


def stop():
    cap.release()
    load()


cap = cv2.VideoCapture(0)
ref = ['h', 's', 'u']

stream = Stream()
thread = Thread(target=main)
frame = None
kq = Queue(maxsize=5)
stop = False
connected = False
images = []
labels = []

app.run(host='localhost', debug=True)
