import cv2
from flask import Flask, Response, request
from stream import Stream
from threading import Thread, Lock
from queue import Queue

app = Flask(__name__)


class utils:
    def getShapes(frame):
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        _, th = cv2.threshold(frame, 50, 255, cv2.THRESH_BINARY)
        _, contours, _ = cv2.findContours(th, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

        conts = []

        for c in contours:
            if cv2.contourArea(c) in range(1500, 5000):
                conts.append(c)

        return contours


@app.route('/')
def start():
    thread.start()
    thk.start()
    return Response(vstream(), mimetype='multipart/x-mixed-replace; boundary=frame')


def vstream():
    while thread.isAlive():
        img = cv2.imencode('.jpg', stream.get())[1]
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + img.tobytes() + b'\r\n\r\n')


def main():
    while not stop:
        lock.acquire()
        _, frame = cap.read()
        stream.add(frame)
        lock.release()


def listenKeys():
    while not stop:
        k = kq.get(block=True)
        lock.acquire()
        print("K = '" + k + "'")
        if k is '':
            for c in utils.getShapes(frame):
                img = frame.copy()
                cv2.drawContours(img, [c], 0, (0, 255, 0), 3)
                stream.add(img)
                k = kq.get(block=True)
                if k is 'q':
                    break
        lock.release()


@app.route('/key')
def key():
    kq.put(request.args['k'])
    return ('', 204)


stream = Stream()
thread = Thread(target=main)
cap = cv2.VideoCapture(0)
lock = Lock()
frame = None
kq = Queue(maxsize=5)
thk = Thread(target=listenKeys)
stop = False

app.run(host='localhost', debug=True)
