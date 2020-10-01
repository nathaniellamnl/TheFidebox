package com.thefidebox.fidebox.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;


@IgnoreExtraProperties

public class CommentLog implements Parcelable {

    private String route;
    @ServerTimestamp
    private Timestamp timestamp;
    private String comment;
    private int giggles;
    private String category;
    private String commentId;
    private String commenterId;
    private int child_comments;

    public CommentLog(String route, Timestamp timestamp, String comment, int giggles, String category, String commentId, String commenterId, int child_comments) {
        this.route = route;
        this.timestamp = timestamp;
        this.comment = comment;
        this.giggles = giggles;
        this.category = category;
        this.commentId = commentId;
        this.commenterId = commenterId;
        this.child_comments = child_comments;
    }

    public CommentLog(){

    }

    protected CommentLog(Parcel in) {
        route = in.readString();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        comment = in.readString();
        giggles = in.readInt();
        category = in.readString();
        commentId = in.readString();
        commenterId = in.readString();
        child_comments = in.readInt();
    }

    public static final Creator<CommentLog> CREATOR = new Creator<CommentLog>() {
        @Override
        public CommentLog createFromParcel(Parcel in) {
            return new CommentLog(in);
        }

        @Override
        public CommentLog[] newArray(int size) {
            return new CommentLog[size];
        }
    };

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getGiggles() {
        return giggles;
    }

    public void setGiggles(int giggles) {
        this.giggles = giggles;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public int getChild_comments() {
        return child_comments;
    }

    public void setChild_comments(int child_comments) {
        this.child_comments = child_comments;
    }

    @Override
    public String toString() {
        return "CommentLog{" +
                "route='" + route + '\'' +
                ", timestamp=" + timestamp +
                ", comment='" + comment + '\'' +
                ", giggles=" + giggles +
                ", category='" + category + '\'' +
                ", commentId='" + commentId + '\'' +
                ", commenterId='" + commenterId + '\'' +
                ", child_comments=" + child_comments +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(route);
        dest.writeParcelable(timestamp, flags);
        dest.writeString(comment);
        dest.writeInt(giggles);
        dest.writeString(category);
        dest.writeString(commentId);
        dest.writeString(commenterId);
        dest.writeInt(child_comments);
    }
}




