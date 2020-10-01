package com.thefidebox.fidebox.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserPrivateInfo implements Parcelable {
    private String email;
    private String userId;

    public UserPrivateInfo() {

    }

    public UserPrivateInfo(String email, String userId) {
        this.email = email;
        this.userId = userId;
    }

    protected UserPrivateInfo(Parcel in) {
        email = in.readString();
        userId = in.readString();
    }

    public static final Creator<UserPrivateInfo> CREATOR = new Creator<UserPrivateInfo>() {
        @Override
        public UserPrivateInfo createFromParcel(Parcel in) {
            return new UserPrivateInfo(in);
        }

        @Override
        public UserPrivateInfo[] newArray(int size) {
            return new UserPrivateInfo[size];
        }
    };

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(userId);
    }
}
