#!/usr/bin/env python

# WS server example

import asyncio
import websockets
import numpy as np
import io
import skimage.io
import binascii
import os
import sys
import json

ROOT_DIR = os.path.abspath("../python/")

sys.path.append(ROOT_DIR)
from testing_RCNN import evaluate_image
from Mask_Transfer import mask_transfer
from testing_fastTransfer import create_transfer
from utils import save_img
from Display_Mask import display_instances

'''
    This function handles the interactions for the websocket between the android app and our various CNNs we use to modify the picture.
    The flow should be:
    1. Receive an image.
    2. Run MaskRCNN on the Image return image with masks
    3. Receive confirmation, return list of items.
    3. Receive a list of mask names 
    4. Send confirmation
    5. Receive style type.
    4. Run the FastStyleCNN on the image over the specific masks
    5. Return the resulting image.
'''
async def handleImageMaskNStyle(websocket, path):
    print("AWAITING")
    name = await websocket.recv()
    if name == "image":
        print("loading image")
        await handleMask(websocket)

def convert_filepath(style):
    if style == "muse":
        return "../fast-style-transfer/la-muse"
    if style == "seated":
        return "../fast-style-transfer/seated-nude"
    if style == "wave":
        return "../fast-style-transfer/wave"
    return None

async def handleMask(websocket):
    name = ""
    file = []
    while name != "end":
        name = await websocket.recv()
        if name != "end":
            print("got a packet!")
            file.extend(name)
    print("image acquired")
    bfile = bytes(file)
    image = skimage.io.imread(bfile, plugin='imageio')
    mask_results = evaluate_image(image)[0]
    display_instances(image, mask_results['rois'], mask_results['masks'], mask_results['class_ids'], mask_results['class_names'], mask_results['scores']) 
    print("sending ImageBack")
    with open("masked.png", mode='rb') as masks:
        masked_image = masks.read()
        await websocket.send("mask")
        i = 0
        for i in range(1000000, len(masked_image), 1000000):
            await websocket.send(bytes(masked_image[(i-1000000):i]))
        await websocket.send(bytes(masked_image[i:]))
        await websocket.send("endMaskList")
    print("FinishedSendingImage")
    
    maskList = "mask_list"
    for i in range(len(mask_results['class_ids'])):
        id = mask_results['class_ids'][i]
        maskList+="," + mask_results['class_names'][id] + ":" + str(i)
    print("Sending List of Masks")
    await websocket.send(maskList)

    masks = await websocket.recv()
    chosen = []
    print (mask_results['class_ids'])
    #First element is called 'chosen_masks' for the sake of IDing the message
    for i in masks.split(',')[1:]:
        chosen.append(int(i.split(':')[-1]))
    
    #Send a response so the app knows to move onto the next section.
    await websocket.send('mask_received')
    print("maskReceived")

    styles = await websocket.recv()
    style = styles.split(',')[1]

    final_mask = np.zeros([mask_results['masks'].shape[0], mask_results['masks'].shape[1]])
    for i in chosen:
        final_mask |= mask_results['masks'][:,:,i]

    print(style)
    stylized = mask_transfer(image, 1 - final_mask, convert_filepath(style))

    save_img("output.jpg", stylized)

    await websocket.send('styleStart')
    with open("output.jpg", mode='rb') as output_file:
        output = output_file.read()
        i = 0
        for i in range(1000000, len(output), 1000000):
            await websocket.send(bytes(output[(i-1000000):i]))
        await websocket.send(bytes(masked_image[i:]))
        await websocket.send('endStyle')

    #while name != "end":
    #     name = await websocket.recv()
    #     if name != "end":
    #      file.extend(name)
    #        print("image acquired")
    #        bfile = bytes(file)
    #        image = skimage.io.imread(bfile, plugin='imageio')
    #        mask_results = evaluate_image(image)[0]
            
    #        save_img("output.jpg", mask_transfer(image, 1 - mask_results['masks'][:,:,0], "../fast-style-transfer/la-muse"))
    #        print("image saved")
    #        with open("output.jpg", mode='rb') as finished:
    #            result = finished.read()
    #        await websocket.send("finished!")
    

start_server = websockets.serve(handleImageMaskNStyle, 'localhost', 8765)
print("START SERVER")
asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()
print ("STARTED")
