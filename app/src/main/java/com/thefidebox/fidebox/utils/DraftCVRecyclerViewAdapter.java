package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thefidebox.fidebox.models.DraftCV;
import com.thefidebox.fidebox.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DraftCVRecyclerViewAdapter extends RecyclerView.Adapter<DraftCVRecyclerViewAdapter.DraftLetterRecyclerViewHolder> {


    private static final String TAG = "DraftLetterAdapter";
    private OnDraftListener mOnDraftListener;


    private ArrayList<DraftCV> mDraftList;
    Context mContext;

    public class DraftLetterRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView mDraft, mDraftCategory, mDraftFirstFailure, mDraftDate;
        ImageView mDelete;
        RelativeLayout mRelLayoutProgressBar;

        DraftLetterRecyclerViewHolder(@NonNull View itemView, OnDraftListener onDraftListener1) {
            super(itemView);

            mDraft = itemView.findViewById(R.id.tv_draft);
            mDraftCategory = itemView.findViewById(R.id.tv_draft_category);
            mDraftFirstFailure = itemView.findViewById(R.id.tv_draft_failure_1);
            mDraftDate = itemView.findViewById(R.id.tv_draft_date);
            mDelete = itemView.findViewById(R.id.iv_delete);
            mRelLayoutProgressBar = itemView.findViewById(R.id.relLayout_progress_bar);

            final OnDraftListener ondraftListener = onDraftListener1;

            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ondraftListener.OnDeleteClick(getAdapterPosition());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ondraftListener.onDraftClick(getAdapterPosition());
                }
            });
        }

    }

    public DraftCVRecyclerViewAdapter(Context context, ArrayList<DraftCV> DraftletterList, OnDraftListener onDraftListener) {
        mDraftList = DraftletterList;
        mContext = context;
        this.mOnDraftListener = onDraftListener;
    }


    @NonNull
    @Override
    public DraftLetterRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_draft, parent, false);


        return new DraftLetterRecyclerViewHolder(v, mOnDraftListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final DraftLetterRecyclerViewHolder holder, final int position) {
        DraftCV currentDraftLetter = mDraftList.get(position);
        Log.d(TAG, "onBindViewHolder: " + currentDraftLetter.toString());

        try {
            holder.mDraftDate.setText(DateDisplay(currentDraftLetter.getTimestamp().toDate()));
        } catch (NullPointerException e) {
            holder.mDraftDate.setText("");
        }

    }

    @Override
    public int getItemCount() {
        return mDraftList.size();
    }

    protected String DateDisplay(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        String dateString = sdf.format(date);

        String yr = dateString.substring(0, 4);
        String month = dateString.substring(5, 7);
        String day = dateString.substring(8, 10);

        return day + "/" + month + "/" + yr;
    }

    public interface OnDraftListener {
        void onDraftClick(int position);

        void OnDeleteClick(int position);
    }



}
