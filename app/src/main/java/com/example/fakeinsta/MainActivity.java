package com.example.fakeinsta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query.Direction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView profileView;
    private TextView username;
    private TextView bio;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    ArrayList<PostedPhoto> photoList;
    private Switch aSwitch;

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerViewAdapter recyclerViewAdapter;

    Uri photoURI;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        photoList = new ArrayList <PostedPhoto> ();

        profileView = findViewById(R.id.profileView);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        btnLogout = findViewById(R.id.logout);
        aSwitch = findViewById(R.id.switchButton);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Toast.makeText(MainActivity.this, "Go to Global Feed",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, GlobalFeedActivity.class));
                    finish();
                } else {
                    // The toggle is disabled
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Logged Out",
                        Toast.LENGTH_SHORT).show();
                try {
                    finish();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                    mAuth.signOut();
                    getApp().setSignOutFlag(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if(mAuth.getCurrentUser() == null){
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            settingUp();
        }

    }

    private void settingUp(){
        DocumentReference documentReference = db.collection("Users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                username.setText(documentSnapshot.getString("username"));
                bio.setText(documentSnapshot.getString("bio"));
                Glide.with(MainActivity.this).load(documentSnapshot.getString("profileURL")).into(profileView);
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);

        Toast.makeText(MainActivity.this, "Loading images",
                Toast.LENGTH_LONG).show();

        reloadPhotos();
    }

    private void reloadPhotos() {
        photoList.clear();

        db.collection("Photos")
                .whereEqualTo("uid", userId).orderBy("timestamp", Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PostedPhoto postedPhoto = document.toObject(PostedPhoto.class);
                                photoList.add(postedPhoto);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                        recyclerViewAdapter = new RecyclerViewAdapter(context, photoList);
                        recyclerView.setAdapter(recyclerViewAdapter);
                        recyclerView.setHasFixedSize(true);
                    }
                });
    }

    // keep full size photos
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        getApp().setCurrentPhoto(currentPhotoPath);
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    public void dispatchTakePictureIntent(View view) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(MainActivity.this, "Error occurred while creating the File",
                        Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                getApp().setPhotoUri(photoURI);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO){
            switch (resultCode){
                case RESULT_OK:
                    Log.i(TAG, "onActivityResult: RESULT OK");
                    Bitmap bitmapConvert = null;
                    try {
                        bitmapConvert = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoURI);
                        getApp().setBitmap(bitmapConvert);
                        Intent intent = new Intent(MainActivity.this, CaptionActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case RESULT_CANCELED:
                    Log.i(TAG, "onActivityResult: RESULT CANCELLED");
                    break;
                default:
                    break;

            }
        }
    }

    public UserInformation getApp(){
        return ((UserInformation) getApplicationContext());
    }

}
