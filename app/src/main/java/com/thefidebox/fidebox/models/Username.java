package com.thefidebox.fidebox.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Username implements Parcelable {
    private String username;
    private String userId;

    public Username(){

    }

    public Username(String username, String userId) {
        this.username = username;
        this.userId = userId;
    }

    protected Username(Parcel in) {
        username = in.readString();
        userId = in.readString();
    }

    public static final Creator<Username> CREATOR = new Creator<Username>() {
        @Override
        public Username createFromParcel(Parcel in) {
            return new Username(in);
        }

        @Override
        public Username[] newArray(int size) {
            return new Username[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Username{" +
                "username='" + username + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(userId);
    }


}
