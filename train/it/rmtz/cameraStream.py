import base64
import cv2
import zmq
import sys

if __name__ == "__main__":

    context = zmq.Context()
    footage_socket = context.socket(zmq.PUB)
    footage_socket.setsockopt(zmq.CONFLATE, 1)
    footage_socket.connect('tcp://' + sys.argv[2] + ':2626')
    footage_socket.setsockopt(zmq.SNDHWM, 1)

    camera = cv2.VideoCapture(int(sys.argv[1]))  # init the camera

    while True:
        try:
            _, frame = camera.read()  # grab the current frame
            _, buffer = cv2.imencode('.png', frame)
            jpg_as_text = base64.b64encode(buffer)
            footage_socket.send(jpg_as_text)
        except KeyboardInterrupt:
            camera.release()
            cv2.destroyAllWindows()
            break