package com.thefidebox.fidebox.view_models;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.Users;

public class UserAccountSettingsProfileViewModel extends AndroidViewModel {

    private static final String TAG = "UserAccountViewModel";

    public Context mContext=getApplication().getApplicationContext();
    private String userIdToBeSearched;
    private MutableLiveData<Users> userAccountSettingsMutableLiveData;
    private MutableLiveData<String> letterCount=new MutableLiveData<>();
    private MutableLiveData<String> commentCount=new MutableLiveData<>();

    public UserAccountSettingsProfileViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Users> getUserAccountSettings() {

        Log.d(TAG, "getUserAccountSettings: ");
        if (userAccountSettingsMutableLiveData == null) {

            userAccountSettingsMutableLiveData = new MutableLiveData<>();
            loadUserAccountSettings();
        }

        return userAccountSettingsMutableLiveData;
    }

    public LiveData<String> getLetterCount() {
        return letterCount;
    }

    public LiveData<String> getCommentCount() {
        return commentCount;
    }

    public void setUserIdToBeSearched(String userIdToBeSearched) {
        this.userIdToBeSearched = userIdToBeSearched;
    }

    public void loadUserAccountSettings() {

        Log.d(TAG, "loadUserAccountSettings: ");
        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.users))
                .document(userIdToBeSearched)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                               if (task.isSuccessful()) {
                                                   DocumentSnapshot document = task.getResult();
                                                   if (document.exists()) {
                                                       Log.d(TAG, "gettingDocument: ");
                                                       Users userAccountSettings=document.toObject(Users.class);
                                                       userAccountSettingsMutableLiveData.setValue(userAccountSettings);

                                                   }
                                               }
                                           }
                                       }
                );


        //gettingCommentCount
        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.users))
                .document(userIdToBeSearched)
                .collection("--stat--")
                .document("comments")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "gettingDocument1: "+document.getData());

                            if (document.exists()) {
                                String a =document.get(mContext.getString(R.string.field_count)).toString();
                                commentCount.setValue(a);
                            }

                        } else {
                            Log.d(TAG, "Cached get failed: ", task.getException());
                        }
                    }
                });


        //getting letterCount
        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.users))
                .document(userIdToBeSearched)
                .collection("--stat--")
                .document("CVs")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot document = task.getResult();
                            Log.d(TAG, "gettingDocument2: "+document.getData());
                            if (document.exists()) {

                                String a =document.get(mContext.getString(R.string.field_count)).toString();
                                letterCount.setValue(a);
                            }

                        } else {
                            Log.d(TAG, "Cached get failed: ", task.getException());
                        }
                    }
                });
    }
}
