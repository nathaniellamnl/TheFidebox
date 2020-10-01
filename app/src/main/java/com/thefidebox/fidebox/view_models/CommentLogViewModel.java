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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.CommentLog;

import java.util.ArrayList;

public class CommentLogViewModel extends AndroidViewModel {

    private static final String TAG = "commentViewModel";

    private @ServerTimestamp
    Timestamp timestamp;
    private CommentLog lastCommentLog, lastKeyCommentLog;
    private ArrayList<CommentLog> dataSet = new ArrayList<>();
    private MutableLiveData<ArrayList<CommentLog>> mCommentLogs;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<Boolean> mCanLoadMore = new MutableLiveData<>();
    private MutableLiveData<Boolean> mRefreshCommentLog = new MutableLiveData<>();
    private Context mContext = getApplication().getApplicationContext();
    private String userIdToBeSearched;

    public CommentLogViewModel(@NonNull Application application) {
        super(application);
    }


    public LiveData<ArrayList<CommentLog>> getCommentLogs() {
        Log.d(TAG, "getcomments: ");
        if (mCommentLogs == null) {
            mCommentLogs = new MutableLiveData<>();
            loadCommentLogs();

        }
        // getLastDBcomment();
        mCommentLogs.setValue(dataSet);
        return mCommentLogs;

    }

    public void setmCommentLogs(ArrayList<CommentLog> commentLogs) {

        mCommentLogs.setValue(commentLogs);
    }

    public void setUserIdToBeSearched(String userIdToBeSearched) {
        this.userIdToBeSearched = userIdToBeSearched;
    }

    public LiveData<Boolean> getIsUpdating() {

        return mIsUpdating;
    }

    public LiveData<Boolean> getCanLoadMore() {

        loadMoreData();

        return mCanLoadMore;
    }

    public LiveData<ArrayList<CommentLog>> getmCommentLogs() {
        return mCommentLogs;
    }

    public LiveData<Boolean> getRefreshComment() {

        return mRefreshCommentLog;
    }


    //query data/retrieve by accessing the cache
    private void loadCommentLogs() {

        Log.d(TAG, "loadcomments: ");

        mIsUpdating.setValue(true);
        try {
            CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                    .document(userIdToBeSearched)
                    .collection(mContext.getString(R.string.col_comments));

            db
                    .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());

                                    CommentLog commentLog = document.toObject(CommentLog.class);

                                    dataSet.add(commentLog);
                                }
                                try {
                                    lastCommentLog = dataSet.get(dataSet.size() - 1);
                                } catch (IndexOutOfBoundsException e) {
                                    //no draft comment in the draft box
                                }
                                mCommentLogs.setValue(dataSet);
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


    private void loadMoreData() {

        if (lastKeyCommentLog == null || lastCommentLog == null) {
            mCanLoadMore.setValue(true);

        } else if (!lastCommentLog.getCommentId().equals(lastKeyCommentLog.getCommentId())) {
            mCanLoadMore.setValue(true);

        } else if (lastCommentLog.getCommentId().equals(lastKeyCommentLog.getCommentId())) {
            mCanLoadMore.setValue(false);
        }

        Log.d(TAG, "loadMoreData: Draftcomment");

        final CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(userIdToBeSearched)
                .collection(mContext.getString(R.string.col_comments));

        //consider fetching only from the server
        db
                .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                           @Override
                                           public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                               if (task.isSuccessful()) {
                                                   for (QueryDocumentSnapshot document : task.getResult()) {
                                                       lastKeyCommentLog = document.toObject(CommentLog.class);

                                                       if (lastKeyCommentLog.getCommentId().equals(lastCommentLog.getCommentId())) {
                                                           mCanLoadMore.setValue(false);

                                                       } else {
                                                           db.
                                                                   orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                                                                   .limit(10)
                                                                   .startAfter(lastCommentLog.getTimestamp())
                                                                   .get()
                                                                   .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                           if (task.isSuccessful()) {
                                                                               for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                   CommentLog commentLog = document.toObject(CommentLog.class);
                                                                                   dataSet.add(commentLog);
                                                                                   mCommentLogs.setValue(dataSet);
                                                                               }

                                                                               try {

                                                                                   lastCommentLog = dataSet.get(dataSet.size() - 1);

                                                                               } catch (IndexOutOfBoundsException e) {
                                                                                   mCanLoadMore.setValue(false);
                                                                               }

                                                                               mCanLoadMore.setValue(false);

                                                                           } else {

                                                                           }
                                                                       }
                                                                   });
                                                       }
                                                   }

                                               } else {
                                                   Log.d(TAG, "get failed with ", task.getException());
                                               }
                                           }
                                       }
                );


    }


    public void refresh() {
        mRefreshCommentLog.setValue(true);

        CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(userIdToBeSearched)
                .collection(mContext.getString(R.string.col_comments));

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
                            mCommentLogs.getValue().clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Log.d(TAG, document.getId() + " => " + document.getData());

                                CommentLog commentLog = document.toObject(CommentLog.class);
                                dataSet.add(commentLog);
                            }

                            mCommentLogs.setValue(dataSet);
                            mRefreshCommentLog.setValue(false);

                            try {
                                lastCommentLog = dataSet.get(dataSet.size() - 1);
                            } catch (IndexOutOfBoundsException e) {
                                //no draft letter in the draft box
                            }
                        } else {
                            mRefreshCommentLog.setValue(false);
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

}



