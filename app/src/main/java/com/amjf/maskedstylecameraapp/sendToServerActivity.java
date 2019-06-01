package com.amjf.maskedstylecameraapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.maskedstylecameraapp.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;


public class sendToServerActivity extends AppCompatActivity {

    private Uri storedPhoto;
    private WebSocketClient socket;
    private Button sendAgain;

    // These are to hardcoded to quickly get this working.  Not Planned for production.
    String address = "ws://10.246.251.255:8765";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_server);

        sendAgain = (Button) findViewById(R.id.Retry);
        sendAgain.setEnabled(false);

        //Get the current photo uri from Main Activity.
        Intent intent = getIntent();
        storedPhoto = Uri.parse(intent.getStringExtra("image"));
        makeConnections();

        sendAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAgain.setEnabled(false);
                makeConnections();
            }
        });

    }

    /**
     * Handles all of the steps needed to connect to the server
     *  In order to get this to work in a resonalble amount of time, we decied to use a server
     *  to process the Networks.  The list of reasonings and other options can be found in our report
     *  The flow of the network should go as follows.
     *      * 1. Connect to the server and send the image.
     *      * 2. After some time receive a new image.
     *      * 3. Send a response back to the server to say that you got the image.
     *      * 4. Receive a list of mask names. (CSV)
     *      * 5. send a list of mask names (CSV)
     *      * 6. Receive a response from the Server.
     *      * 7. Send the chosen style name
     *      * 8. After some time receive the resulting image from the server.
     *      * 9. Close the connection.  Save the image.
     */
    private void makeConnections() {
        connectWebSocket();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
        try {
            socket.send(inputData);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            alertWithMessage("Failed to send initial image");
            sendAgain.setEnabled(true);
            return;
        }
        sendAgain.setEnabled(true);

    }

    /**
     * When a failure occurs, we alert the user with a message.
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
     *Web socket logic.  How web sockets are handled.
     */
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        socket = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(message);
                    }
                });
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


