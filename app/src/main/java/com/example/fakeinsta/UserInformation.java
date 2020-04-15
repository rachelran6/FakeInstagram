package com.example.fakeinsta;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

public class UserInformation extends Application{

    private Boolean signOutFlag = false;

    private String currentPhoto = "";

    private String userId = "";

    private String userProfileImage = "";

    private Bitmap bitmap;

    private Uri photoUri;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getCurrentPhoto() {
        return currentPhoto;
    }

    public void setCurrentPhoto(String currentPhoto) {
        this.currentPhoto = currentPhoto;
    }


    public Uri getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public Boolean getSignOutFlag() {
        return signOutFlag;
    }

    public void setSignOutFlag(Boolean signOutFlag) {
        this.signOutFlag = signOutFlag;
    }

}
