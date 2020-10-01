package com.thefidebox.fidebox.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

@IgnoreExtraProperties
public class DraftCV implements Parcelable {

//    private String troll_picture;
    private String name;
    private String position;
    private String category;
    private String date1;
    private String failure1;
    private String date2;
    private String failure2;
    private String date3;
    private String failure3;
    private String cvId;
    private String userId;
    private @ServerTimestamp
    Timestamp timestamp;

    public DraftCV(){

    }

    public DraftCV( String name, String position,
                   String category, String date1, String failure1, String date2,
                   String failure2, String date3, String failure3, String cvId,
                   String userId, Timestamp timestamp) {
//        this.troll_picture = troll_picture;
        this.name = name;
        this.position = position;
        this.category = category;
        this.date1 = date1;
        this.failure1 = failure1;
        this.date2 = date2;
        this.failure2 = failure2;
        this.date3 = date3;
        this.failure3 = failure3;
        this.cvId = cvId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    protected DraftCV(Parcel in) {
//        troll_picture = in.readString();
        name = in.readString();
        position = in.readString();
        category = in.readString();
        date1 = in.readString();
        failure1 = in.readString();
        date2 = in.readString();
        failure2 = in.readString();
        date3 = in.readString();
        failure3 = in.readString();
        cvId = in.readString();
        userId = in.readString();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<DraftCV> CREATOR = new Creator<DraftCV>() {
        @Override
        public DraftCV createFromParcel(Parcel in) {
            return new DraftCV(in);
        }

        @Override
        public DraftCV[] newArray(int size) {
            return new DraftCV[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(troll_picture);
        dest.writeString(name);
        dest.writeString(position);
        dest.writeString(category);
        dest.writeString(date1);
        dest.writeString(failure1);
        dest.writeString(date2);
        dest.writeString(failure2);
        dest.writeString(date3);
        dest.writeString(failure3);
        dest.writeString(cvId);
        dest.writeString(userId);
        dest.writeParcelable(timestamp, flags);
    }
/*
    public String getTroll_picture() {
        return troll_picture;
    }

    public void setTroll_picture(String troll_picture) {
        this.troll_picture = troll_picture;
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate1() {
        return date1;
    }

    public void setDate1(String date1) {
        this.date1 = date1;
    }

    public String getFailure1() {
        return failure1;
    }

    public void setFailure1(String failure1) {
        this.failure1 = failure1;
    }

    public String getDate2() {
        return date2;
    }

    public void setDate2(String date2) {
        this.date2 = date2;
    }

    public String getFailure2() {
        return failure2;
    }

    public void setFailure2(String failure2) {
        this.failure2 = failure2;
    }

    public String getDate3() {
        return date3;
    }

    public void setDate3(String date3) {
        this.date3 = date3;
    }

    public String getFailure3() {
        return failure3;
    }

    public void setFailure3(String failure3) {
        this.failure3 = failure3;
    }

    public String getCvId() {
        return cvId;
    }

    public void setCvId(String cvId) {
        this.cvId = cvId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DraftCV{" +
//                "troll_picture='" + troll_picture + '\'' +
                "name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", category='" + category + '\'' +
                ", date1='" + date1 + '\'' +
                ", failure1='" + failure1 + '\'' +
                ", date2='" + date2 + '\'' +
                ", failure2='" + failure2 + '\'' +
                ", date3='" + date3 + '\'' +
                ", failure3='" + failure3 + '\'' +
                ", cvId='" + cvId + '\'' +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }


}
