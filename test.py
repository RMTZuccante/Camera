from camera import Camera
from keras import models
from pickle import load
from sys import stderr, stdout
from os import path
from cv2 import boundingRect, resize, imshow, waitKey
from numpy import array, amax, argmax
from time import sleep
from collections import Counter


def findChar(model, frame, range=(1000, 8000)):
    conts, gray = Camera.Utils.findShapes(frame, range)
    images = []

    for c in conts:
        x, y, w, h = boundingRect(c)
        images.append(resize(gray[y: y + h, x:x + w], (80, 80)))

    chars = []
    if len(images) > 0:
        pred = model.predict(array(images))
        for c in pred:
            #if amax(c) > .8:
            chars.append(argmax(c))

    return chars


def main():
    dir = path.dirname(__file__)
    if dir is None or len(dir) is 0:
        dir = '.'

    ref = ['H', 'S', 'U']

    try:
        model_file = open(dir + '/model/model.json', 'r')
        weights_file = open(dir + '/model/weights.bin', 'rb')

        model = models.model_from_json(model_file.read())
        model.set_weights(load(weights_file))
        model.compile(optimizer='adam', loss='sparse_categorical_crossentropy', metrics=['accuracy'])

        model_file.close()
        weights_file.close()
        print('Model imported')
    except FileNotFoundError:
        stderr.write('Model files not found\n')
        exit(-1)
    except Exception:
        stderr.write('Model files are corrupted\n')
        exit(-1)

    Camera.loadCameras()

    while True:
        frame = Camera.read()
        c = Counter(findChar(model, frame))
        imshow('camera', frame)
        waitKey(1)
        for group in [[k, ] * v for k, v in c.items()]:
            if len(group) > 0:
                stdout.write(str(len(group)) + ' ' + ref[group[0]] + ', ')
        if len(c) > 0:
            print()
        sleep(.2)

    Camera.close()


if __name__ == "__main__":
    main()
