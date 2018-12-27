from threading import Lock
import numpy as np
import queue


class Stream:
    q = queue.Queue(maxsize=20)
    lock = Lock()

    def __init__(self):
        self.q.put(np.zeros((400, 400, 3), np.uint8))

    def get(self):
        return self.q.get(block=True)

    def add(self, frame):
        with self.lock:
            self.q.put(frame)
