package com.example.mm.firebasestoragedatabase.models;

public class UploadObject {

    private String mName;
    private String mImageUrl;

    public UploadObject() {
        // Empty Constructor
    }

    public UploadObject(String name, String imageUrl) {

        if (name.trim().equals("")){
            name = "No Name";
        }

        this.mName = name;
        this.mImageUrl = imageUrl;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }
}
