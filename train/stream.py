import numpy as np
import queue
from cv2 import putText, FONT_HERSHEY_SIMPLEX, getTextSize
from threading import Lock


class Stream:
    q = queue.Queue(maxsize=2)
    __lock = Lock()

    def lock(self):
        self.__lock.acquire()

    def unlock(self):
        self.__lock.release()

    def get(self):
        return self.q.get(block=True)

    def add(self, frame):
        with self.__lock:
            self.q.put(frame)

    def addMex(self, mex, color):
        with self.__lock:
            img = np.zeros((800, 800, 3), np.uint8)
            textsize = getTextSize(mex, FONT_HERSHEY_SIMPLEX, 1, 2)[0]
            textX = int((img.shape[1] - textsize[0]) / 2)
            textY = int((img.shape[0] + textsize[1]) / 2)

            putText(img, mex, (textX, textY), FONT_HERSHEY_SIMPLEX, 1, color, 2)
            for i in range(0, 2): self.q.put(img)
