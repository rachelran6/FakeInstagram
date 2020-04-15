package com.example.fakeinsta;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";
    private EditText email;
    private EditText password;
    private EditText password2;
    private EditText username;
    private EditText bio;
    private Button btnSignup;

    private String strEmail = "";
    private String strPass = "";
    private String strPass2 = "";
    private String strUsername = "";
    private String strBio = "";
    private String userId = "";

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseFirestore db;
    private Boolean FLAG = true;

    // take photo
    int TAKE_IMAGE_CODE = 10001;
    ImageView imageView;
    Bitmap bitmap;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initView();
    }

    private void initView() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        btnSignup = findViewById(R.id.register);

        imageView = findViewById(R.id.image);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                register();
                break;
        }
    }

    private void register() {
        // check if the user has choose a profile image
        if(FLAG == true) {
            Toast.makeText(this, "Please take a profile photo.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            strEmail = email.getText().toString();
            strPass = password.getText().toString();
            strPass2 = password2.getText().toString();
            strUsername = username.getText().toString();
            strBio = bio.getText().toString();

            // check if the textfields are empty
            if (TextUtils.isEmpty(strEmail) | TextUtils.isEmpty(strPass) | TextUtils.isEmpty(strPass2)
                    | TextUtils.isEmpty(strUsername) | TextUtils.isEmpty(strBio)) {
                Toast.makeText(this, "Please fill all the blanks.", Toast.LENGTH_SHORT).show();
                return;
            }
            // check if password == password2
            if (!strPass.equals(strPass2)) {
                Toast.makeText(this, "Please confirm the password.", Toast.LENGTH_SHORT).show();
                password.getText().clear();
                password2.getText().clear();
                return;
            }
            // check if bio is too long
            if (strBio.length() > 100) {
                Toast.makeText(this, "Please give a shorter biography (less than 100 characters).", Toast.LENGTH_SHORT).show();
                bio.getText().clear();
                return;
            }

            Toast.makeText(RegisterActivity.this, "Signing up!",
                    Toast.LENGTH_LONG).show();

            mAuth.createUserWithEmailAndPassword(strEmail, strPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // register success, save user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                Toast.makeText(RegisterActivity.this, "Account created!",
                                        Toast.LENGTH_LONG).show();
                                Toast.makeText(RegisterActivity.this, "Loading to profile!",
                                        Toast.LENGTH_LONG).show();
                                userId = mAuth.getCurrentUser().getUid();
                                DocumentReference documentReference = db.collection("Users").document(userId);

                                // Create a new user with a first and last name
                                Map<String, Object> user = new HashMap<>();
                                user.put("email", strEmail);
                                user.put("username", strUsername);
                                user.put("bio", strBio);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot added with ID: " + userId);
                                    }
                                });

                                uploadImage(bitmap);
                                // initially false
                                FLAG = true;

                                getApp().setUserId(mAuth.getCurrentUser().getUid());

                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 3000);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE){
            switch (resultCode){
                case RESULT_OK:
                    Log.i(TAG, "onActivityResult: RESULT OK");
                    bitmap = (Bitmap) data.getExtras().get("data");
                    imageView.setImageBitmap(bitmap);
                    // to tell that the user save a profile image
                    FLAG = false;
                    break;
                case RESULT_CANCELED:
                    Log.i(TAG, "onActivityResult: RESULT CANCELLED");
                    break;
                default:
                    break;

            }
        }
    }

    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        String uid = FirebaseAuth.getInstance().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("profileImages").child(uid+".jpeg");

        reference.putBytes(byteArrayOutputStream.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getDownloadUrl(reference);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "OnFailure: ", e.getCause());
            }
        });
    }

    private void getDownloadUrl(StorageReference reference){
        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: "+uri);
                setUserProfileUrl(uri);
            }
        });
    }

    private void setUserProfileUrl(Uri uri){
        mUser = mAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        mUser.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(RegisterActivity.this, "Image uploaded!", Toast.LENGTH_SHORT).show();
                getApp().setUserProfileImage(mAuth.getCurrentUser().getPhotoUrl().toString());

                userId = mAuth.getCurrentUser().getUid();
                DocumentReference documentReference = db.collection("Users").document(userId);
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put("email", strEmail);
                userUpdate.put("username", strUsername);
                userUpdate.put("bio", strBio);
                userUpdate.put("profileURL", getApp().getUserProfileImage());
                documentReference.update(userUpdate).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + userId);
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Failed upload image!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public UserInformation getApp(){
        return ((UserInformation) getApplicationContext());
    }
}