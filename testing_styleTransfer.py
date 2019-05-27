import os
import sys
import numpy as np
#np.random.seed(1)
#from tensorflow import set_random_seed
#set_random_seed(1)

ROOT_DIR = os.path.abspath("./a-neural-algorithm-of-artistic-style/")
sys.path.append(ROOT_DIR)

from neural_stylization.transfer_style import Stylizer
from neural_stylization.optimizers import GradientDescent, L_BFGS, Adam
from neural_stylization.util.build_callback import build_callback
from neural_stylization.util.img_util import load_image

CONTENT = 'a-neural-algorithm-of-artistic-style/img/content/tubingen.jpg'
load_image(CONTENT)

# the standardized dimensions for the images in this notebook
# DIMS = int(1024/3), int(768/3)

# setting to None uses the default size of the content image.
# WARNING: extremely memory intensive depending on the size
# of the content image. prepare to torture your machine
DIMS = None

DIMS

sty = Stylizer(content_weight=1, style_weight=1e5)

seated_nudes = sty(
    content_path=CONTENT,
    style_path='a-neural-algorithm-of-artistic-style/img/styles/seated-nude.jpg',
    optimize=L_BFGS(max_evaluations=20),
    iterations=30,
    image_size=DIMS,
    callback=build_callback('a-neural-algorithm-of-artistic-style/build/transfer/seated-nude')
)

seated_nudes.save('resultimg.png')

