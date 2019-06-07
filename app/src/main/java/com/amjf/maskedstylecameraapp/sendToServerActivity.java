package com.amjf.maskedstylecameraapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.maskedstylecameraapp.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity that handles the heart of the application and sends messages to and from the web server.
 * We will take the photo from the last activity, send it to the server for MaskRCNN.  Get the
 * results back and use them to pick our mask.  We will pick our style and send it to the server
 * then we will return the results.
 */
public class sendToServerActivity extends AppCompatActivity {

    // Local values
    private Uri storedPhoto;
    private WebSocketClient socket;
    private ByteArrayOutputStream dataStream;
    private boolean inImage;
    private String[] masks;
    private List<Integer> chosenMasks;

    // UI Elements
    private Button sendAgain;
    private ProgressBar spinner;
    private ImageView imageView;
    private Button chooseMask;
    private Button chooseStyle;

    // These are to hardcoded to quickly get this working.  Not Planned for production.
    String address = "ws://10.0.2.2:8765";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_server);

        //Get all of the UI Elements we modify in the activity.
        spinner = (ProgressBar) findViewById(R.id.progress_loader);
        sendAgain = (Button) findViewById(R.id.Retry);
        sendAgain.setEnabled(false);
        imageView = (ImageView) findViewById(R.id.capturedImage);
        chooseMask = (Button) findViewById(R.id.ChooseMask);
        chooseMask.setEnabled(false);
        chooseStyle = (Button) findViewById(R.id.chooseStyle);
        chooseStyle.setEnabled(false);


        //Get the current photo uri from Main Activity.
        Intent intent = getIntent();
        storedPhoto = Uri.parse(intent.getStringExtra("image"));
        makeConnections();

        //Set up all of our listeners.
        sendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinner.setEnabled(true);
                        sendAgain.setEnabled(false);
                        imageView.setImageDrawable(null);
                    }
                });
                makeConnections();
            }
        });

        chooseMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maskDialog();
            }
        });

        chooseStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStyleDialog();
            }
        });

    }

    /**
     * Handles all of the steps needed to connect to the server
     * In order to get this to work in a resonalble amount of time, we decied to use a server
     * to process the Networks.  The list of reasonings and other options can be found in our report
     * The flow of the network should go as follows.
     * * 1. Connect to the server and send the image.
     * * 2. After some time receive a new image.
     * * 3. Send a response back to the server to say that you got the image.
     * * 4. Receive a list of mask names. (CSV)
     * * 5. send a list of mask names (CSV)
     * * 6. Receive a response from the Server.
     * * 7. Send the chosen style name
     * * 8. After some time receive the resulting image from the server.
     * * 9. Close the connection.  Save the image.
     */
    private void makeConnections() {
        //Connect to our websocket and wait a second for the connection to finish.
        connectWebSocket();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Read our file into a byte array.
        InputStream iStream = null;
        try {
            iStream = getContentResolver().openInputStream(storedPhoto);
        } catch (FileNotFoundException e) {
            Log.w("EXCEPTION", e.getMessage());
        }
        byte[] inputData = new byte[0];
        try {
            inputData = getBytes(iStream);
        } catch (IOException e) {
            Log.w("EXCEPTION", e.getMessage());
        }

        //Send the bytes to the server 1 meg at a time.
        try {
            socket.send("image");
            int i = 0;
            System.out.println(inputData[10]);
            for (; (i + 1000000) < inputData.length; i += 1000000) {
                socket.send(Arrays.copyOfRange(inputData, i, i + 1000000));
            }
            socket.send(Arrays.copyOfRange(inputData, i, inputData.length));
            socket.send("end");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            alertWithMessage("Failed to send initial image");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendAgain.setEnabled(true);
                }
            });

            return;
        }
    }

    /**
     * When a failure occurs, we alert the user with a message.
     * This is used to pop up a given message.
     *
     * @param message
     */
    private void alertWithMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setTitle("Oh No!!");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Create the dialog for looking at your list of masks and display it to the screen.
     */
    private void maskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        chosenMasks = new ArrayList<Integer>();
        // Set the dialog title
        builder.setTitle(R.string.maskDialog)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(masks, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    chosenMasks.add(which);
                                } else if (chosenMasks.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    chosenMasks.remove(Integer.valueOf(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton(R.string.mask, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the selectedItems results somewhere
                        // or return them to the component that opened the dialog
                        sendMasks("nomask");
                    }
                })
                .setNegativeButton(R.string.nomask, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sendMasks("mask");
                    }

                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Make a dialog for picking style.
     * Note that this is pretty rough but we are running out of time here.
     */
    private void createStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.style_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();
        Button chooseMuse = (Button) dialog.findViewById(R.id.chooseMuse);
        Button chooseSeated = (Button) dialog.findViewById(R.id.chooseSeated);
        Button chooseWave = (Button) dialog.findViewById(R.id.chooseWave);
        Button choosePC = (Button) dialog.findViewById(R.id.choosePc);

        // We need to handle our various styles we want to use and get an action when we select one.
        chooseMuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenStyle(dialog, "muse");
            }
        });
        chooseSeated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenStyle(dialog, "seated");
            }
        });
        chooseWave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenStyle(dialog, "wave");
            }
        });
        choosePC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chosenStyle(dialog, "pc");
            }
        });


    }

    /**
     * Choose the style we want to use.
     * @param dialog Dialog to close.
     * @param type type of style we want to use.
     */
    private void chosenStyle(AlertDialog dialog, String type) {
        final AlertDialog d = dialog;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                d.dismiss();
                chooseStyle.setEnabled(false);
                imageView.setImageDrawable(null);
            }
        });
        String message = "style_type," + type;
        socket.send(message);
    }

    /**
     * Handle the action where we send masks and on success we Activate the next button.
     */
    private void sendMasks(String m) {
        String message = m;
        for (Integer i : chosenMasks) {
            message += "," + masks[i];
        }

        socket.send(message);
    }

    /**
     * Web socket logic.  How web sockets are handled.
     */
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        // This handles what our wb socket will do.
        socket = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            // When we receive a message detect what type it is.
            @Override
            public void onMessage(String s) {
                final String message = s;
                // If it is a mask message get ready to read an image.
                if (message.equals("mask")) {
                    inImage = true;
                    dataStream = new ByteArrayOutputStream();
                }
                // If it is a style start message.  Get ready to receive an image.
                else if (message.equals("styleStart")) {
                    inImage = true;
                    dataStream = new ByteArrayOutputStream();
                }
                // We have finished receiveing the mask list.  Process the data.
                else if ("endMaskList".equals(message)) {
                    byte[] img = dataStream.toByteArray();
                    final Bitmap decodedByte = BitmapFactory.decodeByteArray(img, 0, img.length);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setEnabled(false);
                            sendAgain.setEnabled(false);
                            chooseMask.setEnabled(true);
                            imageView.setImageBitmap(decodedByte);
                        }
                    });
                    inImage = false;

                    // process image
                }
                // We have finished receiving the final product.  Parse it and display it.
                else if ("endStyle".equals(message)) {
                    byte[] img = dataStream.toByteArray();
                    final Bitmap decodedByte = BitmapFactory.decodeByteArray(img, 0, img.length);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner.setEnabled(false);
                            imageView.setImageBitmap(decodedByte);
                        }
                    });
                    inImage = false;
                }
                // We have masks to process.
                else if (message.contains("mask_list")) {
                    String[] msks = message.split(",");
                    msks = Arrays.copyOfRange(msks, 1, msks.length);
                    masks = msks;
                }
                // Our choices have been received.
                else if (message.equals("mask_received")) {
                    // Update the UI when we know the masks have been received.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chooseMask.setEnabled(false);
                            chooseStyle.setEnabled(true);
                        }
                    });
                }
            }

            // Read images as bytes.
            @Override
            public void onMessage(ByteBuffer buffer) {
                if (inImage == true) {
                    try {
                        dataStream.write(buffer.array());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        socket.connect();
    }

    /**
     * Takes an input stream and converts it to bytes.
     *
     * @param inputStream to convert.
     * @return bytes
     * @throws IOException
     */
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
