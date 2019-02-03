import cv2
import zmq
import base64
import numpy as np


def getFrame(socket):
    frame = socket.recv_string()
    img = base64.b64decode(frame)
    npimg = np.fromstring(img, dtype=np.uint8)
    return cv2.imdecode(npimg, 1)


if __name__ == "__main":
    context = zmq.Context()
    footage_socket = context.socket(zmq.SUB)
    footage_socket.bind('tcp://*:5555')  # Open socket @ port 5555
    footage_socket.setsockopt_string(zmq.SUBSCRIBE, np.unicode(''))

    while True:
        cv2.imshow("Stream", getFrame(footage_socket))
        k = cv2.waitKey(1)
        if k == 'q':
            cv2.destroyAllWindows()
            break
