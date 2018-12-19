import cv2
import tensorflow as tf
from tensorflow import keras
import numpy

model = None


def exitCV():
    cap.release()
    cv2.destroyAllWindows()


'''def capFrame(frame):
    print('Frame catturato')
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    cv2.imshow('Frame', frame)
    cv2.waitKey(0)
    _, threshold = cv2.threshold(frame, 50, 255, cv2.THRESH_BINARY)
    cv2.imshow('Frame', threshold)
    cv2.waitKey(0)
    _, contours, _ = cv2.findContours(threshold, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

    for cont in contours:
        a = cv2.contourArea(cont)
        if a > 1500 and a < 5000:
            print(cv2.boundingRect(cont))
            print("Per: ", cv2.arcLength(cont, True), " Edg: ",
                  len(cv2.approxPolyDP(cont, 0.1 * cv2.arcLength(cont, True), True)))
            temp = frame
            cv2.drawContours(temp, [cont], 0, (255))
            cv2.imshow('Frame', temp)
            cv2.waitKey(0)

    ''''''for cont in contours:
        obj = frame[cont.]''''''
    cv2.imshow('Frame', frame)
    cv2.waitKey(0)
    cv2.destroyWindow('Frame')
'''


def exitNoSave():
    print('Exiting without saving')
    exitCV()
    exit()


labels = ['h', 's', 'u']
train_images = []
train_lables = []


def findChars(frame):
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    _, threshold = cv2.threshold(frame, 50, 255, cv2.THRESH_BINARY)
    _, contours, _ = cv2.findContours(threshold, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)

    for cont in contours:
        a = cv2.contourArea(cont)
        if a > 1500 and a < 5000:
            x, y, w, h = cv2.boundingRect(cont)
            img = frame.copy()
            clear = img.copy()
            img = cv2.drawContours(img, [cont], 0, (255, 10, 10))
            img = img[y: y + h, x:x + w]
            cv2.imshow('Char', img)

            k = chr(cv2.waitKey(0))
            if k in labels:
                clear = clear[y: y + h, x:x + w]
                clear = cv2.resize(clear, (80, 80))

                train_lables.append(labels.index(k))
                train_images.append(clear)
                print(k, " aggiunta")
            cv2.destroyWindow('Char')


def train(train_images, train_labels):
    train_images = numpy.array(train_images) / 255.0

    global model

    model = keras.Sequential([
        keras.layers.Flatten(input_shape=(80, 80)),
        keras.layers.Dense(128, activation=tf.nn.relu),
        keras.layers.Dense(3, activation=tf.nn.softmax)
    ])

    model.compile(optimizer=tf.train.AdamOptimizer(),
                  loss='sparse_categorical_crossentropy',
                  metrics=['accuracy'])
    model.fit(train_images, train_labels, epochs=15)


def test(cap):
    while True:
        _, frame = cap.read()
        if cv2.waitKey(1) is ord('q'):
            break
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        _, threshold = cv2.threshold(frame, 50, 255, cv2.THRESH_BINARY)
        _, contours, _ = cv2.findContours(threshold, cv2.RETR_TREE, cv2.CHAIN_APPROX_NONE)
        for cont in contours:
            a = cv2.contourArea(cont)
            if a > 1500 and a < 5000:
                img = frame.copy()
                x, y, w, h = cv2.boundingRect(cont)
                img = img[y: y + h, x:x + w]
                img = cv2.resize(img, (80, 80)) / 255.0
                img = numpy.expand_dims(img, 0)
                pred = model.predict(img)
                if numpy.amax(pred[0]) > 0.7:
                    cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
                    cv2.putText(frame, labels[numpy.argmax(pred[0])].upper(), (x + w + 10, y + h), 0, 0.8, (0, 255, 0))

        cv2.imshow('Challenge', frame)
    cv2.destroyWindow('Challenge')


cap = cv2.VideoCapture(0)

while True:
    _, frame = cap.read()
    c = cv2.waitKey(1)
    cv2.imshow('Camera', frame)

    if c is ord(' '):
        findChars(frame)
    if c is ord('t'):
        train(train_images, train_lables)
    if c is ord('c'):
        test(cap)
    if c is ord('q'):
        exitNoSave()

    '''f = keys.get(cv2.waitKey(1))
    if f:
        f()'''

# When everything done, release the capture
