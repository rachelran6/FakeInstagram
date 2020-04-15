package com.example.fakeinsta;

public class Comments {
    private String uid = "";
    private String username = "";
    private String profileRef = "";
    private String timestamp = "";
    private String comment = "";
    private String photoRef = "";

    public Comments(String uid, String username, String profileRef, String timestamp,  String comment, String photoRef) {
        this.uid = uid;
        this.username = username;
        this.profileRef = profileRef;
        this.timestamp = timestamp;
        this.comment = comment;
        this.photoRef = photoRef;
    }

    public Comments(){

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileRef() {
        return profileRef;
    }

    public void setProfileRef(String profileRef) {
        this.profileRef = profileRef;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPhotoRef() {
        return photoRef;
    }

    public void setPhotoRef(String photoRef) {
        this.photoRef = photoRef;
    }

}
