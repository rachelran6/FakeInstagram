package com.example.fakeinsta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CaptionActivity extends AppCompatActivity {

    private static final String TAG = "AddImage";
    private ImageView imageView;
    private Button btnCancel;
    private Button btnSubmit;
    private EditText caption;
    private Switch aSwitch;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String timeStamp = "";
    private String captionStr;
    private String currentPhotoPath;
    private Boolean enableHashtag = false;
    private String hashtags = "";
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        imageView = findViewById(R.id.captionImage);
        btnCancel = findViewById(R.id.captionCancel);
        btnSubmit = findViewById(R.id.captionSubmit);
        caption = findViewById(R.id.caption);
        aSwitch = findViewById(R.id.enableButton);

        imageView.setImageBitmap(getApp().getBitmap());

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Toast.makeText(CaptionActivity.this, "Getting hashtags",
                            Toast.LENGTH_SHORT).show();
                    enableHashtag = true;
                    autoHashtags(enableHashtag);
                } else {
                    // The toggle is disabled
                    Toast.makeText(CaptionActivity.this, "Cancelling hashtags",
                            Toast.LENGTH_SHORT).show();
                    enableHashtag = false;
                    autoHashtags(enableHashtag);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CaptionActivity.this, "Cancelled",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CaptionActivity.this, MainActivity.class));
                finish();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captionStr = caption.getText().toString();
                if (TextUtils.isEmpty(captionStr)){
                    Toast.makeText(CaptionActivity.this, "Please fill the caption.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (captionStr.length() > 200) {
                    Toast.makeText(CaptionActivity.this, "Please give a shorter caption (less than 200 characters).", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{

                    try {
                        uploadImage(getApp().getBitmap());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(CaptionActivity.this, "Submitted",
                            Toast.LENGTH_SHORT).show();

                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            startActivity(new Intent(CaptionActivity.this, MainActivity.class));
                            finish();
                        }
                    }, 3000);

                }
            }
        });

    }

    private void autoHashtags(Boolean enableHashtag){
        hashtags = "";
        if (enableHashtag==true){
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(getApp().getBitmap());

            // Or, to set the minimum confidence required:
            FirebaseVisionOnDeviceImageLabelerOptions options =
                    new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                            .setConfidenceThreshold(0.7f)
                            .build();
            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                    .getOnDeviceImageLabeler(options);

            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            // Task completed successfully
                            for (FirebaseVisionImageLabel label: labels) {
                                String text = label.getText();
                                String entityId = label.getEntityId();
                                float confidence = label.getConfidence();
                                Log.e(TAG, "Hashtags: "+entityId+"  ** text:"+text+" ** confidence"+confidence);
                                hashtags = hashtags + " #" + text;
                            }
                            captionStr = caption.getText().toString();
                            caption.setText(captionStr+hashtags);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            Toast.makeText(CaptionActivity.this, "Failed to get hashtags",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else{
            captionStr = caption.getText().toString();
            if( captionStr.contains("#") ){
                String[] origin = captionStr.split(" #");
                caption.setText(origin[0]);
            }
        }
    }

    private void uploadImage(Bitmap bitmap) throws IOException {
        // after user took a picture, cut image to square and downscale the image to 1024*1024,
        Bitmap square = null;

        if (bitmap.getWidth() >= bitmap.getHeight()){

            square = Bitmap.createBitmap(
                    bitmap,
                    bitmap.getWidth()/2 - bitmap.getHeight()/2,
                    0,
                    bitmap.getHeight(),
                    bitmap.getHeight()
            );

        }else{

            square = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.getHeight()/2 - bitmap.getWidth()/2,
                    bitmap.getWidth(),
                    bitmap.getWidth()
            );
        }

        currentPhotoPath = getApp().getCurrentPhoto();
        ExifInterface ei = new ExifInterface(currentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(square, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(square, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(square, 270);
                break;

            default:
                rotatedBitmap = square;
        }

        Bitmap finalBitmap = Bitmap.createScaledBitmap(rotatedBitmap, 1024, 1024, true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        userId = FirebaseAuth.getInstance().getUid();
        timeStamp = String.valueOf(System.currentTimeMillis());
        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("Photos").child(userId+"/"+timeStamp+".jpeg");

        reference.putBytes(byteArrayOutputStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(CaptionActivity.this, "Image Posted",
                        Toast.LENGTH_SHORT).show();
                getDownloadUrl(reference);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: ", e.getCause());
            }
        });

    }

    public static Bitmap rotateImage(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: "+uri);
                addUserPhotos(uri);
            }
        });
    }

    private void addUserPhotos(Uri uri){
        // keep a link in firebase database
        captionStr = caption.getText().toString();
        Map<String, Object> photo = new HashMap<>();
        photo.put("uid", userId);
        photo.put("storageRef", String.valueOf(uri));
        photo.put("timestamp", timeStamp);
        photo.put("caption", captionStr);

        db.collection("Photos")
                .add(photo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    public UserInformation getApp(){
        return ((UserInformation) getApplicationContext());
    }

}
