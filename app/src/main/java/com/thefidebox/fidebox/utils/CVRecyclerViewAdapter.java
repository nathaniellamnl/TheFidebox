package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thefidebox.fidebox.giggle_comment_util.GiggleCommentManipulation;
import com.thefidebox.fidebox.giggle_comment_util.SmileyEvil;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.NotificationModel;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.profile.ProfileActivity;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;

import java.util.ArrayList;
import java.util.List;

public class CVRecyclerViewAdapter extends RecyclerView.Adapter<CVRecyclerViewAdapter.CVRecyclerViewHolder> {

    private static final String TAG = "CVRecyclerViewAdapter";
    private final int ACTIVITY_NUMBER;
    private CVRecyclerViewAdapter.OnCVListener mOnCVListener;
    private FirebaseMethods mFirebaseMethods;
    public boolean reachEndOfDatabase;
    private RequestManager glide;
    RequestOptions requestOptions;

    //var
    ArrayList<CV> mCVList;
    Context mContext;
    AppCompatActivity mActivity;

    public void setReachEndOfDatabase(boolean reachEndOfDatabase) {
        this.reachEndOfDatabase = reachEndOfDatabase;
    }


    //@NonNull final CVRecyclerViewAdapter.LetterRecyclerViewHolder holder
    public class CVRecyclerViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout userNameDateLayout, trollPictureLayout, dateFailureLayout;
        LinearLayout giggleLayout;
        RelativeLayout commentLayout, relLayoutProgressBar, relativeLayoutGiggle, relLayoutGiggleComment;
        ImageView  giggleBlack, giggleGreen, notificationRing;
        ImageView trollPicture;
        TextView name, date, giggleNo, commentNo, username, cvPosition, date1, date2, date3, failure1, failure2, failure3;
        SmileyEvil smileyEvil;
        Boolean giggleByCurrentUser = false;
        OnCVListener mOnCVListener;
        List<NotificationModel> notificationModelsToNextActivity;
        long cvGiggleNumber = 0;
        long cvCommentNumber = 0;


        CVRecyclerViewHolder(@NonNull final View itemView, CVRecyclerViewAdapter.OnCVListener onCVListener) {
            super(itemView);

            username = itemView.findViewById(R.id.tv_user_name);
            name = itemView.findViewById(R.id.tv_name);
            cvPosition = itemView.findViewById(R.id.tv_position);
            date = itemView.findViewById(R.id.tv_date);
            giggleNo = itemView.findViewById(R.id.tv_giggle_no);
            commentNo = itemView.findViewById(R.id.tv_comment_no);
            trollPicture = itemView.findViewById(R.id.iv_troll_picture);
            giggleBlack = itemView.findViewById(R.id.iv_giggle_black);
            giggleGreen = itemView.findViewById(R.id.iv_giggle_green);
            commentLayout = itemView.findViewById(R.id.relLayout_comment);
            relLayoutProgressBar = itemView.findViewById(R.id.relLayout_progress_bar);
            date1 = itemView.findViewById(R.id.et_date_1);
            date2 = itemView.findViewById(R.id.et_date_2);
            date3 = itemView.findViewById(R.id.et_date_3);
            failure1 = itemView.findViewById(R.id.et_failure_1);
            failure2 = itemView.findViewById(R.id.et_failure_2);
            failure3 = itemView.findViewById(R.id.et_failure_3);

            giggleLayout = itemView.findViewById(R.id.linearLayout);
            userNameDateLayout = itemView.findViewById(R.id.constraintLayout1);
            trollPictureLayout = itemView.findViewById(R.id.constraintLayout2);
            dateFailureLayout = itemView.findViewById(R.id.constraintLayout3);
            relativeLayoutGiggle = itemView.findViewById(R.id.relLayout_giggle);
            relLayoutGiggleComment = itemView.findViewById(R.id.relLayout_giggle_comment);
            notificationRing = itemView.findViewById(R.id.notification_ring);

            notificationModelsToNextActivity = new ArrayList<>();
            this.mOnCVListener = onCVListener;

        }

    }

    public CVRecyclerViewAdapter(Context context, ArrayList<CV> CVList,
                                 CVRecyclerViewAdapter.OnCVListener onCVListener,
                                 int activity_number, AppCompatActivity appCompatActivity
    ) {
        mCVList = CVList;
        mContext = context;
        mActivity = appCompatActivity;
        this.mOnCVListener = onCVListener;
        this.ACTIVITY_NUMBER = activity_number;
        reachEndOfDatabase = false;
        this.glide = Glide.with(mContext);

        //set glide image size
        int dpValue = 140; // margin in dips
        float d = mContext.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d); // margin in pixels

        requestOptions = RequestOptions.overrideOf(margin, margin)
                .circleCrop()
                .placeholder(R.drawable.ic_fail)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
    }


    @NonNull
    @Override
    public CVRecyclerViewAdapter.CVRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_cv, parent, false);

        return new CVRecyclerViewAdapter.CVRecyclerViewHolder(v, mOnCVListener);

    }

    @Override
    public void onBindViewHolder(@NonNull final CVRecyclerViewAdapter.CVRecyclerViewHolder holder, final int position) {
        final CV currentCV = mCVList.get(position);


        holder.cvCommentNumber = 0;
        holder.cvGiggleNumber = 0;

        if (position == mCVList.size() - 1 && (position + 1) % 10 == 0 && !reachEndOfDatabase) {

            holder.relLayoutProgressBar.setVisibility(View.VISIBLE);
            holder.userNameDateLayout.setVisibility(View.GONE);
            holder.trollPictureLayout.setVisibility(View.GONE);
            holder.dateFailureLayout.setVisibility(View.GONE);
            holder.giggleLayout.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.relLayoutProgressBar.setVisibility(View.GONE);
                    holder.userNameDateLayout.setVisibility(View.VISIBLE);
                    holder.trollPictureLayout.setVisibility(View.VISIBLE);
                    holder.dateFailureLayout.setVisibility(View.VISIBLE);
                    holder.giggleLayout.setVisibility(View.VISIBLE);
                }
            }, 500);

        }

        holder.smileyEvil = new SmileyEvil(holder.giggleBlack, holder.giggleGreen);
        String mCurrentUser_id = null;

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mCurrentUser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        try {
            if (mCurrentUser_id.equals(currentCV.getUserId())) {

                FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(mContext.getString(R.string.col_cv_giggle_notifications))
                        .whereEqualTo(mContext.getString(R.string.field_cvId), currentCV.getCvId())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot snapshots,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "listen:error", e);
                                    return;
                                }

                                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                    switch (dc.getType()) {
                                        case ADDED:
                                            holder.cvGiggleNumber++;
                                            updateView(holder);
                                            break;
                                        case MODIFIED:
                                            break;
                                        case REMOVED:
                                            holder.cvGiggleNumber--;
                                            updateView(holder);
                                            break;
                                    }
                                }

                            }
                        });

                FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(mContext.getString(R.string.col_cv_comment_notifications))
                        .whereEqualTo(mContext.getString(R.string.field_cvId), currentCV.getCvId())
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot snapshots,
                                                @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.w(TAG, "listen:error", e);
                                    return;
                                }

                                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                    switch (dc.getType()) {
                                        case ADDED:
                                            holder.cvCommentNumber++;
                                            updateView(holder);
                                            break;
                                        case MODIFIED:
                                            break;
                                        case REMOVED:
                                            holder.cvCommentNumber--;
                                            updateView(holder);
                                            break;
                                    }
                                }

                            }
                        });
            }

        } catch (NullPointerException e) {

        }


        //set onClickListener
        holder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToViewCommentActivity(position, holder.cvGiggleNumber, holder.cvCommentNumber);
            }
        });

        holder.trollPictureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToViewCommentActivity(position, holder.cvGiggleNumber, holder.cvCommentNumber);
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser()!=null){

                    switchToProfileActivity(currentCV.getUserId());
                } else{

                    mContext.startActivity(new Intent(mContext, SignInActivity.class));
                }
            }
        });

        holder.giggleBlack.setClickable(false);
        holder.giggleGreen.setClickable(false);
        holder.giggleGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                        if (!holder.smileyEvil.getGiggleUploading()) {

                            GiggleCommentManipulation.downGiggleNumber(holder.giggleNo, mCVList.get(position));

                            holder.smileyEvil.toggleLike();
                            GiggleCommentManipulation.OnClickAction(currentCV.getCvId(), currentCV.getCategory(),
                                    currentCV.getUserId(), mContext, holder.smileyEvil);

                        } else {
                            ToastMaker.showToastMaker(mContext, mContext.getString(R.string.giggle_fast));
                        }

                        holder.smileyEvil.setGiggleUploading(true);

                    } else {

                        mContext.startActivity(new Intent(mContext, SignInActivity.class));
                    }

                } catch (NullPointerException e) {
                    mContext.startActivity(new Intent(mContext,SignInActivity.class));
                }
            }
        });

        holder.giggleBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                        if (!holder.smileyEvil.getGiggleUploading()) {

                            GiggleCommentManipulation.upGiggleNumber(holder.giggleNo, currentCV);
                            holder.smileyEvil.toggleLike();
                            GiggleCommentManipulation.OnClickAction(currentCV.getCvId(), currentCV.getCategory(),
                                    currentCV.getUserId(), mContext, holder.smileyEvil);
                        } else {
                            ToastMaker.showToastMaker(mContext, mContext.getString(R.string.giggle_fast));
                        }
                        holder.smileyEvil.setGiggleUploading(true);
                    } else {

                        mContext.startActivity(new Intent(mContext, SignInActivity.class));
                    }

                } catch (NullPointerException e) {
                    mContext.startActivity(new Intent(mContext,SignInActivity.class));
                }

            }
        });


        //getting name
        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(currentCV.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Users userAc = documentSnapshot.toObject(Users.class);
                        holder.username.setText(userAc.getDisplay_name());

                    }
                });

        //getting date
        holder.date.setText(DateDisplay.format(currentCV.getTimestamp().toDate()));

        glide.load(currentCV.getTroll_picture())
                .apply(requestOptions)
                .into(holder.trollPicture);

        try {
            holder.name.setText(currentCV.getName());
            if (currentCV.getName().equals("")) {
                holder.name.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            holder.name.setVisibility(View.GONE);
        }

        try {
            holder.cvPosition.setText(currentCV.getPosition());
            if (currentCV.getPosition().equals("")) {
                holder.cvPosition.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            holder.cvPosition.setVisibility(View.GONE);
        }

        //getting dates and failures
        holder.date1.setText(currentCV.getDate1());
        holder.failure1.setText(currentCV.getFailure1());

        try {
            holder.date2.setText(currentCV.getDate2());
            holder.failure2.setText(currentCV.getFailure2());
            if (currentCV.getDate2().equals("")) {
                holder.date2.setVisibility(View.GONE);
            }
            if (currentCV.getFailure2().equals("")) {
                holder.failure2.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            holder.date2.setVisibility(View.GONE);
            holder.failure2.setVisibility(View.GONE);
            holder.date3.setVisibility(View.GONE);
            holder.failure3.setVisibility(View.GONE);
        }

        try {
            holder.date3.setText(currentCV.getDate3());
            holder.failure3.setText(currentCV.getFailure3());
            if (currentCV.getDate3().equals("")) {
                holder.date3.setVisibility(View.GONE);
            }
            if (currentCV.getFailure3().equals("")) {
                holder.failure3.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            holder.date3.setVisibility(View.GONE);
            holder.failure3.setVisibility(View.GONE);
        }

        //set numbers
        holder.giggleNo.setText(String.valueOf(currentCV.getGiggles()));
        holder.commentNo.setText(String.valueOf(currentCV.getComments()));


        //
        if (mCurrentUser_id != null) {
            FirebaseFirestore.getInstance().collection(currentCV.getCategory())
                    .document(currentCV.getCvId())
                    .collection(mContext.getString(R.string.field_giggles))
                    .whereEqualTo(mContext.getString(R.string.field_userId), mCurrentUser_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {
                                    holder.giggleByCurrentUser = false;
                                    setUpGiggle(holder);
                                }

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        holder.giggleByCurrentUser = true;
                                        setUpGiggle(holder);
                                    }
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

        }
        //

    }

    private void setUpGiggle(final CVRecyclerViewAdapter.CVRecyclerViewHolder holder) {
        try {

            if (holder.giggleByCurrentUser) {
                Log.d(TAG, "setupGiggleString: letter is liked by current user");
                holder.giggleBlack.setVisibility(View.GONE);
                holder.giggleBlack.setClickable(true);
                holder.giggleGreen.setVisibility(View.VISIBLE);
                holder.giggleGreen.setClickable(true);

            } else {
                Log.d(TAG, "setupLikesString: letter is not giggleed by current user");
                holder.giggleBlack.setVisibility(View.VISIBLE);
                holder.giggleBlack.setClickable(true);
                holder.giggleGreen.setVisibility(View.GONE);
                holder.giggleGreen.setClickable(true);
            }
        } catch (NullPointerException e) {

        }
    }

    @Override
    public long getItemId(int position) {
        if(mCVList!=null){

            return mCVList.get(position).getCvId().hashCode();
        } else{
            return -1;
        }
    }

    @Override
    public int getItemCount() {
        return mCVList.size();
    }


    public interface OnCVListener {
//        void OnMoreOptionsLetterClick(int position);

        void OnCommentClick(int position);
    }


    private void switchToViewCommentActivity(int pos, long cvGiggleNumber, long cvCommentNmuber) {

        // onCommentClick();
        Intent intent = new Intent(mContext, ViewCommentActivity.class);
        intent.putExtra("cv", mCVList.get(pos));
        intent.putExtra("category", mCVList.get(pos).getCategory());
        intent.putExtra("pos", pos);
        intent.putExtra("cvCommentNumber", cvCommentNmuber);
        intent.putExtra("cvGiggleNumber", cvGiggleNumber);
        AppCompatActivity appCompatActivity = (AppCompatActivity) mContext;
        appCompatActivity.startActivityForResult(intent, HomeActivity.HOME_ACTIVITY_REQUEST_CODE);
    }

    private void switchToProfileActivity(String userId) {
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.putExtra("callingClass",TAG);
        intent.putExtra("userId", userId);
        mContext.startActivity(intent);

    }

    private void updateView(CVRecyclerViewHolder viewHolder) {
        if (viewHolder.cvCommentNumber + viewHolder.cvGiggleNumber > 0) {
            viewHolder.notificationRing.setVisibility(View.VISIBLE);
        } else {
            viewHolder.notificationRing.setVisibility(View.INVISIBLE);
        }
    }
}

