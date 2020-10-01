package com.thefidebox.fidebox.home;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.utils.CVRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment implements CVRecyclerViewAdapter.OnCVListener {

    @Override
    public void OnCommentClick(int position) {

        Log.d(TAG, "OnCommentClick: " + position);


        mCVRecyclerViewAdapter.notifyItemChanged(position);
    }

    public CategoryFragment() {
        super();
        setArguments(new Bundle());
    }

    //Constant
    private static final String TAG = "CategoryFragment";
    private static final int ACTIVITY_NUMBER = 0;

    //Views
    private RecyclerView mRecyclerView;
    ProgressBar mProgressBar, mainProgressBar;
    private SwipeRefreshLayout mRefreshLayout;
    RelativeLayout mRelativeLayout;

    //Variable
    private ArrayList<CV> mCVS;
    private CV lastCV, lastKeyCV;
    private CVRecyclerViewAdapter mCVRecyclerViewAdapter;
    private Context mContext;
    private LinearLayoutManager mLayoutManager;
    private String mCategory=null;
    Boolean isScrolling = false;
    GiggleCommentReceiver giggleCommentReceiver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_category, container, false);

        mContext = getActivity();

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mProgressBar = view.findViewById(R.id.progress_bar_bottom);
        mRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        mRelativeLayout = view.findViewById(R.id.relLayout);
        mainProgressBar = view.findViewById(R.id.progress_bar_main);

        mCVS = new ArrayList<>();
        mContext = getActivity();

        mCategory = getCategoryFromBundle();

        mRecyclerView.setVisibility(View.INVISIBLE);
        mainProgressBar.setVisibility(View.VISIBLE);

        mRecyclerView.setItemViewCacheSize(20);


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

                        isScrolling = false;

                        fetchData();
                    } else if ((lastCompletelyVisible + 1) == totalItems && dy == 0) {

                    }
                }

            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                mRefreshLayout.setEnabled(true);
            }
        }, 500);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                                @Override
                                                public void onRefresh() {
                                                    Log.d(TAG, "onRefresh: ");

                                                    if (mCategory != null) {


                                                        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(mCategory);

                                                        final Query query = collectionReference

                                                                .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                                                                .limit(10);

                                                        query
                                                                .get()
                                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            mCVS.clear();
                                                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                                                CV CV = document.toObject(CV.class);
                                                                                Log.d(TAG, document.getId() + " => " + document.getData());
                                                                                mCVS.add(CV);
                                                                            }

                                                                            mCVRecyclerViewAdapter.notifyDataSetChanged();
                                                                            mRefreshLayout.setRefreshing(false);

                                                                            try {

                                                                                lastCV = mCVS.get(mCVS.size() - 1);

                                                                            } catch (IndexOutOfBoundsException e) {
                                                                                //to be amended, no letter present
                                                                            }

                                                                        } else {
                                                                            Log.d(TAG, "Error getting documents: ", task.getException());

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
        );

        getLastKeyCV();
        init();
        return view;
    }

    private void fetchData() {

        if(mCategory!=null){


        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(mCategory);
        final Query query;


        //make sure last key CV is obtained first
        try {
            if (lastCV.getCvId().equals(lastKeyCV.getCvId())) {
                mCVRecyclerViewAdapter.setReachEndOfDatabase(true);
                return;
            }
        } catch (NullPointerException e) {

        }

        if (lastCV == null) {
            query = collectionReference
                    .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                    .limit(10);
        } else {
            query = collectionReference
                    .orderBy(mContext.getString(R.string.field_timestamp), Query.Direction.DESCENDING)
                    .startAfter(lastCV.getTimestamp())
                    .limit(10);
        }

        query
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                CV CV = document.toObject(CV.class);
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                mCVS.add(CV);

                                mCVRecyclerViewAdapter.notifyItemInserted(mCVS.size() - 1);
                            }

                            try {

                                lastCV = mCVS.get(mCVS.size() - 1);

                            } catch (IndexOutOfBoundsException e) {
                                //to be amended, no letter present
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    mainProgressBar.setVisibility(View.GONE);
                                    mRecyclerView.setVisibility(View.VISIBLE);
                                }
                            }, 500);


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());

                        }
                    }
                });

        }

    }

    private void init() {

        mCVRecyclerViewAdapter = new CVRecyclerViewAdapter(mContext, mCVS, CategoryFragment.this, 0, (AppCompatActivity) getActivity());
        //   mCVRecyclerViewAdapter.setHasStableIds(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mCVRecyclerViewAdapter);

        fetchData();
    }


    private String getCategoryFromBundle() {

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            return bundle.getString(mContext.getString(R.string.field_category));

        } else {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (giggleCommentReceiver == null) {
            giggleCommentReceiver = new GiggleCommentReceiver();
        }
        LocalBroadcastManager.getInstance(mContext).registerReceiver(giggleCommentReceiver,
                new IntentFilter(mContext.getString(R.string.GIGGLE_COMMENT_CHANGE)));

    }


    @Override
    public void onDetach() {

        if (giggleCommentReceiver != null) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(giggleCommentReceiver);
        }
        super.onDetach();
    }


    public void updateRVItem(int pos, CV cv) {

        try {

            mCVS.set(pos, cv);
            mCVRecyclerViewAdapter.notifyItemChanged(pos);
        } catch (IndexOutOfBoundsException e) {

        }

    }

    private class GiggleCommentReceiver extends BroadcastReceiver {

        public GiggleCommentReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: " + intent.getStringExtra("giggleByCurrentUser"));

            String cvId = intent.getStringExtra("giggleByCurrentUser");

            for (int i = 0; i < mCVS.size(); i++) {

                if (cvId.equals(mCVS.get(i).getCvId())) {

                    TextView giggleNumber = mRecyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.tv_giggle_no);

                    Log.d(TAG, "onReceive 2: " + giggleNumber.getText().toString());

                    CV changedCV = mCVS.get(i);

                    Log.d(TAG, "onReceive 2: " + changedCV.getGiggles());
                    if (intent.getBooleanExtra("giggleByCurrentUserBoolean", false)) {

                        changedCV.setGiggles(changedCV.getGiggles() + 1);

                        mCVS.set(i, changedCV);

                        mCVRecyclerViewAdapter.notifyItemChanged(i);

                    } else {

                        changedCV.setGiggles(changedCV.getGiggles() - 1);

                        mCVS.set(i, changedCV);
                        mCVRecyclerViewAdapter.notifyItemChanged(i);

                    }
                }
            }
        }
    }

    private void getLastKeyCV() {

        if (mCategory != null) {

            FirebaseFirestore.getInstance().collection(mCategory)
                    .orderBy(mContext.getString(R.string.field_timestamp), com.google.firebase.firestore.Query.Direction.ASCENDING)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    lastKeyCV = document.toObject(CV.class);

                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }

    }

}


