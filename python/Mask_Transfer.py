from testing_fastTransfer import create_transfer
from testing_RCNN import evaluate_image
from utils import save_img
import skimage.io
import numpy as np


def mask_transfer(original, mask, checkpoint_dir, device_t='/gpu:0'):
    transfered = create_transfer(original, checkpoint_dir, device_t)[0]
    fixed_mask = np.zeros([mask.shape[0], mask.shape[1], 3])
    fixed_mask[:, :, 0] = mask
    fixed_mask[:, :, 1] = mask
    fixed_mask[:, :, 2] = mask
    size_offset = np.array(transfered.shape) - np.array(original.shape)
    starting = size_offset // 2
    cropped_transfer = transfered[starting[0]:(starting[0]+original.shape[0]), starting[1]:(starting[1]+original.shape[1])]
    print(transfered.shape)
    print(cropped_transfer.shape)
    print(original.shape)
    print(fixed_mask.shape)
    return fixed_mask * cropped_transfer + (1 - fixed_mask) * original

def test():
    image = skimage.io.imread("../Mask_RCNN/images/3862500489_6fd195d183_z.jpg")
    results = evaluate_image(image)
    mask = 1 - results[0]['masks'][:,:, 0]
    transfered = mask_transfer(image, mask, "../fast-style-transfer/wave")
    return transfered

if __name__ == "__main__":
    save_img("output.jpg", test())
    
