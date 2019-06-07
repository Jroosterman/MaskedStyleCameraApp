# MaskedStyleCameraApp
An app that Masks sections of a Picture you take and stylizes it to your favorite artwork.

This application is created by Andrew Miller and John Frankel for Drexel's Computer Vision Class.

Unfortunately we were not able to get our AWS server up and running in time so to run this project you will need to Get Android Studio and python3 set up.

Running the Android app:
1. Download and install Android Studio
2. Open the Android app (use android studio to open the project in the app folder)
3. Hit the run button
4. Create an Emulated device.  NOTE: if you want to run on your own phone you will need to enable developer mode and USB debugging.
5. Run the application with the run button and it should appear on the emulated device.

Running the Server:
1. Download tensor flow
2. In PythonServer run "pip3 install -r requirements.txt". This will install the requirements for the server.
3. In the folder PythonServer you will want to run MasterWebsocket.py
4. They may be dependencies you don't have so download them with pip3 install and keep trying until you have all of them.

Once both pieces are running you can use the application.

If you are running on your local LAN with your own phone you will need to change the IP Addresses in both MasterWebsocket.py and sendToServerActivity.java
The line in sendToServerActivity.java looks like 
String address = "ws://10.0.2.2:8765";
Change the ip to the IP of your server machine:
String address = "ws://10.3.3.122:8765";
In MasterWebsocket.py go towards the bottom and replace the line:
start_server = websockets.serve(handleImageMaskNStyle, 'localhost', 8765)
with that machines local ip address
start_server = websockets.serve(handleImageMaskNStyle, '10.3.3.122', 8765)

We apologize that this is difficult to run.  We were hoping to get the AWS server running but we had little experience with it and could not get it working in time.


