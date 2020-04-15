package com.example.fakeinsta;

public class PostedPhoto {
    public PostedPhoto(String uid, String storageRef, String timestamp, String caption) {
        this.uid = uid;
        this.storageRef = storageRef;
        this.timestamp = timestamp;
        this.caption = caption;
    }

    public PostedPhoto(){

    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStorageRef() {
        return storageRef;
    }

    public void setStorageRef(String storageRef) {
        this.storageRef = storageRef;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    private String uid = "";
    private String storageRef = "";
    private String timestamp = "";
    private String caption = "";

}
