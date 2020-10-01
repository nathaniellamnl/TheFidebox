package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.profile.ProfileActivity;

import java.util.ArrayList;

public class CommentProfileRecyclerViewAdapter extends RecyclerView.Adapter<CommentProfileRecyclerViewAdapter.commentLogRecyclerViewHolder> {

    private static final String TAG = "CommentProfileRVAdapter";
    private onCommentListener mOnCommentListener;

    private ArrayList<CommentLog> mCommentLogList;
    Context mContext;
    public boolean reachEndOfDatabase;

    public void setReachEndOfDatabase   (boolean reachEndOfDatabase) {
        this.reachEndOfDatabase = reachEndOfDatabase;
    }

    public class commentLogRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView commentLogContent, commentLogDate,category,giggleNumber,replyNumber;
        ImageView giggleBlack,ivCategory;
        RelativeLayout relativeLayoutParent,relativeLayoutGrandParent;
        ProgressBar progressBar;
        long commentGiggleNumber,commentReplyNumber;

        commentLogRecyclerViewHolder(@NonNull View itemView, onCommentListener oncommentListener) {
            super(itemView);
            commentLogContent=itemView.findViewById(R.id.tv_comment_content);
            commentLogDate=itemView.findViewById(R.id.tv_date);
            category=itemView.findViewById(R.id.tv_category);
            ivCategory=itemView.findViewById(R.id.iv_category);
            giggleNumber=itemView.findViewById(R.id.tv_giggle_no);
            giggleBlack=itemView.findViewById(R.id.iv_giggle_black);
            replyNumber=itemView.findViewById(R.id.tv_reply_no);
            progressBar=itemView.findViewById(R.id.progress_bar_main);
            relativeLayoutParent=itemView.findViewById(R.id.relLayout_parent);
            relativeLayoutGrandParent=itemView.findViewById(R.id.relLayout_grandparent);

        }

    }

    public CommentProfileRecyclerViewAdapter(Context context, ArrayList<CommentLog> commentLogList,
                                             onCommentListener oncommentListener
                                             ) {
        mCommentLogList = commentLogList;
        mContext = context;
        this.mOnCommentListener = oncommentListener;
        reachEndOfDatabase=false;
    }


    @NonNull
    @Override
    public commentLogRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_fragment_comment_profile, parent, false);

        return new commentLogRecyclerViewHolder(v, mOnCommentListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final commentLogRecyclerViewHolder holder, final int position) {
        final CommentLog currentCommentLog = mCommentLogList.get(position);

        holder.progressBar.setVisibility(View.GONE);

        holder.commentGiggleNumber=0;
        holder.commentReplyNumber=0;

        try {

            if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(currentCommentLog.getCommenterId())) {

                String[] routeParts=currentCommentLog.getRoute().split("/");
                String cvId=routeParts[1];
                Log.d(TAG, "onEvent: cvID "+cvId);

                FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(mContext.getString(R.string.col_comment_giggle_notifications))
                        .whereEqualTo("commentId",currentCommentLog.getCommentId())
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
                                            holder.commentGiggleNumber++;
                                            updateCategoryView(currentCommentLog.getCategory(),holder);
                                            Log.d(TAG, "New city: " + dc.getDocument().getData());
                                            break;
                                        case MODIFIED:
                                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                            break;
                                        case REMOVED:
                                            holder.commentGiggleNumber--;
                                            updateCategoryView(currentCommentLog.getCategory(),holder);
                                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                            break;
                                    }
                                }

                            }
                        });


                FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .collection(mContext.getString(R.string.col_comment_reply_notifications))
                        .whereEqualTo("commentId",currentCommentLog.getCommentId())
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
                                            holder.commentGiggleNumber++;
                                            updateCategoryView(currentCommentLog.getCategory(),holder);
                                            Log.d(TAG, "New city: " + dc.getDocument().getData());
                                            break;
                                        case MODIFIED:
                                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                            break;
                                        case REMOVED:
                                            holder.commentGiggleNumber--;
                                            updateCategoryView(currentCommentLog.getCategory(),holder);
                                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                            break;
                                    }
                                }

                            }
                        });
            }

        } catch (NullPointerException e) {

        }

        if(position==mCommentLogList.size()-1 && (position+1)%10==0 ){
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.relativeLayoutParent.setVisibility(View.GONE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.progressBar.setVisibility(View.GONE);
                    holder.relativeLayoutParent.setVisibility(View.VISIBLE);
                }
            },500);

        }

        holder.commentLogDate.setText(DateDisplay.format(currentCommentLog.getTimestamp().toDate()));
        holder.commentLogContent.setText(currentCommentLog.getComment());
        holder.category.setText(currentCommentLog.getCategory());
        holder.giggleNumber.setText(String.valueOf(currentCommentLog.getGiggles()));
        holder.replyNumber.setText(String.valueOf(currentCommentLog.getChild_comments()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                oncomment_Listener.onCommentClick(getAdapterPosition());

                Intent intent=new Intent(mContext, ViewCommentActivity.class);
                intent.putExtra(mContext.getString(R.string.comment_log),currentCommentLog);

                ((ProfileActivity)mContext).startActivityForResult(intent,ProfileActivity.PROFILE_ACTIVITY_REQUEST_CODE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mCommentLogList.size();
    }


    public interface onCommentListener {
        void onCommentClick(int position);

        void OnDeletecommentClick(int position);
    }

    private void updateCategoryView(String category,commentLogRecyclerViewHolder holder){

        Log.d(TAG, "onEvent4: ");

        if(category.equals(mContext.getString(R.string.Football)) && (holder.commentGiggleNumber+holder.commentReplyNumber)<=0){

            holder.ivCategory.setImageResource(R.drawable.ic_football);
            return;
        } else if(category.equals(mContext.getString(R.string.Football)) && (holder.commentGiggleNumber+holder.commentReplyNumber)>0){


            holder.relativeLayoutGrandParent.setBackgroundColor(mContext.getResources().getColor(R.color.colorTintBlue));
            return;
        }

        if(category.equals(mContext.getString(R.string.Politics)) && (holder.commentGiggleNumber+holder.commentReplyNumber)<=0){

            holder.ivCategory.setImageResource(R.drawable.ic_politics);
            return;
        } else if(category.equals(mContext.getString(R.string.Politics)) && (holder.commentGiggleNumber+holder.commentReplyNumber)>0){

            holder.relativeLayoutGrandParent.setBackgroundColor(mContext.getResources().getColor(R.color.colorTintBlue));
            return;
        }

        if(category.equals(mContext.getString(R.string.Life)) && (holder.commentGiggleNumber+holder.commentReplyNumber)<=0){
            holder.ivCategory.setImageResource(R.drawable.ic_life);
            return;
        } else if(category.equals(mContext.getString(R.string.Life)) &&(holder.commentGiggleNumber+holder.commentReplyNumber)>0){

            holder.relativeLayoutGrandParent.setBackgroundColor(mContext.getResources().getColor(R.color.colorTintBlue));
            return;
        }

        if(category.equals(mContext.getString(R.string.Relationship)) && (holder.commentGiggleNumber+holder.commentReplyNumber)<=0){
            holder.ivCategory.setImageResource(R.drawable.ic_heart_circle);
        } else if(category.equals(mContext.getString(R.string.Relationship)) && (holder.commentGiggleNumber+holder.commentReplyNumber)>0){

            holder.relativeLayoutGrandParent.setBackgroundColor(mContext.getResources().getColor(R.color.colorTintBlue));
        }
    }

}
