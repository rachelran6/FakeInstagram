package com.example.fakeinsta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CommentActivity extends AppCompatActivity {

    private static final String TAG = "CommentActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private String imageUrl;
    private String caption;
    private String checkUid;
    private Button btnSend;
    private Button btnDelete;
    private Button btnBack;
    private String timeStamp;
    private String comments;
    private EditText commentEditText;
    private String commentUsername;
    private String userProfileUrl;

    ArrayList<Comments> commentsList;
    ArrayList<String> photoID;
    ArrayList<String> commentsID;

    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CommentViewAdapter commentViewAdapter;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        if(getIntent().hasExtra("image_url")){
            imageUrl = getIntent().getStringExtra("image_url");
        }

        context = CommentActivity.this;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        commentsList = new ArrayList <Comments> ();
        photoID = new ArrayList<String>();
        commentsID = new ArrayList<String>();

        commentEditText = findViewById(R.id.editComments);
        btnSend = findViewById(R.id.btnComments);
        btnDelete = findViewById(R.id.btnDeletePost);
        btnBack = findViewById(R.id.btnBack);

        recyclerView = findViewById(R.id.commentRecyclerView);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);

        Toast.makeText(CommentActivity.this, "Loading comments",
                Toast.LENGTH_LONG).show();

        reloadComments();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comments = commentEditText.getText().toString();
                if (TextUtils.isEmpty(comments)){
                    Toast.makeText(CommentActivity.this, "Please fill the comment.", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (comments.length() > 200) {
                    Toast.makeText(CommentActivity.this, "Please give a shorter comment (less than 200 characters).", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    Toast.makeText(CommentActivity.this, "Comment submitted",
                            Toast.LENGTH_SHORT).show();
                    uploadComments();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePost();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void deletePost() {
        if(btnDelete.isEnabled()){
            Toast.makeText(CommentActivity.this, "Delete post",
                    Toast.LENGTH_SHORT).show();

            db.collection("Photos").document(photoID.get(0))
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });

            for (int i=0; i<commentsID.size(); i++){
                db.collection("Comments").document(commentsID.get(i))
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
            }

            Intent intent = new Intent(CommentActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(CommentActivity.this, "You cannot delete this post.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void reloadComments() {
        photoID.clear();
        commentsID.clear();
        commentsList.clear();

        db.collection("Photos")
                .whereEqualTo("storageRef", imageUrl)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PostedPhoto postedPhoto = document.toObject(PostedPhoto.class);
                                photoID.add(document.getId());
                                caption = postedPhoto.getCaption();
                                checkUid = postedPhoto.getUid();
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            if(userId.equals(checkUid)){
                                btnDelete.setEnabled(true);
                            }

                            db.collection("Comments")
                                    .whereEqualTo("photoRef", imageUrl).orderBy("timestamp", Query.Direction.ASCENDING)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    Comments comments = document.toObject(Comments.class);
                                                    commentsID.add(document.getId());
                                                    commentsList.add(comments);
                                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                                }
                                            } else {
                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                            }

                                            commentViewAdapter = new CommentViewAdapter(context, commentsList, caption, imageUrl);
                                            recyclerView.setAdapter(commentViewAdapter);
                                            recyclerView.setHasFixedSize(true);
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    private void uploadComments(){
        comments = commentEditText.getText().toString();
        DocumentReference documentReference = db.collection("Users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                commentUsername = documentSnapshot.getString("username");
                userProfileUrl = documentSnapshot.getString("profileURL");

                timeStamp = String.valueOf(System.currentTimeMillis());

                Map<String, Object> comment = new HashMap<>();
                comment.put("uid", userId);
                comment.put("username", commentUsername);
                comment.put("profileRef", userProfileUrl);
                comment.put("timestamp", timeStamp);
                comment.put("comment", comments);
                comment.put("photoRef", imageUrl);

                db.collection("Comments")
                        .add(comment)
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
        });

        commentEditText.setText("");
        reloadComments();

    }

    public UserInformation getApp(){
        return ((UserInformation) getApplicationContext());
    }

}
