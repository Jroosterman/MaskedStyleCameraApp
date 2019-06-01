import os
import sys
import tensorflow as tf
import skimage.io
import json
import numpy as np

ROOT_DIR = os.path.abspath("./fast-style-transfer/src")
sys.path.append(ROOT_DIR)
import transform


def create_transfer(img, checkpoint_dir, device_t='/gpu:0'):
    g = tf.Graph()
    soft_config = tf.ConfigProto(allow_soft_placement=True)
    soft_config.gpu_options.allow_growth = True
    with g.as_default(), g.device(device_t), \
             tf.Session(config=soft_config) as sess:
        batch_shape = (1,) + img.shape
        img_placeholder = tf.placeholder(tf.float32, shape=batch_shape, name='img_placeholder')

        preds = transform.net(img_placeholder)
        saver = tf.train.Saver()
        ckpt = tf.train.get_checkpoint_state(checkpoint_dir)
        saver.restore(sess, ckpt.model_checkpoint_path)
        X = np.zeros(batch_shape, dtype=np.float32)
        X[0] = img
        _preds = sess.run(preds, feed_dict={img_placeholder:X})
        return _preds

#if __name__ == "__main__":
#    image = skimage.io.imread("tubingen.jpg")
#    print(create_transfer(image, "./fast-style-transfer/seated-nude2/"))
