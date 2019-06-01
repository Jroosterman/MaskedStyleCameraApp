#!/usr/bin/env python

# WS server example

import asyncio
import websockets

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
    print(f"< {name}")

    #await websocket.send("SUP")

start_server = websockets.serve(handleImageMaskNStyle, 'localhost', 8765)
print("START SERVER")
asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()
print ("STARTED")