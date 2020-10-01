package com.thefidebox.fidebox.models;

import android.os.Parcel;
import android.os.Parcelable;


import com.google.firebase.Timestamp;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


@IgnoreExtraProperties
public class CV implements Parcelable {

    private String troll_picture;
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
    private int giggles;
    private int comments;
    private @ServerTimestamp
    Timestamp timestamp;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(troll_picture);
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
        dest.writeInt(giggles);
        dest.writeInt(comments);
        dest.writeParcelable(timestamp, flags);
    }

    public CV (){

    }

    public CV(String troll_picture, String name, String position,
              String category, String date1, String failure1,
              String date2, String failure2, String date3, String failure3,
              String cvId, String userId, int giggles, int comments, Timestamp timestamp) {
        this.troll_picture = troll_picture;
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
        this.giggles = giggles;
        this.comments = comments;
        this.timestamp = timestamp;
    }

    protected CV(Parcel in) {
        troll_picture = in.readString();
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
        giggles = in.readInt();
        comments = in.readInt();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<CV> CREATOR = new Creator<CV>() {
        @Override
        public CV createFromParcel(Parcel in) {
            return new CV(in);
        }

        @Override
        public CV[] newArray(int size) {
            return new CV[size];
        }
    };

    public String getTroll_picture() {
        return troll_picture;
    }

    public void setTroll_picture(String troll_picture) {
        this.troll_picture = troll_picture;
    }

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

    public int getGiggles() {
        return giggles;
    }

    public void setGiggles(int giggles) {
        this.giggles = giggles;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CV{" +
                "troll_picture='" + troll_picture + '\'' +
                ", name='" + name + '\'' +
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
                ", giggles=" + giggles +
                ", comments=" + comments +
                ", timestamp=" + timestamp +
                '}';
    }

    protected String LetterDateDisplay(Date date){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        String dateString=sdf.format(date);

        String yr=dateString.substring(0,4);
        String month=dateString.substring(5,7);
        String day=dateString.substring(8,10);

        return day+"/"+month+"/"+yr;
    }


}
