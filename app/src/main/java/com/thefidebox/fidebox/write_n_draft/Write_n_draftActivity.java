package com.thefidebox.fidebox.write_n_draft;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.models.DraftCV;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.BottomNavigationClass;
import com.thefidebox.fidebox.utils.DraftCVRecyclerViewAdapter;
import com.thefidebox.fidebox.utils.ToastMaker;
import com.thefidebox.fidebox.view_models.DraftViewModel;

import java.util.ArrayList;

public class Write_n_draftActivity extends AppCompatActivity implements DraftCVRecyclerViewAdapter.OnDraftListener {

    @Override
    public void onDraftClick(int position) {

        Intent intent = new Intent(this, WriteActivity.class);
        intent.putExtra("draftCV", mDraftViewModel.getmDraftCVs().getValue().get(position));
        intent.putExtra("position", position);
        startActivityForResult(intent, WRITE_N_DRAFT_REQUEST_CODE);
    }

    @Override
    public void OnDeleteClick(final int position) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setCancelable(true)
                .setMessage(getString(R.string.confirm_delete))
                .setNegativeButton(getString(R.string.Cancel), null)
                .setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDraftViewModel.RemoveDraftLetterFromDatabase(mDraftViewModel.getmDraftCVs().getValue().get(position).getCvId(), position);

                    }
                });

        builder.show();
    }

    //constants
    private static final String TAG = "Write_n_draft Activity";
    private static final int ACTIVITY_NUM = 1;
    private static final int WRITE_N_DRAFT_REQUEST_CODE = 4;

    //views
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar mProgressBarMain, mProgressBar;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout, mRelLayoutEmptyDraft;
    private SwipeRefreshLayout mRefreshLayout;
    private BottomNavigationView bottomNavigationView;

    //var
    private DraftCVRecyclerViewAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private DraftViewModel mDraftViewModel;
    private boolean isScrolling;
    private int lastCompletelyVisible;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    BottomNavigationClass bottomNavigationClass;
    ListenerRegistration registration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_n_draft);

        mContext = Write_n_draftActivity.this;

        mFrameLayout = findViewById(R.id.container_frameLayout);
        mRelativeLayout = findViewById(R.id.relLayoutParent);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mRefreshLayout = findViewById(R.id.refreshLayout);

        mRelLayoutEmptyDraft = findViewById(R.id.relLayout_empty_draft);
        mProgressBarMain = findViewById(R.id.progress_bar_main);
        mProgressBar = findViewById(R.id.progress_bar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        mProgressBar.setVisibility(View.GONE);
        final FloatingActionButton mfab = findViewById(R.id.fab);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(false);

        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setItemViewCacheSize(20);

        //ViewModels
        mDraftViewModel = new ViewModelProvider(this).get(DraftViewModel.class);

        //for initial set up of RV items
        mDraftViewModel.getDrafts().observe(this, new Observer<ArrayList<DraftCV>>() {
            @Override
            public void onChanged(ArrayList<DraftCV> draftLetters) {
                Log.d(TAG, "DraftletteronChanged: " + draftLetters.size());

                mAdapter.notifyItemInserted(draftLetters.size() - 1);

            }
        });

        mAdapter = new DraftCVRecyclerViewAdapter(mContext, mDraftViewModel.getmDraftCVs().getValue(), this);

        mRecyclerView.setAdapter(mAdapter);

        //show progress bar while loading
        mDraftViewModel.getIsUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

                if (aBoolean) {
                    mProgressBarMain.setVisibility(View.VISIBLE);

                } else {
                    mProgressBarMain.setVisibility(View.GONE);

                }
            }
        });

        //check if initial draft set is empty.if so, show empty
        mDraftViewModel.getIsEmpty().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    mRelLayoutEmptyDraft.setVisibility(View.VISIBLE);
                }
            }
        });

        mDraftViewModel.getPositionToBeDeleted().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                deleteFromDraftLetterList(integer);
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

                        mDraftViewModel.getCanLoadMore().observe(Write_n_draftActivity.this, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean aBoolean) {

                                Log.d(TAG, "canloadmoreonChanged: " + aBoolean);
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRefreshLayout.getLayoutParams();
                                if (aBoolean) {
                                    int dpValue = 50; // margin in dips
                                    float d = mContext.getResources().getDisplayMetrics().density;
                                    int margin = (int) (dpValue * d); // margin in pixels
                                    params.setMargins(0, 0, 0, margin);
                                    mLayoutManager.scrollToPositionWithOffset(lastCompletelyVisible, 0);
                                    mProgressBar.setVisibility(View.VISIBLE);

                                    //    mRefreshLayout.setLayoutParams(params);

                                } else {
                                    mProgressBar.setVisibility(View.GONE);
                                    params.setMargins(0, 0, 0, 0);

                                    //  mRefreshLayout.setLayoutParams(params);
                                }
                                mRefreshLayout.setLayoutParams(params);
                            }
                        });

                        //mDraftViewModel
                    } else {

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mRefreshLayout.getLayoutParams();
                        params.setMargins(0, 0, 0, 0);
                        mRefreshLayout.setLayoutParams(params);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

            }
        });

        mfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Write_n_draftActivity.this, WriteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(intent, WRITE_N_DRAFT_REQUEST_CODE);
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        setupFirebaseAuth();
    }

    public void hideLayout() {
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
        bottomNavigationView.setVisibility(View.GONE);
    }


    public void showLayout() {
        Log.d(TAG, "hideLayout: showing layout");

        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
        bottomNavigationView.setVisibility(View.VISIBLE);
    }


    //update the current list after successfully deleting from the database
    public void deleteFromDraftLetterList(int position) {

        mAdapter.notifyItemRemoved(position);

        if (mAdapter.getItemCount() == 0) {
            mRelLayoutEmptyDraft.setVisibility(View.VISIBLE);
        }
    }

       /*
    ------------------------------------ Firebase ---------------------------------------------
     */

    /**
     * Setup the firebase auth object
     */
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
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: ");


        bottomNavigationClass = new BottomNavigationClass(this, ACTIVITY_NUM, bottomNavigationView);
        bottomNavigationClass.setupBottomNavigation();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            registration = FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("--stat--")
                    .document("notifications")
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                BottomNavigationClass.comment_reply_number = (long) snapshot.getData().get("comment_reply_notifications");
                                BottomNavigationClass.comment_giggle_number = (long) snapshot.getData().get("comment_giggle_notifications");
                                BottomNavigationClass.cv_giggle_number = (long) snapshot.getData().get("cv_giggle_notifications");
                                BottomNavigationClass.cv_comment_number = (long) snapshot.getData().get("cv_comment_notifications");

                                updateBadge();

                                Log.d(TAG, "Current data: " + snapshot.getData());
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });
        }

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is signed in
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            try{
                //once in the foreground,cancel notification in the phone
                notificationManager.cancel(FCMService.COMMENT_NOTIFICATION_ID);
                notificationManager.cancel(FCMService.CV_NOTIFICATION_ID);
            } catch (NullPointerException e){

            }
        }
    }

    @Override
    public void onStart() {

        Log.d(TAG, "onStart: ");
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {

        Log.d(TAG, "onStop: ");
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        if (registration != null) {
            registration.remove();

        }
    }


    /**
     * only for receiving from the back navigation of WriteActivity but not for the the successful send from WriteActivity
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);


        //from WriteActivity
        if (requestCode == WRITE_N_DRAFT_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                //get result from FirebaseMethods(WriteActivity) and update the current list
                try {

                    String action = data.getStringExtra("action");

                    DraftCV draft = data.getParcelableExtra("draft");

                    Log.d(TAG, "onActivityResult1: " + action);
                    Log.d(TAG, "onActivityResult2: " + draft.toString());

                    if (action.equals("add")) {

                        mDraftViewModel.addDraftToList(draft);
                        mAdapter.notifyItemInserted(0);

                        mRelLayoutEmptyDraft.setVisibility(View.GONE);

                    } else if (action.equals("update")) {
                        int pos = data.getIntExtra("pos", -1);
                        if (pos >= 0) {

                            mDraftViewModel.updateDraft(draft, pos);
                            mAdapter.notifyItemRemoved(pos);
                            mAdapter.notifyItemInserted(0);

                        }
                    }

                } catch (NullPointerException e) {

                }


                //if add/update fails, then show this
                try {
                    if (data.getStringExtra("draftResult").equals("fail")) {
                        ToastMaker.showToastMaker(this, getString(R.string.save_failed));
                    }

                } catch (NullPointerException e) {

                }

            }
        }
    }

    void updateBadge() {

        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.profilebtn);
        if (BottomNavigationClass.cv_giggle_number > 0 || BottomNavigationClass.cv_comment_number > 0 ||
                BottomNavigationClass.comment_giggle_number > 0 || BottomNavigationClass.comment_reply_number > 0) {

            badge.setVisible(true);
            badge.setBadgeGravity(BadgeDrawable.TOP_END);

        } else {

            badge.setVisible(false);
        }

    }


}
