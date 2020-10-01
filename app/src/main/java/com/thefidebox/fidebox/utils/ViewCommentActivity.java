package com.thefidebox.fidebox.utils;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.WriteBatch;
import com.thefidebox.fidebox.giggle_comment_util.GiggleCommentManipulation;
import com.thefidebox.fidebox.giggle_comment_util.SmileyEvil;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.Comment;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewCommentActivity extends AppCompatActivity {


    //constants
    private static final String TAG = "ViewCommentActivity";

    //var
    int mActivityNumber, pos;
    @ServerTimestamp
    private Timestamp timestamp;
    private Context mContext;
    private ArrayList<Comment> mParentComments;
    private Comment lastComment, lastKeyComment;
    private CV mCV;
    private int mIndex;
    String category, type;
    boolean giggleByCurrentUser, isScrolling;
    Intent intent;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    CommentLog commentLog;
    SmileyEvil smileyEvil;

    //Views
    RelativeLayout commentLayout, relativeLayout;
    FrameLayout frameLayout;
    ImageView send, backArrow, giggleBlack, giggleGreen;
    TextView newComment, giggleNumber, commentNumber;
    private RecyclerView mRecyclerView;
    CommentRecyclerViewAdapter mCommentRecyclerViewAdapter;
    CommentRecyclerViewAdapter.CommentRecyclerViewHolder viewHolder;
    LinearLayout linearLayout_giggle_comment;
    ProgressBar mainProgressBar, bottomProgressBar;
    ConstraintLayout userNameDateLayout, trollPictureLayout, dateFailureLayout;

    /*if coming from profileActivity*/ TextView userName, date, name, cvPosition, failure1, date1, failure2, date2, failure3, date3;
    ImageView trollPicture;


    private LinearLayoutManager mLayoutManager;


    public interface OnCommentItemsListener {
        void onCommentItems(Comment comment, DocumentReference docRef, int ActivityNumber);
    }

    //OnCommentItemsListener mOnCommentItemsListener;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comment);

        relativeLayout = findViewById(R.id.container_relLayout);
        frameLayout = findViewById(R.id.container_frameLayout);
        commentLayout = findViewById(R.id.commentLayout);
        mRecyclerView = findViewById(R.id.recycler_view);
        newComment = findViewById(R.id.et_comment);
        send = findViewById(R.id.btn_send);
        backArrow = findViewById(R.id.iv_backArrow);
        giggleBlack = findViewById(R.id.iv_giggle_black);
        giggleGreen = findViewById(R.id.iv_giggle_green);
        giggleNumber = findViewById(R.id.tv_giggle_no);
        commentNumber = findViewById(R.id.tv_comment_no);
        mainProgressBar = findViewById(R.id.progress_bar_main);
        bottomProgressBar = findViewById(R.id.progress_bar_bottom);
        linearLayout_giggle_comment = findViewById(R.id.linearLayout_giggle_comment);


        //From profile activity
        userName = findViewById(R.id.tv_user_name);
        date = findViewById(R.id.tv_date);
        date1 = findViewById(R.id.et_date_1);
        date2 = findViewById(R.id.et_date_2);
        date3 = findViewById(R.id.et_date_3);
        failure1 = findViewById(R.id.et_failure_1);
        failure2 = findViewById(R.id.et_failure_2);
        failure3 = findViewById(R.id.et_failure_3);
        trollPicture = findViewById(R.id.iv_troll_picture);
        name = findViewById(R.id.tv_name);
        cvPosition = findViewById(R.id.tv_position);
        userNameDateLayout = findViewById(R.id.constraintLayout1);
        trollPictureLayout = findViewById(R.id.constraintLayout2);
        dateFailureLayout = findViewById(R.id.constraintLayout3);


        bottomProgressBar.setVisibility(View.GONE);
        mContext = ViewCommentActivity.this;

        smileyEvil = new SmileyEvil(giggleBlack, giggleGreen);

        setupFirebaseAuth();
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        intent = new Intent();

        gettingIntent();

    }

    private void loadComments(final String cvId, final String category, final CommentLog commentLog) {

        //if commentLog is not null, load it first and then other comments
        if (commentLog != null) {
            final String[] routeStrings = commentLog.getRoute().split("/");

            FirebaseFirestore.getInstance().collection(mCV.getCategory())
                    .document(cvId)
                    .collection(mContext.getString(R.string.field_comments))
                    .document(routeStrings[3])
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    Comment notificationComment = document.toObject(Comment.class);

                                    mParentComments.add( notificationComment);
                                    mCommentRecyclerViewAdapter.notifyItemInserted(0);

                                } else {
                                    Log.d(TAG, "No such document");
                                }


                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                            loadMainComments(cvId,routeStrings);
                        }
                    });
        } else{
            loadMainComments(cvId,null);
        }

    }

    private void loadMainComments(final String cvId,final String[] routeStrings){

        FirebaseFirestore.getInstance().collection(mCV.getCategory())
                .document(cvId)
                .collection(mContext.getString(R.string.field_comments))
                .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Comment comment = document.toObject(Comment.class);
                                Log.d(TAG, "loadcomments:inside. ");

                                //make sure self-comment from push notification shows up as the 1st item
                                if (commentLog != null) {
                                    if (!comment.getCommentId().equals(routeStrings[3])) {
                                        mParentComments.add(1,comment);
                                        mCommentRecyclerViewAdapter.notifyItemInserted(1);
                                    }
                                } else {
                                    mParentComments.add(0,comment);
                                    mCommentRecyclerViewAdapter.notifyItemInserted(0);
                                }

                            }
                            //finished loading
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    mRecyclerView.setVisibility(View.VISIBLE);

                                    if (type != null) {
                                        if ("comment".equals(type)) {
                                            mLayoutManager.scrollToPositionWithOffset(0, 0);
                                        }
                                    }

                                    linearLayout_giggle_comment.setVisibility(View.VISIBLE);
                                    mainProgressBar.setVisibility(View.INVISIBLE);

                                }
                            }, 1000);

                            if (commentLog != null /*&& mParentComments.size() <= 10*/) {
                                mLayoutManager.scrollToPositionWithOffset(0, 0);
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void gettingIntent() {
        //getting intent from either HomeActivity or ProfileActivity(comment section)

        Intent intentFromOtherActivity = getIntent();
        Log.d(TAG, "gettingIntent: ");

        try {
            mCV = intentFromOtherActivity.getParcelableExtra("cv");

            Log.d(TAG, "gettingIntent: " + mCV.toString());

            //if null, mCV should be from pushNotification
            if (mCV == null) {
                getIntentFromPushNotificationOrProfileComment(intentFromOtherActivity);
            } else {
                //make sure the cv obtained is the latest
                FirebaseFirestore.getInstance().collection(mCV.getCategory())
                        .document(mCV.getCvId())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        mCV = document.toObject(CV.class);

                                        commentNumber.setText(String.valueOf(mCV.getComments()));
                                        giggleNumber.setText(String.valueOf(mCV.getGiggles()));

                                        intent.putExtra("cv", mCV);
                                        intent.putExtra("giggleByCurrentUser", pos);
                                        ViewCommentActivity.this.setResult(RESULT_OK, intent);

                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });


                category = intentFromOtherActivity.getStringExtra("category");
                pos = intentFromOtherActivity.getIntExtra("pos", -1);

                long cvGiggleNumber /*  cv notification  */ = intentFromOtherActivity.getLongExtra("cvGiggleNumber", -1);
                long cvCommentNumber /*  cv notification  */ = intentFromOtherActivity.getLongExtra("cvCommentNumber", -1);

                showAndUpdateNotification(cvGiggleNumber, cvCommentNumber, null);

                init(null);
            }

        } catch (NullPointerException e) {

            Log.d(TAG, "gettingIntent: " + e.getMessage());
            getIntentFromPushNotificationOrProfileComment(intentFromOtherActivity);
        }
    }

    private void getIntentFromPushNotificationOrProfileComment(Intent intentFromOtherActivity) {

        Log.d(TAG, "gettingIntentFromPushNotification: ");

        //from push notification
        mCV = intentFromOtherActivity.getParcelableExtra("cvFromFCMService");
        type = intentFromOtherActivity.getStringExtra("type");
        commentLog = intentFromOtherActivity.getParcelableExtra(getString(R.string.comment_log));

        userNameDateLayout.setVisibility(View.VISIBLE);
        trollPictureLayout.setVisibility(View.VISIBLE);
        dateFailureLayout.setVisibility(View.VISIBLE);

        if (mCV == null) {

            //from profile comment

            final String[] commentLogPath = commentLog.getRoute().split("/");

            FirebaseFirestore.getInstance().collection(commentLogPath[0])
                    .document(commentLogPath[1])
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {

                                    mCV = document.toObject(CV.class);
                                    init(commentLog);

                                    loadingCVContent();

                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });


        } else {
            init(commentLog);
            loadingCVContent();
        }

    }

    /**
     * This method is only called if directed from push notification or profile comment
     */
    private void loadingCVContent() {

//        updateNotificationFromPushNotification();
        //getting name
        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(mCV.getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Users userAc = documentSnapshot.toObject(Users.class);
                        userName.setText(userAc.getDisplay_name());
                    }
                });

        //getting date
        date.setText(DateDisplay.format(mCV.getTimestamp().toDate()));

        //getting CV picture
        Glide.with(this).load(mCV.getTroll_picture())
                .circleCrop()
                .fallback(R.drawable.ic_fail)
                .placeholder(R.drawable.grey_box)
                .into(trollPicture);

        //getting CV name
        try {
            name.setText(mCV.getName());
            if (mCV.getName().equals("")) {
                name.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            name.setVisibility(View.GONE);
        }

        //getting CV position
        try {
            cvPosition.setText(mCV.getPosition());
            if (mCV.getPosition().equals("")) {
                cvPosition.setVisibility(View.GONE);
            }
        } catch (NullPointerException e) {
            cvPosition.setVisibility(View.GONE);
        }

        //getting dates and failures
        date1.setText(mCV.getDate1());
        failure1.setText(mCV.getFailure1());

        try {
            date2.setText(mCV.getDate2());
            failure2.setText(mCV.getFailure2());
            if (mCV.getDate2().equals("")) {
                date2.setVisibility(View.GONE);
            }
            if (mCV.getFailure2().equals("")) {
                failure2.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            date2.setVisibility(View.GONE);
            failure2.setVisibility(View.GONE);
            date3.setVisibility(View.GONE);
            failure3.setVisibility(View.GONE);
        }

        try {
            date3.setText(mCV.getDate3());
            failure3.setText(mCV.getFailure3());
            if (mCV.getDate3().equals("")) {
                date3.setVisibility(View.GONE);
            }
            if (mCV.getFailure3().equals("")) {
                failure3.setVisibility(View.GONE);
            }

        } catch (NullPointerException e) {
            date3.setVisibility(View.GONE);
            failure3.setVisibility(View.GONE);
        }

        commentNumber.setText(String.valueOf(mCV.getComments()));
        giggleNumber.setText(String.valueOf(mCV.getGiggles()));

        mainProgressBar.setVisibility(View.GONE);
        updateNotificationFromPushNotificationOrProfileComment(type);
    }

    /**
     * for transition from local transition (not from clicking the push notification
     */
    private void showAndUpdateNotification(long cvGiggleNumber, long cvCommentNumber, String type) {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            WriteBatch db = FirebaseFirestore.getInstance().batch();

            if (cvGiggleNumber > 0 && cvCommentNumber > 0) {
                ToastMaker.showToastMaker(this, getString(R.string.cv_new_giggle_and_comment));
            } else if (cvGiggleNumber > 0) {

                ToastMaker.showToastMaker(this, getString(R.string.cv_new_giggle));
            } else if (cvCommentNumber > 0) {

                ToastMaker.showToastMaker(this, getString(R.string.cv_new_comment));
            }

            if (cvGiggleNumber > 0 && cvCommentNumber > 0) {

                DocumentReference cvGiggleRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(getString(R.string.col_cv_giggle_notifications))
                        .document(mCV.getCvId());
                db.delete(cvGiggleRef);

                DocumentReference cvCommentRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(getString(R.string.col_cv_comment_notifications))
                        .document(mCV.getCvId());
                db.delete(cvCommentRef);

            } else if (cvGiggleNumber > 0) {

                DocumentReference cvGiggleRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(getString(R.string.col_cv_giggle_notifications))
                        .document(mCV.getCvId());
                db.delete(cvGiggleRef);

            } else if (cvCommentNumber > 0) {

                DocumentReference cvCommentRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(getString(R.string.col_cv_comment_notifications))
                        .document(mCV.getCvId());
                db.delete(cvCommentRef);

            }

            db.commit().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + e);
                }
            });
        }
    }


    /**
     * for the transition from push notification
     */
    private void updateNotificationFromPushNotificationOrProfileComment(String type) {

        Log.d(TAG, "updateNotificationFromPushNotificationOrProfileComment: " + type);

        WriteBatch db = FirebaseFirestore.getInstance().batch();

        //delete notification
        if ("CV".equals(type)) {

            DocumentReference cvGiggleRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(getString(R.string.col_cv_giggle_notifications))
                    .document(mCV.getCvId());
            db.delete(cvGiggleRef);


            DocumentReference cvCommentRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(getString(R.string.col_cv_comment_notifications))
                    .document(mCV.getCvId());
            db.delete(cvCommentRef);

        } else {

            DocumentReference commentGiggleRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(getString(R.string.col_comment_giggle_notifications))
                    .document(mCV.getCvId());

            db.delete(commentGiggleRef);

            DocumentReference commentReplyRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(getString(R.string.col_comment_reply_notifications))
                    .document(mCV.getCvId());
            db.delete(commentReplyRef);
        }

        db.commit();
    }


    //for setting up comment section regardless of the originating activity
    private void init(@Nullable CommentLog commentLog) {

        Log.d(TAG, "init: ");
        //intial RV set up
        initialRVSetUp(commentLog);

        //async task to load the comments
        loadComments(mCV.getCvId(), mCV.getCategory(), commentLog);

        //async task to load the last comment
        getLastDBDComment0();
        setUpGiggle();

    }


    private void initialRVSetUp(@Nullable CommentLog commentLog) {

        mParentComments = new ArrayList<>();
        mCommentRecyclerViewAdapter = new CommentRecyclerViewAdapter(mContext, mParentComments,
                FirebaseFirestore.getInstance().collection(mCV.getCategory()).document(mCV.getCvId())
                        .collection(getString(R.string.col_comments)), mCV, 0,
                FirebaseFirestore.getInstance().collection(getString(R.string.col_users)).document(mCV.getUserId())
                        .collection(getString(R.string.col_comments)), commentLog);

//        mCommentRecyclerViewAdapter.setHasStableIds(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setAdapter(mCommentRecyclerViewAdapter);

        //loading
        mRecyclerView.setVisibility(View.INVISIBLE);
        linearLayout_giggle_comment.setVisibility(View.INVISIBLE);
        mainProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItems = mLayoutManager.getItemCount();
                int lastCompletelyVisible = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (isScrolling) {
                    if ((lastCompletelyVisible + 1) == totalItems && dy > 0) {

                        Log.d(TAG, "onScrolled: 3");
                        isScrolling = false;
                        loadMoreData();
                    }

                }
            }
        });


        final DocumentReference cvDocRef = FirebaseFirestore.getInstance().collection(mCV.getCategory()).document(mCV.getCvId());
        final DocumentReference userCVDocRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                .document(mCV.getUserId())
                .collection(getString(R.string.col_cvs))
                .document(mCV.getCvId());

        setCommentLayout(cvDocRef, userCVDocRef);


        giggleBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!smileyEvil.getGiggleUploading()) {

                    //notify giggle change
                    intent.putExtra("giggleByCurrentUser", pos);

                    intent.putExtra("cv", GiggleCommentManipulation.upGiggleNumber(giggleNumber, mCV));
                    ViewCommentActivity.this.setResult(RESULT_OK, intent);

                    smileyEvil.toggleLike();
                    GiggleCommentManipulation.OnClickAction(mCV.getCvId(), mCV.getCategory(), mCV.getUserId(), ViewCommentActivity.this, smileyEvil);

                } else {
                    ToastMaker.showToastMaker(ViewCommentActivity.this, getString(R.string.giggle_fast));
                }

                smileyEvil.setGiggleUploading(true);
            }
        });


        giggleGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!smileyEvil.getGiggleUploading()) {

                    ///notify giggle change
                    intent.putExtra("giggleByCurrentUser", pos);

                    intent.putExtra("cv", GiggleCommentManipulation.downGiggleNumber(giggleNumber, mCV));
                    ViewCommentActivity.this.setResult(RESULT_OK, intent);

                    smileyEvil.toggleLike();
                    GiggleCommentManipulation.OnClickAction(mCV.getCvId(), mCV.getCategory(), mCV.getUserId(), ViewCommentActivity.this, smileyEvil);
                } else {
                    ToastMaker.showToastMaker(ViewCommentActivity.this, getString(R.string.giggle_fast));
                }

                smileyEvil.setGiggleUploading(true);
            }
        });

    }

    private void setUpGiggle() {

        Log.d(TAG, "setUpGiggle: ");
        try {

            final String mCurrentUser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance().collection(mCV.getCategory())
                    .document(mCV.getCvId())
                    .collection(getString(R.string.field_giggles))
                    .whereEqualTo(getString(R.string.field_userId), mCurrentUser_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {
                                    giggleByCurrentUser = false;
                                }

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        giggleByCurrentUser = true;
                                    }
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }

                                if (giggleByCurrentUser) {
                                    Log.d(TAG, "setup: CV is liked by current user");
                                    giggleBlack.setVisibility(View.GONE);
                                    giggleGreen.setVisibility(View.VISIBLE);

                                } else {
                                    Log.d(TAG, "setup: CV is not supported by current user");
                                    giggleBlack.setVisibility(View.VISIBLE);
                                    giggleGreen.setVisibility(View.GONE);
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });


        } catch (NullPointerException e) {

        }
    }


    private void setCommentLayout(final DocumentReference cvDocRef, final DocumentReference userCVDocRef) {
        commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToReplyFragment(null, cvDocRef, mCV, 0, null, userCVDocRef);
            }
        });
    }


    public void insertNewComment(final Comment commentToBeInserted) {

        mParentComments.add(0, commentToBeInserted);
        mCommentRecyclerViewAdapter.notifyItemInserted(0);

    }


    private void getLastDBDComment0() {

        Log.d(TAG, "getLastDBDComment0: ");
        FirebaseFirestore.getInstance().collection(mCV.getCategory())
                .document(mCV.getCvId())
                .collection(mContext.getString(R.string.col_comments))
                .orderBy(mContext.getString(R.string.field_timestamp), com.google.firebase.firestore.Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                lastKeyComment = document.toObject(Comment.class);
                                Log.d(TAG, "lastKeyComment: " + lastKeyComment.toString());
//                                commentBooleanB = true;
//                                commentLoader();

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    public void switchToReplyFragment(@Nullable final Comment comment, final DocumentReference letterDocRef, final CV CV, final int depth,
                                      @Nullable final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder,
                                      final DocumentReference userLetterDocRef) {

        Log.d(TAG, "OnCommentClick: calling from commentitem");
        ReplyFragment replyFragment = new ReplyFragment();


        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.CV), CV);
        args.putParcelable("comment", comment);
        args.putString(getString(R.string.document_reference), letterDocRef.getPath());
        args.putString(getString(R.string.user_cv_document_reference), userLetterDocRef.getPath());
        args.putInt(getString(R.string.depth), depth);
        args.putParcelable("viewHolder", holder);

        replyFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideLayout();

        transaction.replace(R.id.container_frameLayout, replyFragment, getString(R.string.view_comment_fragment));
        transaction.addToBackStack(getString(R.string.view_comment_fragment));
        transaction.commit();
    }


    public void hideLayout() {
        Log.d(TAG, "hideLayout: hiding layout");
        relativeLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
        commentLayout.setVisibility(View.GONE);
    }


    public void showLayout() {
        Log.d(TAG, "hideLayout: showing layout");
        relativeLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
        commentLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {

        if (smileyEvil.getGiggleUploading()) {
            return;
        }

        ReplyFragment fragment = (ReplyFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.view_comment_fragment));

        try {
            if (fragment != null) {
                fragment.onBackPressed();

            } else {

                super.onBackPressed();
            }
        } catch (NullPointerException e) {

            super.onBackPressed();
        }
    }

    public void upCommentNumber() {
        mCV.setComments(mCV.getComments() + 1);
        commentNumber.setText(String.valueOf(mCV.getComments()));

        intent.putExtra("giggleByCurrentUser", pos);
        intent.putExtra("cv", mCV);
        ViewCommentActivity.this.setResult(RESULT_OK, intent);
    }


    public void scrollToPosition(int layoutPos) {
        mRecyclerView.smoothScrollToPosition(layoutPos);
    }


    public void loadMoreData() {
        Log.d(TAG, "ViewLoadMoreItems: ");
        try {
            if (lastComment.getCommentId().equals(lastKeyComment.getCommentId())) {
                mCommentRecyclerViewAdapter.setReachEndOfList(true);
                return;
            }
            if (mCV != null) {

                FirebaseFirestore.getInstance().collection(mCV.getCategory())
                        .document(mCV.getCvId())
                        .collection(mContext.getString(R.string.field_comments))
                        .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                        .startAfter(lastComment.getTimestamp())
                        .limit(10)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Comment comment = document.toObject(Comment.class);
                                        mParentComments.add(comment);

                                        if (mParentComments != null) {
                                            //to be amended

                                            mCommentRecyclerViewAdapter.notifyItemInserted(mParentComments.size() - 1);
                                        }
                                    }
                                    bottomProgressBar.setVisibility(View.GONE);
                                    lastComment = mParentComments.get(mParentComments.size() - 1);
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

            }
        } catch (NullPointerException e) {

        }
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                checkCurrentUser(firebaseAuth.getCurrentUser());
            }
        };

    }

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");

        if (user == null) {
            Intent intent = new Intent(mContext, SignInActivity.class);
            finish();
            startActivity(intent);
            this.overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}

