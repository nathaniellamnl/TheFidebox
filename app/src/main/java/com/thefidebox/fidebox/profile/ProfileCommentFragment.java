package com.thefidebox.fidebox.profile;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
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

import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.utils.CommentProfileRecyclerViewAdapter;
import com.thefidebox.fidebox.view_models.CommentLogViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileCommentFragment extends Fragment implements CommentProfileRecyclerViewAdapter.onCommentListener{

    @Override
    public void onCommentClick(int position) {

    }

    @Override
    public void OnDeletecommentClick(int position) {

    }

    private static final String MODEL = "CommentLogViewModel";
    private static final String TAG = "ProfileCommentFragment";
    private static final int ACTIVITY_NUM = 4;

    //views
    private RecyclerView mRecyclerView;
    private ProgressBar progressBarMain;
    private SwipeRefreshLayout refreshLayout;
    private RelativeLayout relativeLayoutEmpty;

    //var
    private CommentProfileRecyclerViewAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private CommentLogViewModel commentLogViewModel;
    private Context mContext;
    private boolean isScrolling;
    private int lastCompletelyVisible;
    private String userIdToBeSearched;



    public ProfileCommentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view_letters);
        mContext = getActivity();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        progressBarMain = view.findViewById(R.id.progress_bar_main);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        relativeLayoutEmpty=view.findViewById(R.id.relLayout_empty);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        userIdToBeSearched=getUserIdToBeSearchedFromBundle();
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                commentLogViewModel.refresh();
            }
        });

        commentLogViewModel = new ViewModelProvider(getActivity()).get(CommentLogViewModel.class);
        commentLogViewModel.setUserIdToBeSearched(userIdToBeSearched);

        commentLogViewModel.getRefreshComment().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                refreshLayout.setRefreshing(aBoolean);
                mAdapter.notifyDataSetChanged();
            }
        });

        commentLogViewModel.getCommentLogs().observe(getViewLifecycleOwner(), new Observer<ArrayList<CommentLog>>() {
                    @Override
                    public void onChanged(ArrayList<CommentLog> commentLogs) {
                        try {
                            mAdapter.notifyItemInserted(commentLogs.size() - 1);

                        } catch (IndexOutOfBoundsException e) {

                        }
                    }
                }
        );

        commentLogViewModel.getIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {
                    progressBarMain.setVisibility(View.VISIBLE);
                } else {
                    progressBarMain.setVisibility(View.GONE);
                }

            }
        });

        mAdapter = new CommentProfileRecyclerViewAdapter(mContext, commentLogViewModel.getmCommentLogs().getValue(), this);
        mRecyclerView.setAdapter(mAdapter);

        commentLogViewModel.getmCommentLogs().observe(getViewLifecycleOwner(), new Observer<ArrayList<CommentLog>>() {
            @Override
            public void onChanged(ArrayList<CommentLog> commentLogs) {
                if(commentLogs.size()==0){

                    relativeLayoutEmpty.setVisibility(View.VISIBLE);

                } else{
                    relativeLayoutEmpty.setVisibility(View.GONE);
                }

            }
        });

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
                lastCompletelyVisible = mLayoutManager.findLastCompletelyVisibleItemPosition();

                if (isScrolling) {
                    if ((lastCompletelyVisible + 1) == totalItems) {

                        isScrolling = false;

                        commentLogViewModel.getCanLoadMore().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean aBoolean) {

                                if(!aBoolean){
                                    mAdapter.setReachEndOfDatabase(true);
                                }
                            }
                        });

                    } else {

                    }
                }

            }
        });


    }


    public void changeCommentLog(CommentLog commentLog){


        Log.d(TAG, "changeCommentLog: ");

        for(int i=0;i<commentLogViewModel.getCommentLogs().getValue().size();i++){
            ArrayList<CommentLog> commentLogs=commentLogViewModel.getCommentLogs().getValue();

            if(commentLog.getCommentId().equals(commentLogs.get(i).getCommentId())){
                commentLogs.set(i,commentLog);
                commentLogViewModel.setmCommentLogs(commentLogs);
                mAdapter.notifyItemChanged(i);
                break;
            }
        }

    }



    private String getUserIdToBeSearchedFromBundle() {

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            return bundle.getString("userId");

        } else {
            return null;
        }
    }


}
