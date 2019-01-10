from keras import models, backend as K
from pickle import load
from os import path
from sys import stderr
import tensorflow as tf

dir = path.dirname(__file__)
if dir is None or len(dir) is 0:
    dir = '.'

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

tfgraph = K.get_session().graph

tf.train.write_graph(tfgraph, './model', 'tfModel.pb', as_text=False)