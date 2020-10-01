package com.thefidebox.fidebox.view_models;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.thefidebox.fidebox.models.DraftCV;
import com.thefidebox.fidebox.utils.ToastMaker;

import java.util.ArrayList;

public class DraftViewModel extends AndroidViewModel {

    private static final String TAG = "DraftViewModel";

    private @ServerTimestamp
    Timestamp timestamp;
    private DraftCV lastDraftCV, lastKeyDraftCV;
    private ArrayList<DraftCV> dataSet = new ArrayList<>();
    private MutableLiveData<ArrayList<DraftCV>> mDraftCVs;
    private MutableLiveData<Boolean> mIsUpdating = new MutableLiveData<>();
    private MutableLiveData<Boolean> mCanLoadMore = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsEmpty = new MutableLiveData<>();
    private MutableLiveData<Integer> positionToBeDeleted=new MutableLiveData<>();

    private Context mContext=getApplication().getApplicationContext();

    public DraftViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<ArrayList<DraftCV>> getDrafts() {


        if (mDraftCVs == null) {
            mDraftCVs = new MutableLiveData<>();
            loadDraftLetters();

        }
        // getLastDBDraftLetter();
        mDraftCVs.setValue(dataSet);
        return mDraftCVs;

    }


    public LiveData<Integer> getPositionToBeDeleted(){
        return positionToBeDeleted;
    }

    public LiveData<Boolean> getIsUpdating() {

        if (mIsUpdating == null) {
            mIsUpdating = new MutableLiveData<>();
        }

        return mIsUpdating;
    }

    public LiveData<Boolean> getCanLoadMore() {

        mCanLoadMore.setValue(true);
        loadMoreData();

        return mCanLoadMore;
    }

    public LiveData<Boolean> getIsEmpty() {
        return mIsEmpty;
    }

    public LiveData<ArrayList<DraftCV>> getmDraftCVs() {
        return mDraftCVs;
    }

    //query data/retrieve by accessing the cache
    private void loadDraftLetters() {

        mIsUpdating.setValue(true);
        try {
            CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(mContext.getString(R.string.col_draft_cvs));

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

                                    DraftCV AddedDraftletter = document.toObject(DraftCV.class);

                                    dataSet.add(AddedDraftletter);
                                }
                                try {
                                    lastDraftCV = dataSet.get(dataSet.size() - 1);
                                } catch (IndexOutOfBoundsException e) {
                                    //no draft letter in the draft box
                                }
                                mDraftCVs.setValue(dataSet);
                                mIsUpdating.setValue(false);

                                if(dataSet.size()==0){
                                    mIsEmpty.setValue(true);
                                }

                            } else {
                                mIsEmpty.setValue(true);
                                mIsUpdating.setValue(false);
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mIsUpdating.setValue(false);
                        }
                    });

        } catch (NullPointerException e) {
            mIsUpdating.setValue(false);
            Log.d(TAG, "setupRecyclerView: Null");
        }

    }

    private void loadMoreData() {

        if (lastKeyDraftCV == null || lastDraftCV == null) {
            mCanLoadMore.setValue(true);

        } else if (!lastDraftCV.getCvId().equals(lastKeyDraftCV.getCvId())) {
            mCanLoadMore.setValue(true);

        } else if (lastDraftCV.getCvId().equals(lastKeyDraftCV.getCvId())) {
            mCanLoadMore.setValue(false);

        }

        Log.d(TAG, "loadMoreData: DraftCV");

        final CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_draft_cvs));

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
                                                       lastKeyDraftCV = document.toObject(DraftCV.class);

                                                       Log.d(TAG, "loadMoreData: " + lastKeyDraftCV.toString());

                                                       if (lastKeyDraftCV.getCvId().equals(lastDraftCV.getCvId())) {
                                                           mCanLoadMore.setValue(false);

                                                       } else {
                                                           db.orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                                                                   .limit(10)
                                                                   .startAfter(lastDraftCV.getTimestamp())
                                                                   .get()
                                                                   .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                       @Override
                                                                       public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                           if (task.isSuccessful()) {
                                                                               for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                   DraftCV draftLetter = document.toObject(DraftCV.class);
                                                                                   dataSet.add(draftLetter);
                                                                                   mDraftCVs.setValue(dataSet);
                                                                               }

                                                                               try {

                                                                                   lastDraftCV = dataSet.get(dataSet.size() - 1);

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


    public void addDraftToList(DraftCV draft) {

        Log.d(TAG, "addDraftToList: "+draft.toString());

                mDraftCVs.getValue().add(0,draft);


    }



    public void RemoveDraftLetterFromDatabase(final String letter_id, final int position) {

        CollectionReference db = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_draft_cvs));

        db.document(letter_id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        mDraftCVs.getValue().remove(position);
                        Log.d(TAG, "onSuccess: "+mDraftCVs.getValue().size());
                        positionToBeDeleted.setValue(position);

//                        getApplication()..deleteFromDraftLetterList(position);
                        ToastMaker.showToastMaker(mContext,mContext.getString(R.string.draft_deleted));
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        ToastMaker.showToastMaker(mContext,mContext.getString(R.string.Error));
                        Log.w(TAG, "Error deleting document", e);
                    }
                });

    }

   public void updateDraft(final DraftCV draft, final int position) {

       Log.d(TAG, "updateDraft: "+draft.toString());
       Log.d(TAG, "updateDraft: "+position);

                            mDraftCVs.getValue().remove(position);
                            mDraftCVs.getValue().add(0, draft);
                            mDraftCVs.setValue(dataSet);

    }


}

