package com.thefidebox.fidebox.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Users implements Parcelable{
    private String userId;
    private String display_name;
    private String username;
    private String bio;
    private String profile_photo;

    public Users(){

    }

    public Users(String userId, String display_name, String username, String bio, String profile_photo) {
        this.userId = userId;
        this.display_name = display_name;
        this.username = username;
        this.bio = bio;
        this.profile_photo = profile_photo;
    }

    protected Users(Parcel in) {
        userId = in.readString();
        display_name = in.readString();
        username = in.readString();
        bio = in.readString();
        profile_photo = in.readString();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    @Override
    public String toString() {
        return "Users{" +
                "userId='" + userId + '\'' +
                ", display_name='" + display_name + '\'' +
                ", username='" + username + '\'' +
                ", bio='" + bio + '\'' +
                ", profile_photo='" + profile_photo + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(display_name);
        dest.writeString(username);
        dest.writeString(bio);
        dest.writeString(profile_photo);
    }
}
