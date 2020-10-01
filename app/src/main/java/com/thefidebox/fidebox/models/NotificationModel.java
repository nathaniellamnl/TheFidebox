package com.thefidebox.fidebox.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Objects;


@IgnoreExtraProperties
public class NotificationModel implements Parcelable {
    private static final String TAG = "NotificationModel";
    private String body;
    private String cvId;
    private String type;
    private String userId;


    public NotificationModel() {

    }

    public NotificationModel(String body, String cvId, String type, String userId) {
        this.body = body;
        this.cvId = cvId;
        this.type = type;
        this.userId = userId;
    }

    protected NotificationModel(Parcel in) {
        body = in.readString();
        cvId = in.readString();
        type = in.readString();
        userId = in.readString();
    }

    public static final Creator<NotificationModel> CREATOR = new Creator<NotificationModel>() {
        @Override
        public NotificationModel createFromParcel(Parcel in) {
            return new NotificationModel(in);
        }

        @Override
        public NotificationModel[] newArray(int size) {
            return new NotificationModel[size];
        }
    };

    public static String getTAG() {
        return TAG;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCvId() {
        return cvId;
    }

    public void setCvId(String cvId) {
        this.cvId = cvId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "NotificationModel{" +
                "body='" + body + '\'' +
                ", cvId='" + cvId + '\'' +
                ", type='" + type + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(body);
        dest.writeString(cvId);
        dest.writeString(type);
        dest.writeString(userId);
    }
}
