package com.quickenloans.techconference.basicmessenger;

import java.io.Serializable;

 class Message implements Serializable {
    private String text;
    private String name;
    private String photoUrl;

     Message() {

     }

     Message(String text, String name, String photoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

}



