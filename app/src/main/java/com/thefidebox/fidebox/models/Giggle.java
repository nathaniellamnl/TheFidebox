package com.thefidebox.fidebox.models;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Giggle {

    private String userId;

    public Giggle(){

    }

    public Giggle(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Giggle{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
