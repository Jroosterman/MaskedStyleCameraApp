package com.amjf.maskedstylecameraapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.maskedstylecameraapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class sendToServerActivity extends AppCompatActivity {

    private Uri storedPhoto;
    private Socket socket;

    // These are to hardcoded to quickly get this working.  Not Planned for production.
    String serverIpAddress = "10.0.2.2";
    int port = 8765;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_server);

        //Get the current photo uri from Main Activity.
        Intent intent = getIntent();
        storedPhoto = Uri.parse(intent.getStringExtra("image"));

        Thread cThread = new Thread(new ClientThread());
        cThread.start();
    }

    /**
     * In order to get this to work in a resonalble amount of time, we decied to use a server
     * to process the Networks.  The list of reasonings and other options can be found in our report
     * The flow of the network should go as follows.
     * 1. Connect to the server and send the image.
     * 2. After some time receive a new image.
     * 3. Send a response back to the server to say that you got the image.
     * 4. Receive a list of mask names. (CSV)
     * 5. send a list of mask names (CSV)
     * 6. Receive a response from the Server.
     * 7. Send the chosen style name
     * 8. After some time receive the resulting image from the server.
     * 9. Close the connection.  Save the image.
     */
    public class ClientThread implements Runnable {

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                socket = new Socket(serverAddr, port);
                try {

                    OutputStream output = socket.getOutputStream();
                    //InputStream iStream =   getContentResolver().openInputStream(storedPhoto);
                    //byte[] imgBytes = getBytes(iStream);
                    String f = "Frank";
                    output.write(f.getBytes());
                    output.flush();

                    InputStream input = socket.getInputStream();
                    byte[] b = new byte[100];
                    input.read(b);

                    System.out.println(b.toString());

                } catch (Exception e) {
                    Log.e("ClientActivity", "S: Error", e);
                }

            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch(
        Exception e)

        {
            Log.e("ClientActivity", "C: Error", e);
        }
    }

        /**
         * Takes an input stream and converts it to bytes.
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

}
