package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.WriteBatch;
import com.thefidebox.fidebox.dialogs.ConfirmCommentDialog;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.Comment;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.R;

import java.sql.Array;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ReplyFragment extends Fragment implements ConfirmCommentDialog.OnConfirmCommentListener {

    @Override
    public void onConfirmComment(Boolean confirm) {

        if (confirm) {
            ((ViewCommentActivity) mContext).showLayout();
            ((ViewCommentActivity) mContext).getSupportFragmentManager().popBackStack();
            ((ViewCommentActivity) mContext).getSupportFragmentManager().beginTransaction().remove(ReplyFragment.this);
        }
    }

    public ReplyFragment() {
        super();
        setArguments(new Bundle());

        // Required empty public constructor
    }

    //Constants
    private static final String TAG = "ReplyFragment";

    //view
    ImageView  more, backArrow;
    ImageView trollPicture;
    TextView send, userName, name, cvPosition,parentComment, date;
    EditText newComment;
    RelativeLayout relLayoutComment,relativeLayout2,relativeLayout1;
    CoordinatorLayout coordinatorLayout;
    TextView  date1, date2, date3, failure1, failure2, failure3;
    ConstraintLayout constraintLayout1,constraintLayout2,constraintLayout3;
    ProgressBar mainProgressBar;

    //var
    CV mCV;
    Context mContext;
    Comment vParentComment;
    int mdepth;
    CommentRecyclerViewAdapter.CommentRecyclerViewHolder mHolder;
    private DocumentReference mDocumentReference, mUserCVDocumentReference;

    // int depth,parentdepth;
    @ServerTimestamp
    private Timestamp timestamp;
    //depth= the depth at which a new comment is inserted

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View cvReply = inflater.inflate(R.layout.fragment_reply, container, false);

        mContext = getActivity();

        //get necessary info from bundle
        try {
            mdepth = getDepthFromBundle();
            mDocumentReference = FirebaseFirestore.getInstance().document(getDocumentReferenceFromBundle());
            mUserCVDocumentReference = FirebaseFirestore.getInstance().document(getUserLetterDocumentReferenceFromBundle());
            mCV = getCVFromBundle();
            mHolder=getViewHolderFromBundle();

            Log.d(TAG, "onCreateView:1 " + getDocumentReferenceFromBundle());
            Log.d(TAG, "onCreateView:2 " + getUserLetterDocumentReferenceFromBundle());

            if (mdepth > 0) {
                vParentComment = getCommentFromBundle();

            }


        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        //for commentReply
        constraintLayout1=cvReply.findViewById(R.id.constraintLayout1);
        userName = cvReply.findViewById(R.id.tv_user_name);
//        more = commentReply.findViewById(R.id.iv_more);
        date = cvReply.findViewById(R.id.tv_date);
        coordinatorLayout = cvReply.findViewById(R.id.coordinatorLayout);
        newComment = cvReply.findViewById(R.id.et_comment);
        send = cvReply.findViewById(R.id.tv_send);
        backArrow = cvReply.findViewById(R.id.iv_backArrow);
        mainProgressBar=cvReply.findViewById(R.id.progress_bar_main);
        relativeLayout1=cvReply.findViewById(R.id.relLayout1);
        relativeLayout2=cvReply.findViewById(R.id.relLayout2);

        mainProgressBar.setVisibility(View.VISIBLE);

        //based on depth, 2 layouts are provided (one for replying directly to CV, one for replying to comments
        if(mdepth==0){

            //for cvReply
            name = cvReply.findViewById(R.id.tv_name);
            trollPicture=cvReply.findViewById(R.id.iv_troll_picture);
            cvPosition=cvReply.findViewById(R.id.tv_position);
            date1 = cvReply.findViewById(R.id.et_date_1);
            date2 = cvReply.findViewById(R.id.et_date_2);
            date3 = cvReply.findViewById(R.id.et_date_3);
            failure1 = cvReply.findViewById(R.id.et_failure_1);
            failure2 = cvReply.findViewById(R.id.et_failure_2);
            failure3 = cvReply.findViewById(R.id.et_failure_3);

        } else{

            constraintLayout2=cvReply.findViewById(R.id.constraintLayout2);
            constraintLayout3=cvReply.findViewById(R.id.constraintLayout3);
            relativeLayout2.removeViewInLayout(constraintLayout2);
            relativeLayout2.removeViewInLayout(constraintLayout3);

            View commentReply = inflater.inflate(R.layout.layout_comment_reply, container, false);
            RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.BELOW,R.id.constraintLayout1);

            relativeLayout2.addView(commentReply,layoutParams);
            parentComment = commentReply.findViewById(R.id.tv_comment);
        }

        relativeLayout2.setVisibility(View.INVISIBLE);
        newComment.setVisibility(View.INVISIBLE);

        //refer to the note for making ReplyFragment(SingleTon)
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        //getting username
        setUpName();

        if (mdepth == 0) {

            //getting CV date
            Log.d(TAG, "onCreateView: "+mCV.getTimestamp().toDate().toString());
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


        } else {
            parentComment.setText(vParentComment.getComment());
            date.setText(DateDisplay.format(vParentComment.getTimestamp().toDate()));
        }

        return cvReply;
    }


    @Override
    public void onStart() {
        super.onStart();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainProgressBar.setVisibility(View.VISIBLE);

                if (getActivity().getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }

                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                if (
                        newComment.getText().toString().trim().equals("null") ||
                                newComment.getText().toString().trim().length() <= 0) {

                    ShowSnackBar.show(coordinatorLayout, mContext.getString(R.string.empty_comment));

                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                } else {
                    addNewComment(newComment.getText().toString());

                }
            }
        });


    }

    private void setUpName() {

        String userId;

        if(mdepth==0){
            userId=mCV.getUserId();
        } else{
            userId=vParentComment.getCommenterId();
        }

            FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                    .document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Users userAccountSettings = document.toObject(Users.class);
                                    userName.setText(userAccountSettings.getDisplay_name());

                                    mainProgressBar.setVisibility(View.INVISIBLE);
                                    relativeLayout2.setVisibility(View.VISIBLE);
                                    newComment.setVisibility(View.VISIBLE);

                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

    }


    private void addNewComment(String Comment) {

        //for navigating from profile-->comments-->letters-->particular commment
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        WriteBatch batch = db.batch();

        //set comment in the CV
        DocumentReference commentSubCol = mDocumentReference.collection(mContext.getString(R.string.col_comments)).document();
        final Comment comment = new Comment();
        comment.setComment(Comment);
        comment.setTimestamp(timestamp);
        comment.setCommenterId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        comment.setCvId(mCV.getCvId());
        comment.setCommentId(commentSubCol.getId());
        comment.setGiggles(0);
        comment.setChild_comments(0);
        comment.setCategory(mCV.getCategory());

        batch.set(commentSubCol, comment);
        //

  /*      //optional
        //set UserAc CV comment
        DocumentReference posterAccountCVCommentRef = mUserCVDocumentReference.collection(mContext.getString(R.string.col_comments)).document(commentSubCol.getId());
        batch.set(posterAccountCVCommentRef, comment);
        //
*/
        //set Commentor Profile comment
        DocumentReference userAccountCommentRef = db.collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_comments))
                .document(commentSubCol.getId());


        final CommentLog commentLog = new CommentLog();
        commentLog.setRoute(mDocumentReference.collection(mContext.getString(R.string.col_comments))
                .document(commentSubCol.getId()).getPath());
        commentLog.setCategory(mCV.getCategory());
        commentLog.setComment(Comment);
        commentLog.setGiggles(0);
        commentLog.setTimestamp(timestamp);
        commentLog.setChild_comments(0);
        commentLog.setCommentId(commentSubCol.getId());
        commentLog.setCommenterId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        batch.set(userAccountCommentRef, commentLog);

        batch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ShowSnackBar.show(coordinatorLayout, mContext.getString(R.string.send_failed) );
                Log.d(TAG, "onFailure: " + e.getMessage());
                Log.d(TAG, "onFailure: "+comment.toString());
                Log.d(TAG, "onFailure: "+commentLog.toString());
                mainProgressBar.setVisibility(View.INVISIBLE);

                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        newComment.setText("");
                        try{
                            getActivity().getSupportFragmentManager().popBackStack();
                        } catch (NullPointerException e){

                        }
                        comment.setTimestamp(new Timestamp(Calendar.getInstance().getTime()));

                        if (mdepth == 0) {
                            ((ViewCommentActivity) mContext).insertNewComment(comment);
                            ((ViewCommentActivity) mContext).scrollToPosition(0);
                        } else {

                            if(vParentComment.getChild_comments()==0){

                                mHolder.relativeLayoutViewMore.setVisibility(View.VISIBLE);
                                mHolder.mRelLayoutRV.setVisibility(View.VISIBLE);
                                mHolder.viewMore.setText(mContext.getString(R.string.hide_replies));
                                mHolder.viewMore.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        expandOrCollapseReplies(mHolder);
                                    }
                                });
                                mHolder.mRecyclerView.setVisibility(View.VISIBLE);

                            }

                            vParentComment.setChild_comments(vParentComment.getChild_comments()+1);
                            mHolder.mChildComments.add(comment);
                            mHolder.earliestChildComment=comment;
                            mHolder.mAdapter.notifyItemInserted(mHolder.mChildComments.size()-1);

                            //show the child comments

                            mHolder.mRecyclerView.smoothScrollToPosition(mHolder.mChildComments.size()-1);

                            mHolder.mainProgressBar.setVisibility(View.GONE);
                        }

                        ((ViewCommentActivity)mContext).upCommentNumber();
                        ((ViewCommentActivity) mContext).showLayout();

                        mainProgressBar.setVisibility(View.INVISIBLE);

                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                });
    }


    private Comment getCommentFromBundle() {
        Log.d(TAG, "getCommnetFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            return bundle.getParcelable("comment");

        } else {
            return null;
        }
    }

    private String getDocumentReferenceFromBundle() {
        Log.d(TAG, "getDocumentReferenceFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            return bundle.getString(mContext.getString(R.string.document_reference), "");

        } else {
            return null;
        }
    }

    private String getUserLetterDocumentReferenceFromBundle() {
        Log.d(TAG, "getDocumentReferenceFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            return bundle.getString(mContext.getString(R.string.user_cv_document_reference), "");

        } else {
            return null;
        }
    }

    private CV getCVFromBundle() {
        Log.d(TAG, "getLetterFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();

        if (bundle != null) {

            return bundle.getParcelable(mContext.getString(R.string.CV));

        } else {
            return null;
        }
    }

    private int getDepthFromBundle() {
        Log.d(TAG, "getDepthFromBundle: arguments: " + getArguments());
        Bundle bundle = this.getArguments();

        if (bundle != null) {

            return bundle.getInt(mContext.getString(R.string.depth));

        } else {
            return -1;
        }
    }

    private CommentRecyclerViewAdapter.CommentRecyclerViewHolder getViewHolderFromBundle() {
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            return bundle.getParcelable("viewHolder");

        } else {
            return null;
        }
    }

    public void onBackPressed() {
        FragmentManager fm =getActivity().getSupportFragmentManager();
        try {

            if (
                    newComment.getText().toString().trim().equals("") ||
                            newComment.getText().toString().trim().length() <= 0) {
                fm.popBackStack();
                fm.beginTransaction().remove(ReplyFragment.this);
                ((ViewCommentActivity) mContext).showLayout();
            } else {

                ConfirmCommentDialog dialog = ConfirmCommentDialog.newInstance();
                dialog.setTargetFragment(ReplyFragment.this, 1);
                dialog.setCancelable(false);
                dialog.show(getActivity().getSupportFragmentManager(), getString(R.string.confirm_comment_dialog));

            }

        } catch (NullPointerException e) {

            fm.popBackStack();
            ((ViewCommentActivity) mContext).showLayout();
        }

    }

    public void expandOrCollapseReplies(final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder) {

        if (!holder.viewMore.getText().toString().equals(mContext.getString(R.string.hide_replies))) {

            holder.mRelLayoutRV.setVisibility(View.VISIBLE);
            holder.mLayoutManager.scrollToPositionWithOffset(0, 0);
            holder.viewMore.setText(mContext.getString(R.string.hide_replies));

        } else {
            holder.mRelLayoutRV.setVisibility(View.GONE);
            holder.viewMore.setText(mContext.getString(R.string.view_replies_without_number));
        }
    }
}


