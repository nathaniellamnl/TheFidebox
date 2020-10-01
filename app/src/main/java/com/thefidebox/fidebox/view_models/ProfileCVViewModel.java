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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.NotificationModel;

import java.util.ArrayList;

public class ProfileCVViewModel extends AndroidViewModel {

    private static final String TAG = "ProfileCVViewModel";

    private @ServerTimestamp
    Timestamp timestamp;
    private CV lastCV, lastKeyCV;
    private ArrayList<CV> dataSet = new ArrayList<>();
    private MutableLiveData<ArrayList<CV>> mCVs;
    private MutableLiveData<ArrayList<NotificationModel>> mNotificationModels;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<Boolean> mCanLoadMore = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRefreshCV = new MutableLiveData<>();
    private Context mContext=getApplication().getApplicationContext();
    private String userIdToBeSearched;

    public ProfileCVViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ArrayList<CV>> getCVs() {
        Log.d(TAG, "getLetters: ");
        if (mCVs == null) {
            mCVs = new MutableLiveData<>();
            loadLetters();

        }
        // getLastDBLetter();
        mCVs.setValue(dataSet);
        return mCVs;

    }

    public void setUserIdToBeSearched(String userIdToBeSearched) {
        this.userIdToBeSearched = userIdToBeSearched;
    }

    public LiveData<Boolean> getIsUpdating() {

        return mIsUpdating;
    }

    public LiveData<Boolean> getRefreshLetter() {

        return mRefreshCV;
    }

    public LiveData<Boolean> getCanLoadMore() {

       /* loadMoreData();*/

        return mCanLoadMore;
    }

    public LiveData<ArrayList<CV>> getmLetters() {
        return mCVs;
    }

    public void setmLetters (ArrayList<CV> CVs){

        mCVs.setValue(CVs);
    }


    //query data/retrieve by accessing the cache
    public void loadLetters() {

        Log.d(TAG, "loadLetters: ");

        if(lastKeyCV==null){
            getLastKeyCV();
        }

        Query query;
        CollectionReference collectionReference=FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(userIdToBeSearched)
                .collection(mContext.getString(R.string.col_cvs));

        try{
            if(lastCV.getCvId().equals(lastKeyCV.getCvId())){
                mCanLoadMore.setValue(false);
                return;
            }
        } catch (NullPointerException e){

        }

        if(lastCV==null){
            query=collectionReference
                    .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                    .limit(10);
            mIsUpdating.setValue(true);
        } else{
            query=collectionReference
                    .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                    .startAfter(lastCV.getTimestamp())
                    .limit(10);
        }

        try {
                    query.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());

                                    CV CV = document.toObject(CV.class);

                                    dataSet.add(CV);

                                }

                                mCVs.setValue(dataSet);
                                try {
                                    lastCV = dataSet.get(dataSet.size() - 1);
                                } catch (IndexOutOfBoundsException e) {
                                    //no CV
                                }

                                mIsUpdating.setValue(false);
                            } else {
                                mIsUpdating.setValue(false);
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

        } catch (NullPointerException e) {
            mIsUpdating.setValue(false);
            Log.d(TAG, "setupRecyclerView: Null");
        }

    }


    public void getLastKeyCV(){

        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(userIdToBeSearched)
                .collection(mContext.getString(R.string.col_cvs))
                .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                lastKeyCV=document.toObject(CV.class);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    public void refresh() {
        mRefreshCV.setValue(true);

                CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                        .document(userIdToBeSearched)
                        .collection(mContext.getString(R.string.col_cvs));

                        db
                        .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "RefreshLayout: ");
                                    dataSet.clear();
                                    mCVs.getValue().clear();
                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        Log.d(TAG, document.getId() + " => " + document.getData());

                                        CV CV = document.toObject(CV.class);
                                        dataSet.add(CV);
                                    }

                                    mCVs.setValue(dataSet);
                                    mRefreshCV.setValue(false);

                                    try {
                                        lastCV = dataSet.get(dataSet.size() - 1);
                                    } catch (IndexOutOfBoundsException e) {
                                        //no draft letter in the draft box
                                    }
                                } else {
                                    //amend: show no internet
                                    mRefreshCV.setValue(false);
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });


    }


}



