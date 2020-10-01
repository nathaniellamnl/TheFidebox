package com.thefidebox.fidebox.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;


@IgnoreExtraProperties
 public class Comment implements Parcelable {
    private String comment;
    private String commentId;
    private String commenterId;
    private int giggles;
    @ServerTimestamp private Timestamp timestamp;
    private String cvId;
    private String category;
    private int child_comments;

    protected Comment(Parcel in) {
        comment = in.readString();
        commentId = in.readString();
        commenterId = in.readString();
        giggles = in.readInt();
        timestamp = in.readParcelable(Timestamp.class.getClassLoader());
        cvId = in.readString();
        category = in.readString();
        child_comments = in.readInt();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(comment);
        dest.writeString(commentId);
        dest.writeString(commenterId);
        dest.writeInt(giggles);
        dest.writeParcelable(timestamp, flags);
        dest.writeString(cvId);
        dest.writeString(category);
        dest.writeInt(child_comments);
    }

    //letter title
    //token

    //child_comments as int to save database space

    public Comment(){

    }

    public Comment(String comment, String commentId, String commenterId, int giggles, Timestamp timestamp, String cvId, String category, int child_comments) {
        this.comment = comment;
        this.commentId = commentId;
        this.commenterId = commenterId;
        this.giggles = giggles;
        this.timestamp = timestamp;
        this.cvId = cvId;
        this.category = category;
        this.child_comments = child_comments;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public int getGiggles() {
        return giggles;
    }

    public void setGiggles(int giggles) {
        this.giggles = giggles;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getCvId() {
        return cvId;
    }

    public void setCvId(String cvId) {
        this.cvId = cvId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getChild_comments() {
        return child_comments;
    }

    public void setChild_comments(int child_comments) {
        this.child_comments = child_comments;
    }

    public static Creator<Comment> getCREATOR() {
        return CREATOR;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "comment='" + comment + '\'' +
                ", commentId='" + commentId + '\'' +
                ", commenterId='" + commenterId + '\'' +
                ", giggles=" + giggles +
                ", timestamp=" + timestamp +
                ", cvId='" + cvId + '\'' +
                ", category='" + category + '\'' +
                ", child_comments=" + child_comments +
                '}';
    }
}




