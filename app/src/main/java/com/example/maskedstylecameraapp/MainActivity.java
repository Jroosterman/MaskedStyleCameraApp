package com.example.maskedstylecameraapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Activity for the class. Handles most of the main photo taking activities.
 */
public class MainActivity extends AppCompatActivity {
    //Button on the activity_mail.xml
    private Button btnCapture;

    //Location to display resulting image.
    private ImageView imgCapture;

    //ID of standard android calls.
    static final int REQUEST_TAKE_PHOTO = 1;

    // stored location of image after being taken.
    private Uri picUri;

    /**
     * When you create the activity, set up these values.
     * @param savedInstanceState instance of the current state of the applicaiton.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCapture =(Button)findViewById(R.id.btnTakePicture);
        imgCapture = (ImageView) findViewById(R.id.capturedImage);
        //Set up the button action in classic java UI Way.
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    /**
     * Action happens when you finish an activity.
     * @param requestCode Request code of the activity.
     * @param resultCode result of the activity.
     * @param data data associated with the activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If you are taking a photo.
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // If the result was successful.
            if (resultCode == RESULT_OK) {
                imgCapture.setImageURI(picUri);
            }
            // If the result was unsuccessful.
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Handles the action of setting up the camera to take a picture.
     */
    private void dispatchTakePictureIntent() {
        // Creates the image capture intent.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                picUri = Uri.fromFile(photoFile);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                // Update the event with the file location and start the event.
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Create a image to store the taken image to so we can look it up later.
     * @return File with the image path.
     * @throws IOException if we fail to get the file.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }
}