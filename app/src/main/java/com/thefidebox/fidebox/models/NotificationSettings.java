package com.thefidebox.fidebox.models;

public class NotificationSettings {

    private boolean change_on_cv;
    private boolean change_on_comment;
    private String userId;


    public NotificationSettings(){

    }

    public NotificationSettings(boolean change_on_cv, boolean change_on_comment, String userId) {
        this.change_on_cv = change_on_cv;
        this.change_on_comment = change_on_comment;
        this.userId = userId;
    }

    public boolean isChange_on_cv() {
        return change_on_cv;
    }

    public void setChange_on_cv(boolean change_on_cv) {
        this.change_on_cv = change_on_cv;
    }

    public boolean isChange_on_comment() {
        return change_on_comment;
    }

    public void setChange_on_comment(boolean change_on_comment) {
        this.change_on_comment = change_on_comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "NotificationSettings{" +
                "change_on_cv=" + change_on_cv +
                ", change_on_comment=" + change_on_comment +
                ", userId='" + userId + '\'' +
                '}';
    }
}
