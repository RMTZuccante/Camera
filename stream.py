import numpy as np
import queue
from cv2 import putText, FONT_HERSHEY_SIMPLEX, getTextSize


class Stream:
    q = queue.Queue(maxsize=20)

    def __init__(self):
        self.addBlank()

    def get(self):
        return self.q.get(block=True)

    def add(self, frame):
        self.q.put(frame)

    def addBlank(self):
        self.q.put(np.zeros((400, 400, 3), np.uint8))

    def addMex(self, mex, color):
        img = np.zeros((400, 400, 3), np.uint8)
        textsize = getTextSize(mex, FONT_HERSHEY_SIMPLEX, 1, 2)[0]
        textX = (img.shape[1] - textsize[0]) / 2
        textY = (img.shape[0] + textsize[1]) / 2
        putText(img, mex, (textX, textY), FONT_HERSHEY_SIMPLEX, 1, color, 2)
        self.q.put(img)
