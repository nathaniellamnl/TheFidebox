package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.thefidebox.fidebox.giggle_comment_util.SmileyEvil;
import com.thefidebox.fidebox.models.Comment;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.models.Giggle;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.R;

import java.util.ArrayList;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.CommentRecyclerViewHolder> {

    public interface ScrollToListener {
        void scrollToListener(@NonNull final int position, @NonNull final int depth);
    }

    ScrollToListener mScrollToListener;

    private static final String TAG = "CommentRViewAdapter";

    //  private CV currentLetter;

    //var
    ArrayList<Comment> mCommentList;
    Context mContext;
    int mDepth;
    CollectionReference mCollectionReference, mUserCVCollectionReference;
    CV mCV;
    RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
    boolean reachEndOfList;
    CommentLog mCommentLog;
    String  mGrandParentCommentId,mParentCommentId;

    public void setReachEndOfList(boolean reachEndOfList) {
        this.reachEndOfList = reachEndOfList;
    }

    //@NonNull final CVRecyclerViewAdapter.LetterRecyclerViewHolder holder
    public class CommentRecyclerViewHolder extends RecyclerView.ViewHolder implements Parcelable {

        protected CommentRecyclerViewHolder(Parcel in) {
            super((View) in.readParcelable(View.class.getClassLoader()));
            mView = in.readParcelable(View.class.getClassLoader());
            supportByCurrentUser = in.readByte() != 0;
            isExpanded = in.readByte() != 0;
            mChildComments = in.createTypedArrayList(Comment.CREATOR);
            earliestChildComment = in.readParcelable(Comment.class.getClassLoader());
            earliestKeyChildComment = in.readParcelable(Comment.class.getClassLoader());
            childCommentBooleanA = in.readByte() != 0;
            childCommentBooleanB = in.readByte() != 0;
            mChildComments = in.createTypedArrayList(Comment.CREATOR);
            mAdapter = in.readParcelable(CommentRecyclerViewAdapter.class.getClassLoader());
            mLayoutManager=in.readParcelable(LinearLayoutManager.class.getClassLoader());
        }

        public final Parcelable.Creator<CommentRecyclerViewHolder> CREATOR = new Parcelable.Creator<CommentRecyclerViewHolder>() {
            @Override
            public CommentRecyclerViewHolder createFromParcel(Parcel in) {
                return new CommentRecyclerViewHolder(in);
            }

            @Override
            public CommentRecyclerViewHolder[] newArray(int size) {
                return new CommentRecyclerViewHolder[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable((Parcelable) mView, 0);
            dest.writeByte((byte) (supportByCurrentUser ? 1 : 0));
            dest.writeByte((byte) (isExpanded ? 1 : 0));
            dest.writeTypedList(mChildComments);
            dest.writeParcelable(earliestChildComment, flags);
            dest.writeParcelable(earliestChildComment, flags);
            dest.writeByte((byte) (childCommentBooleanA ? 1 : 0));
            dest.writeByte((byte) (childCommentBooleanB ? 1 : 0));
            dest.writeTypedList(mChildComments);
            dest.writeParcelable((Parcelable) mAdapter, flags);
            dest.writeParcelable((Parcelable)mLayoutManager,flags);

        }

        ProgressBar mainProgressBar,lastProgressBar;
        RelativeLayout mRelLayoutRV, relativeLayoutViewMore, relativeLayoutParent, relativeLayoutSpacing,relativeLayoutGrandParent;
        ImageView giggleBlack, giggleGreen, mComment;
        TextView mName, mCommentContent, mCommentDate, mSupport_No, mComment_No, mLoadMore;
        SmileyEvil smileyEvil;
        boolean supportByCurrentUser = false, isExpanded = false;
        ConstraintLayout mConstraintLayout1, mConstraintLayout2;
        RecyclerView mRecyclerView;
        CommentRecyclerViewAdapter mAdapter;
        ArrayList<Comment> mChildComments;
        Comment earliestChildComment, earliestKeyChildComment;
        boolean childCommentBooleanA = false, childCommentBooleanB = false;
        View mView;
        TextView viewMore;
        LinearLayoutManager mLayoutManager;


        public CommentRecyclerViewHolder(View view) {
            super(view);
            mView = view;
            mName = itemView.findViewById(R.id.tv_user_name);
            mCommentContent = itemView.findViewById(R.id.tv_comment_content);
            mCommentDate = itemView.findViewById(R.id.tv_date);
            mSupport_No = itemView.findViewById(R.id.tv_support_no);
            mComment_No = itemView.findViewById(R.id.tv_comment_no);

            giggleBlack = itemView.findViewById(R.id.iv_hand_white);
            giggleGreen = itemView.findViewById(R.id.iv_hand_blue);
            mComment = itemView.findViewById(R.id.iv_comment);
            mConstraintLayout1 = itemView.findViewById(R.id.constraintLayout1);
            mConstraintLayout2 = itemView.findViewById(R.id.constraintLayout2);
            mRecyclerView = itemView.findViewById(R.id.recycler_view);
            mRelLayoutRV = itemView.findViewById(R.id.relLayout_recycler_view);
            mChildComments = new ArrayList<>();
            viewMore = itemView.findViewById(R.id.viewMore);
            relativeLayoutViewMore = itemView.findViewById(R.id.relLayout_view_more);
            relativeLayoutParent = itemView.findViewById(R.id.relLayout_parent);
            mLayoutManager = new LinearLayoutManager(mContext);
            mainProgressBar = itemView.findViewById(R.id.progress_bar_main);
            relativeLayoutSpacing = itemView.findViewById(R.id.spacing);
            lastProgressBar=itemView.findViewById(R.id.progress_bar_last);
            relativeLayoutGrandParent=itemView.findViewById(R.id.relLayout_grandparent);

            giggleBlack.setClickable(false);
            giggleGreen.setClickable(false);
        }

    }

    public CommentRecyclerViewAdapter(Context context, ArrayList<Comment> CommentList,
                                      CollectionReference colRef, CV CV,
                                      int depth, CollectionReference userLetterCollectionReference,@Nullable CommentLog commentLog) {
        mCommentList = CommentList;
        mContext = context;
        mCollectionReference = colRef;
        mUserCVCollectionReference = userLetterCollectionReference;
        mCV = CV;
        mDepth = depth;
        reachEndOfList=false;
        mCommentLog=commentLog;

        if(mCommentLog!=null){
            String route=mCommentLog.getRoute();
            String[] routeParts=route.split("/");

            try{
                mGrandParentCommentId=routeParts[3];
                mParentCommentId=routeParts[5];
            }catch (IndexOutOfBoundsException e){
                Log.d(TAG, "CommentRecyclerViewAdapter: "+e.getMessage());
            }

        }


    }


    @NonNull
    @Override
    public CommentRecyclerViewAdapter.CommentRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_fragment_view_comment, parent, false);

        return new CommentRecyclerViewAdapter.CommentRecyclerViewHolder(v);

    }

    @Override
    public void onBindViewHolder(@NonNull final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder, final int position) {
        final Comment currentComment = mCommentList.get(position);

        //if the end of the list
        if(position==mCommentList.size()-1 && mDepth==0 && (position+1)%10==0 && !reachEndOfList){
            holder.relativeLayoutParent.setVisibility(View.GONE);
            holder.lastProgressBar.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.relativeLayoutParent.setVisibility(View.VISIBLE);
                    holder.lastProgressBar.setVisibility(View.GONE);
                }
            },500);
        }

        holder.smileyEvil = new SmileyEvil(holder.giggleBlack, holder.giggleGreen);

        CollectionReference colRef = mCollectionReference.document(currentComment.getCommentId())
                .collection(mContext.getString(R.string.field_comments));

        CollectionReference userLetterCollectionReference = mUserCVCollectionReference.document(currentComment.getCommentId())
                .collection(mContext.getString(R.string.field_comments));


        holder.mChildComments.clear();
        holder.mAdapter = new CommentRecyclerViewAdapter(mContext, holder.mChildComments, colRef, mCV,
                mDepth + 1, userLetterCollectionReference,mCommentLog);
//        holder.mAdapter.setHasStableIds(true);

        holder.mRecyclerView.setItemViewCacheSize(20);
        holder.mRecyclerView.setLayoutManager(holder.mLayoutManager);
        holder.mRecyclerView.setLayoutManager(new LinearSmoothScrollerManager(mContext, LinearLayoutManager.VERTICAL, false));
        holder.mRecyclerView.setRecycledViewPool(sharedPool);
        holder.mRecyclerView.setAdapter(holder.mAdapter);

        //loading
        holder.relativeLayoutViewMore.setVisibility(View.GONE);

        holder.mRelLayoutRV.setVisibility(View.GONE);

        holder.giggleGreen.setClickable(false);
        holder.giggleBlack.setClickable(false);


        //adjust margin
        if (mDepth > 0) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            float d = mContext.getResources().getDisplayMetrics().density;
            int margin = (int) (7 * d); // margin in pixels
            layoutParams.setMargins(0, margin, 0, 0);
            holder.itemView.setLayoutParams(layoutParams);

        }


        //Max depth of comment: 3
        if(mDepth==2){
            holder.mComment.setVisibility(View.INVISIBLE);
        }

        ///wait til childkeycomment has finished loading
//        holder.itemView.setVisibility(View.VISIBLE);

        //setting up child comments
        if (currentComment.getChild_comments() > 0) {

            holder.relativeLayoutViewMore.setVisibility(View.VISIBLE);

            holder.viewMore.setText(mContext.getString(R.string.view_replies, currentComment.getChild_comments()));
            //find child comments

            getEarliestChildKeyComment(currentComment, holder);

            holder.viewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    expandOrCollapseReplies(holder, position, currentComment);
                }
            });
        } else{
            holder.itemView.setVisibility(View.VISIBLE);
        }

        //When the comment gets new giggles/comments,  display a background color
        if(mCommentLog!=null){
            if(mCommentLog.getCommentId().equals(currentComment.getCommentId())){
                holder.relativeLayoutGrandParent.setBackgroundColor(mContext.getResources().getColor(R.color.colorTintBlue));
            }

            //When the comment gets new giggles/comments, unhide the grandparent comment
            if(mGrandParentCommentId!=null){
                if(mGrandParentCommentId.equals(currentComment.getCommentId())){
                    holder.viewMore.performClick();
                }
            }

            //When the comment gets new giggles/comments, unhide the parent comment
            if(mParentCommentId!=null){
                if(mParentCommentId.equals(currentComment.getCommentId())){
                    holder.viewMore.performClick();
                }
            }

        }

        holder.giggleGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.smileyEvil.getGiggleUploading()) {

                    holder.smileyEvil.toggleLike();
                    downSupportNumber(holder.mSupport_No);
                    OnClickAction(holder.smileyEvil, holder.mSupport_No, currentComment);

                } else {
                    ToastMaker.showToastMaker(mContext, mContext.getString(R.string.giggle_fast));
                }

                holder.smileyEvil.setGiggleUploading(true);
            }
        });

        holder.giggleBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!holder.smileyEvil.getGiggleUploading()) {

                    holder.smileyEvil.toggleLike();
                    upSupportNumber(holder.mSupport_No);

                    OnClickAction(holder.smileyEvil, holder.mSupport_No, currentComment);

                } else {
                    ToastMaker.showToastMaker(mContext, mContext.getString(R.string.giggle_fast));
                }

                holder.smileyEvil.setGiggleUploading(true);

            }
        });

        holder.mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((ViewCommentActivity) mContext).switchToReplyFragment(currentComment,
                        mCollectionReference.document(currentComment.getCommentId()), mCV, mDepth + 1,
                        holder, mUserCVCollectionReference.document(currentComment.getCommentId()));

            }
        });


        try {
            //setting up comment content
            holder.mCommentContent.setText(currentComment.getComment());

            //set the time it was posted
            holder.mCommentDate.setText(DateDisplay.format(currentComment.getTimestamp().toDate()));

            //setting up name and profile picture
            FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                    .document(currentComment.getCommenterId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Users userAccountSettings = document.toObject(Users.class);
                                    holder.mName.setText(userAccountSettings.getDisplay_name());

                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

            //getting support number
            try {
                holder.mSupport_No.setText(String.valueOf(currentComment.getGiggles()));

            } catch (NullPointerException e) {

                holder.mSupport_No.setText(String.valueOf(0));
            }


            //getting comment number
            try {
                holder.mComment_No.setText(String.valueOf(currentComment.getChild_comments()));
            } catch (NullPointerException e) {

                holder.mComment_No.setText(String.valueOf(0));
            }

        } catch (NullPointerException e) {
            Log.d(TAG, "onBindViewHolder: " + e);
        }

        try {

            final String mCurrentUser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            mCollectionReference
                    .document(currentComment.getCommentId())
                    .collection(mContext.getString(R.string.col_giggles))
                    .whereEqualTo(mContext.getString(R.string.field_userId), mCurrentUser_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {
                                    holder.supportByCurrentUser = false;
                                    setUpGiggle(holder);
                                }

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.exists()) {
                                        holder.supportByCurrentUser = true;
                                        setUpGiggle(holder);
                                    }
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });


        } catch (NullPointerException e) {
            Log.d(TAG, "onBindViewHolder: " + e);
        }

    }

    private void setUpGiggle(final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder) {
        try {

            if (holder.supportByCurrentUser) {
                Log.d(TAG, "setupGiggle: letter is liked by current user");
                holder.giggleBlack.setVisibility(View.GONE);
                holder.giggleBlack.setClickable(true);
                holder.giggleGreen.setVisibility(View.VISIBLE);
                holder.giggleGreen.setClickable(true);

            } else {
                Log.d(TAG, "setupGiggle: letter is not supported by current user");
                holder.giggleBlack.setVisibility(View.VISIBLE);
                holder.giggleBlack.setClickable(true);
                holder.giggleGreen.setVisibility(View.GONE);
                holder.giggleGreen.setClickable(true);
            }
        } catch (NullPointerException e) {

        }

//        holder.relativeLayoutParent.setVisibility(View.VISIBLE);
//        holder.relativeLayoutPlaceHolder.setVisibility(View.GONE);
    }

    private void OnClickAction(final SmileyEvil smileyEvil, final TextView TVsupportNo, final Comment comment) {

        final String mCurrentUser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mCollectionReference
                .document(comment.getCommentId())
                .collection(mContext.getString(R.string.field_giggles))
                .whereEqualTo(mContext.getString(R.string.field_userId), mCurrentUser_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + String.valueOf(task.getResult().size()));
                            if (task.getResult().size() == 0) {

                                //case 1: not supported by user
                                addNewGiggle(comment, smileyEvil);
                                //   getCommentNumber(TVsupportNo);

                            } else if (task.getResult().size() > 0) {
                                //case 2: user supported
                                removeGiggle(comment, smileyEvil);
                            }
                        } else {

                            smileyEvil.setGiggleUploading(false);
                            Log.d(TAG, "onCickAction: fail");
                        }
                    }
                });

    }

    private void addNewGiggle(final Comment comment, final SmileyEvil smileyEvil) {
        Log.d(TAG, "addNewGiggle: adding new Giggle");

        Giggle giggle = new Giggle();
        giggle.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        WriteBatch batch = db.batch();

        DocumentReference supportColRef = mCollectionReference.document(comment.getCommentId())
                .collection(mContext.getString(R.string.field_giggles)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        batch.set(supportColRef, giggle);

        DocumentReference giggleRef = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(comment.getCommenterId())
                .collection(mContext.getString(R.string.col_comments))
                .document(comment.getCommentId())
                .collection(mContext.getString(R.string.col_giggles))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        batch.set(giggleRef, giggle);

        batch.commit().
                addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        smileyEvil.setGiggleUploading(false);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }


    private void removeGiggle(Comment comment, final SmileyEvil smileyEvil) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        WriteBatch batch = db.batch();


        //update CV comment giggle
        DocumentReference supportColRef = mCollectionReference.document(comment.getCommentId())
                .collection(mContext.getString(R.string.field_giggles)).document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        batch.delete(supportColRef);


        DocumentReference giggleRef = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(comment.getCommenterId())
                .collection(mContext.getString(R.string.col_comments))
                .document(comment.getCommentId())
                .collection(mContext.getString(R.string.col_giggles))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        batch.delete(giggleRef);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                smileyEvil.setGiggleUploading(false);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }


    public void cancelProgressBar(@NonNull final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder) {
        Log.d(TAG, "cancelProgressBar:cancelling");
//        holder.mRelLayoutProgressBar.setVisibility(View.GONE);
    }

    private void upSupportNumber(TextView TVsupportNo) {
        TVsupportNo.setText(String.valueOf(Integer.parseInt(TVsupportNo.getText().toString()) + 1));
    }

    private void downSupportNumber(TextView TVsupportNo) {
        TVsupportNo.setText(String.valueOf(Integer.parseInt(TVsupportNo.getText().toString()) - 1));
    }

    private void expandComment(@NonNull final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder, boolean isExpanded) {
        holder.mConstraintLayout2.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

    }

  /*  @Override
    public long getItemId(int position) {
        if(mCommentList !=null){

            Comment comment = mCommentList.get(position);
            return comment.getCommentId().hashCode();
        } else{
            return -1;
        }
    }*/

    private void getEarliestChildKeyComment(final Comment currentComment, @NonNull final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder) {

        mCollectionReference.document(currentComment.getCommentId())
                .collection(mContext.getString(R.string.field_comments))
                .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                holder.earliestKeyChildComment = document.toObject(Comment.class);
                                holder.itemView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            //error
                            holder.itemView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    private void expandOrCollapseReplies(final CommentRecyclerViewAdapter.CommentRecyclerViewHolder holder, final int position,
                                         final Comment currentComment) {

        holder.viewMore.setClickable(false);

        if (!holder.viewMore.getText().toString().equals(mContext.getString(R.string.hide_replies))) {

            holder.mRelLayoutRV.setVisibility(View.VISIBLE);
            holder.mLayoutManager.scrollToPositionWithOffset(0, 0);

            mScrollToListener = (ScrollToListener) mContext;
            mScrollToListener.scrollToListener(position, mDepth);

            try {

                if (holder.earliestKeyChildComment.getCommentId().equals(holder.earliestChildComment.getCommentId())) {
                    holder.viewMore.setText(mContext.getString(R.string.hide_replies));

                    holder.viewMore.setClickable(true);
                    return;
                }
            } catch (NullPointerException e) {

            }

            Query query;

            //Load more child comments
            if (holder.mChildComments.size()>0) {

                query = mCollectionReference.document(currentComment.getCommentId())
                        .collection(mContext.getString(R.string.col_comments))
                        .orderBy(mContext.getString(R.string.field_timestamp), com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .startAfter(holder.earliestChildComment.getTimestamp())
                        .limit(3);
            }

            //first time loading child comment
            else {
                holder.mainProgressBar.setVisibility(View.VISIBLE);
                query = mCollectionReference.document(currentComment.getCommentId())
                        .collection(mContext.getString(R.string.col_comments))
                        .orderBy(mContext.getString(R.string.field_timestamp), com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(3);
            }
                    query
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                               @Override
                                               public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                   if (task.isSuccessful()) {
                                                       for (QueryDocumentSnapshot document : task.getResult()) {

                                                           Comment comment = document.toObject(Comment.class);
                                                           holder.mChildComments.add(0, comment);

                                                           if (holder.mChildComments != null) {
                                                               holder.mAdapter.notifyItemInserted(0);
                                                           }
                                                       }
                                                       try {
                                                           holder.earliestChildComment = holder.mChildComments.get(0);

                                                       } catch (IndexOutOfBoundsException e) {
                                                           Log.d(TAG, "onComplete54: " + e);
                                                           holder.mRecyclerView.setVisibility(View.GONE);
                                                       }

                                                       holder.mainProgressBar.setVisibility(View.GONE);
                                                       int repliesNumToBeShown = currentComment.getChild_comments() - holder.mAdapter.getItemCount();

                                                       if (repliesNumToBeShown <= 0) {
                                                           holder.viewMore.setText(mContext.getString(R.string.hide_replies));
                                                       } else {
                                                           holder.viewMore.setText(mContext.getString(R.string.view_replies, repliesNumToBeShown));
                                                       }
                                                       holder.viewMore.setClickable(true);

                                                   } else {
                                                       holder.viewMore.setClickable(true);
                                                       Log.d(TAG, "Error getting documents: ", task.getException());
                                                   }
                                               }
                                           }
                    );

        } else {
            holder.mRelLayoutRV.setVisibility(View.GONE);
            holder.viewMore.setText(mContext.getString(R.string.view_replies_without_number));
            holder.viewMore.setClickable(true);

            try {
                mScrollToListener = (ScrollToListener) mContext;
            } catch (ClassCastException e) {
                Log.e(TAG, "loadMoreData 1: ClassCastException: " + e.getMessage());
            }
            try {
                mScrollToListener.scrollToListener(position, mDepth);

            } catch (NullPointerException e) {
                Log.e(TAG, "loadMoreData 3: NullPointerException: " + e.getMessage());
            }
        }
    }
}

