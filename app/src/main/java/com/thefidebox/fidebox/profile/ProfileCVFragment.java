package com.thefidebox.fidebox.profile;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.utils.CVRecyclerViewAdapter;
import com.thefidebox.fidebox.view_models.ProfileCVViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileCVFragment extends Fragment implements CVRecyclerViewAdapter.OnCVListener {


    //OnLetterListener: Directing to ViewCommentActivity
    @Override
    public void OnCommentClick(int pos) {

    }

    public ProfileCVFragment() {
        super();
        setArguments(new Bundle());
    }


    //constants
    private final static int ACTIVITY_NUM = 4;
    private final static String TAG = "ProfileCVFragment";

    //views
    private RecyclerView mRecyclerView;
    private ProgressBar progressBarMain;
    private SwipeRefreshLayout refreshLayout;
    private RelativeLayout relativeLayoutEmpty;

    //var
    private CVRecyclerViewAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ProfileCVViewModel profileCVViewModel;
    private Context mContext;
    private boolean isScrolling;
    private String userIdToBeSearched;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cv, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view_letters);
        mContext = getActivity();

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        progressBarMain = view.findViewById(R.id.progress_bar_main);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        relativeLayoutEmpty=view.findViewById(R.id.relLayout_empty);

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
                profileCVViewModel.refresh();

            }
        });

        profileCVViewModel = new ViewModelProvider(getActivity()).get(ProfileCVViewModel.class);
        profileCVViewModel.setUserIdToBeSearched(userIdToBeSearched);

        profileCVViewModel.getRefreshLetter().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                refreshLayout.setRefreshing(aBoolean);

                if(!aBoolean){

                    mAdapter.notifyDataSetChanged();
                }
            }
        });


        profileCVViewModel.getIsUpdating().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {
                    progressBarMain.setVisibility(View.VISIBLE);
                } else {
                    progressBarMain.setVisibility(View.GONE);
                }

            }
        });

        profileCVViewModel.getCVs().observe(getViewLifecycleOwner(), new Observer<ArrayList<CV>>() {
            @Override
            public void onChanged(ArrayList<CV> CVS) {

                try {
                    //
                    mAdapter.notifyItemInserted(CVS.size() - 1);

                } catch (IndexOutOfBoundsException e) {

                }
            }
        });


        mAdapter = new CVRecyclerViewAdapter(mContext, profileCVViewModel.getmLetters().getValue(), ProfileCVFragment.this
                , ACTIVITY_NUM, (AppCompatActivity) getActivity());

//        mAdapter.setHasStableIds(true);

        profileCVViewModel.getmLetters().observe(getViewLifecycleOwner(), new Observer<ArrayList<CV>>() {
            @Override
            public void onChanged(ArrayList<CV> cvs) {

                Log.d(TAG, "onChanged: "+cvs.size());
                if(cvs.size()==0){

                    relativeLayoutEmpty.setVisibility(View.VISIBLE);
                /*    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        }
                    },500);*/
                } else{

                    relativeLayoutEmpty.setVisibility(View.GONE);
                }
            }
        });

        profileCVViewModel.getCanLoadMore().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    mAdapter.setReachEndOfDatabase(aBoolean);
                }
            }
        });


        mRecyclerView.setAdapter(mAdapter);

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
                        profileCVViewModel.loadLetters();

                    } else if ((lastCompletelyVisible + 1) == totalItems && dy == 0) {

                    }
                }
            }
        });

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



