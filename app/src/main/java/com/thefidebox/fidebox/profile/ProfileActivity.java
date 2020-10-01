package com.thefidebox.fidebox.profile;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.settings.SettingsActivity;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.models.Comment;
import com.thefidebox.fidebox.models.CommentLog;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.BottomNavigationClass;
import com.thefidebox.fidebox.utils.CustomTabLayout;
import com.thefidebox.fidebox.utils.CVRecyclerViewAdapter;
import com.thefidebox.fidebox.utils.SectionsPagerAdapter;
import com.thefidebox.fidebox.utils.ViewCommentActivity;
import com.thefidebox.fidebox.view_models.UserAccountSettingsProfileViewModel;

public class ProfileActivity extends AppCompatActivity {

    public void switchToViewLetterFragment(String letterId) {

        Intent intent = new Intent(mContext, ViewCommentActivity.class);
        intent.putExtra(mContext.getString(R.string.letter_id), letterId);

        startActivity(intent);
    }

    //CVRecyclerViewAdapter
    private final static int ACTIVITY_NUM = 3;
    private final static int LETTER_FRAGMENT = 0;
    public static final int PROFILE_ACTIVITY_REQUEST_CODE = 3;

    //View
    private BottomNavigationView bottomNavigationView;
    private ViewPager mViewPager;
    private TextView mLetter, mComment, mName, mBio, topBarTitle;
    private ImageView backArrow, btnMore, mProfilePicture;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private NestedScrollView mNestedScrollView;
    private AppBarLayout mAppbarLayout;
    BottomNavigationClass bottomNavigationClass;
    //Var
    private static final String TAG = "Profile Activity";

    private String userIdToBeSearched;
    private Context mContext;
    private Toolbar toolbar;
    private UserAccountSettingsProfileViewModel userAccountSettingsViewModel;
    private String mDisplayName = "";
    public CustomTabLayout tabLayout;
    private RequestManager glide;
    RequestOptions requestOptions;
    SectionsPagerAdapter sectionsPagerAdapter;
    ListenerRegistration registration;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.profile_bar);
        mContext = ProfileActivity.this;
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        tabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.view_pager);
        mComment = findViewById(R.id.tv_comments_no);
        mLetter = findViewById(R.id.tv_letters_no);
        mName = findViewById(R.id.tv_name);
        mBio = findViewById(R.id.tv_bio);
        mProfilePicture = findViewById(R.id.iv_profile_pic);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolBar);
        backArrow = findViewById(R.id.iv_backArrow);
        btnMore = findViewById(R.id.btn_more);
        mNestedScrollView = findViewById(R.id.nestedScrollView);
        mAppbarLayout = findViewById(R.id.AppBarLayout);
        topBarTitle = findViewById(R.id.tv_name_title);
        backArrow = findViewById(R.id.iv_backArrow);


        int dpValue = 75; // margin in dips
        float d = mContext.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d); // margin in pixels

        requestOptions = RequestOptions.overrideOf(margin, margin)
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        glide = Glide.with(this);

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_more_vert);
        toolbar.setOverflowIcon(drawable);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.super.onBackPressed();
            }
        });

        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        AppBarLayout appBarLayout = findViewById(R.id.AppBarLayout);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                Log.d(TAG, "onOffsetChanged: " + mDisplayName);
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    topBarTitle.setText(mDisplayName);
                    //set animation
                    isShow = true;
                } else if (isShow) {
                    topBarTitle.setText("");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        setupFirebaseAuth();
    }

    private void init() {

        //getting profile image and name
        userAccountSettingsViewModel = new ViewModelProvider(this).get(UserAccountSettingsProfileViewModel.class);
        userAccountSettingsViewModel.setUserIdToBeSearched(userIdToBeSearched);

        Log.d(TAG, "initing: ");
        userAccountSettingsViewModel.getUserAccountSettings().observe(this, new Observer<Users>() {
            @Override
            public void onChanged(Users userAccountSettings) {

                Log.d(TAG, "onChanged: " + userAccountSettings.toString());
                if (userAccountSettings != null) {
                    //getting user account

                    Glide.with(ProfileActivity.this).load(userAccountSettings.getProfile_photo())
                            .apply(requestOptions)
                            .into(mProfilePicture);

                    mDisplayName = userAccountSettings.getDisplay_name();
                    mName.setText(userAccountSettings.getDisplay_name());
                    mBio.setText(userAccountSettings.getBio());

                }
            }
        });

        userAccountSettingsViewModel.getLetterCount().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

                Log.d(TAG, "onChanged: " + s);

                if (s != null) {

                    mLetter.setText(s);
                }
            }
        });

        userAccountSettingsViewModel.getCommentCount().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {

                Log.d(TAG, "onChanged: " + s);
                if (s != null) {

                    mComment.setText(s);
                }
            }
        });

    }


    public void getIntentFromCallingClass() {
        try {
            Intent intent = getIntent();

            if ("CVRecyclerViewAdapter".equals(intent.getStringExtra("callingClass")) ||
                    "searchActivity".equals(intent.getStringExtra("callingClass"))) {
                userIdToBeSearched = intent.getStringExtra("userId");
            }


        } catch (NullPointerException e) {

        } finally {

            //to catch error when user is not logged in
            try {
                if (userIdToBeSearched == null) {
                    userIdToBeSearched = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
            } catch (NullPointerException e) {

            }
        }
    }

    /**
     * Responsible for adding the 3 tabs: Camera, Home, Messages
     */
    private void setupViewPager() {

        Log.d(TAG, "setupViewPager: ");
        Log.d(TAG, "setupViewPager: "+sectionsPagerAdapter);

        if (sectionsPagerAdapter != null &&
                !userIdToBeSearched.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&
                getIntent().getStringExtra("callingClass") == null) {
            sectionsPagerAdapter.removeFragment(0);
            sectionsPagerAdapter.removeFragment(0);
            sectionsPagerAdapter.notifyDataSetChanged();

        }

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ProfileCVFragment profileCVFragment = new ProfileCVFragment();
        Bundle args = new Bundle();
        args.putString("userId", userIdToBeSearched);
        profileCVFragment.setArguments(args);

        ProfileCommentFragment profileCommentFragment = new ProfileCommentFragment();
        profileCommentFragment.setArguments(args);

        sectionsPagerAdapter.addFragment(profileCVFragment); //index 0
        sectionsPagerAdapter.addFragment(profileCommentFragment); //index 1

        mViewPager.setAdapter(sectionsPagerAdapter);

        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.CVs));
        tabLayout.getTabAt(1).setText(getString(R.string.Comments));

        mViewPager.setCurrentItem(LETTER_FRAGMENT);

        updateCVBadgeNumber();
        updateCommentBadgeNumber();

    }

    public void updateCVBadgeNumber() {

        try {

            if (userIdToBeSearched.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && BottomNavigationClass.cv_giggle_number + BottomNavigationClass.cv_comment_number > 0) {
                BadgeDrawable badge = tabLayout.getTabAt(0).getOrCreateBadge();
                badge.setVisible(true);
                badge.setNumber((int) (BottomNavigationClass.cv_giggle_number + BottomNavigationClass.cv_comment_number));

            } else {
                BadgeDrawable badge = tabLayout.getTabAt(0).getOrCreateBadge();
                badge.setVisible(false);
            }
        } catch (NullPointerException e) {

        }
    }

    public void updateCommentBadgeNumber() {

        try {

            if (userIdToBeSearched.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && BottomNavigationClass.comment_giggle_number + BottomNavigationClass.comment_reply_number > 0) {
                BadgeDrawable badge = tabLayout.getTabAt(1).getOrCreateBadge();
                badge.setVisible(true);
                badge.setNumber((int) (BottomNavigationClass.comment_giggle_number + BottomNavigationClass.comment_reply_number));
            } else {
                BadgeDrawable badge = tabLayout.getTabAt(1).getOrCreateBadge();
                badge.setVisible(false);
            }
        } catch (NullPointerException e) {

        }

    }

    public void hideLayout() {
        mNestedScrollView.setVisibility(View.GONE);
        mAppbarLayout.setVisibility(View.GONE);
    }

    public void showLayout() {
        mNestedScrollView.setVisibility(View.VISIBLE);
        mAppbarLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

                if (firebaseAuth.getCurrentUser() == null) {
                    transitionToSignIn();
                }

            }
        };

    }

    private void transitionToSignIn() {
        Intent intent = new Intent(mContext, SignInActivity.class);
        finish();
        startActivity(intent);
        this.overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume: ");
        Log.d(TAG, "onResume: " + userIdToBeSearched);
        Log.d(TAG, "onResume: " + getIntent().getStringExtra("callingClass"));
        super.onResume();

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


        getIntentFromCallingClass();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            if (tabLayout != null) {
                updateCommentBadgeNumber();
                updateCVBadgeNumber();
            }

            try {
                if (!userIdToBeSearched.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && getIntent().getStringExtra("callingClass") == null) {
                    userIdToBeSearched = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    getViewModelStore().clear();
                    btnMore.setVisibility(View.GONE);
                    Log.d(TAG, "onResume11: clearing");
                    setupViewPager();
                    init();
                } else {
                    btnMore.setVisibility(View.VISIBLE);
                }
            } catch (NullPointerException e) {

            }

            // User is signed in
            setupViewPager();
            init();
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
        getIntent().removeExtra("callingClass");

        if (registration != null) {
            registration.remove();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: ");
        if (requestCode == PROFILE_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                //Updating commentLog after changing the commmentLog in the View Comment Activity
                ProfileCommentFragment profileCommentFragment = (ProfileCommentFragment) sectionsPagerAdapter.getItem(1);

                if (profileCommentFragment != null) {
                    profileCommentFragment.changeCommentLog((CommentLog) data.getParcelableExtra("commentLog"));

                }

            }
        }
    }


    void updateBadge() {

        try {

            if (FirebaseAuth.getInstance().getCurrentUser() != null && ((ProfileActivity) mContext).tabLayout != null) {

                ((ProfileActivity) mContext).updateCommentBadgeNumber();
                ((ProfileActivity) mContext).updateCVBadgeNumber();
            }

        } catch (ClassCastException e) {

        }

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



