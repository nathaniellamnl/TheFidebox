package com.thefidebox.fidebox.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.Token;
import com.thefidebox.fidebox.models.UserPrivateInfo;
import com.thefidebox.fidebox.models.Username;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.profile.ProfileActivity;
import com.thefidebox.fidebox.settings.SettingsActivity;
import com.thefidebox.fidebox.utils.BottomNavigationClass;
import com.thefidebox.fidebox.utils.CVRecyclerViewAdapter;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements CVRecyclerViewAdapter.OnCVListener {

   /* @Override
    public void OnMoreOptionsLetterClick(int position) {

    }*/

    @Override
    public void OnCommentClick(int position) {

    }


    RequestManager glide;
    RequestOptions requestOptions;

    //Constant
    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    public static final String TOKEN_BROADCAST = "myfcmtokenbroadcast";
    public static final int HOME_ACTIVITY_REQUEST_CODE = 0;

    //view
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout, mRelLayoutParent;
    private CoordinatorLayout coordinatorLayout;
    private BottomNavigationView bottomNavigationView;
    private TextView appBarTextView;
    BottomNavigationClass bottomNavigationClass;
    ListenerRegistration registration;
    ImageView profilePhoto;

    //var
    private Context mContext;

    //Listener


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setUpUI();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                drawerLayout.closeDrawer(GravityCompat.START);

                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);

                int id = item.getItemId();
                CategoryFragment categoryFragment1 = new CategoryFragment();
                Bundle args1 = new Bundle();

                if (id == R.id.life) {
                    args1.putString(getString(R.string.field_category), getString(R.string.Life));
                    navigationView.setCheckedItem(R.id.life);
                    appBarTextView.setText(getString(R.string.Life));

                } else if (id == R.id.relationship) {
                    args1.putString(getString(R.string.field_category), getString(R.string.Relationship));
                    navigationView.setCheckedItem(R.id.relationship);
                    appBarTextView.setText(getString(R.string.Relationship));
                } else if (id == R.id.football) {

                    args1.putString(getString(R.string.field_category), getString(R.string.Football));
                    navigationView.setCheckedItem(R.id.football);
                    appBarTextView.setText(getString(R.string.Football));

                } else if (id == R.id.politics) {
                    args1.putString(getString(R.string.field_category), getString(R.string.Politics));

                    navigationView.setCheckedItem(R.id.politics);
                    appBarTextView.setText(getString(R.string.Politics));

                } else if (id == R.id.settings) {
                    Intent intent = new Intent(mContext, SettingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    HomeActivity.this.overridePendingTransition(0, 0);

                    navigationView.getMenu().getItem(4).setChecked(false);

                    return false;
                }


                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment = fm.findFragmentByTag(getString(R.string.category_fragment));

                if (fragment != null) {
                    fm.beginTransaction().remove(fragment).commit();
                }

                categoryFragment1.setArguments(args1);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container, categoryFragment1, getString(R.string.category_fragment));
                transaction.commit();

                return false;
            }
        });

        //make sure the fragment produced is always the newest
        navigationView.setCheckedItem(R.id.football);
        createFragment();

        getUserObjectToDatabase();


        this.glide = Glide.with(mContext);

        //set glide image size
        int dpValue = 40; // margin in dips
        float d = mContext.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d); // margin in pixels

        requestOptions = RequestOptions.overrideOf(margin, margin)
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
    }

    private void setUpUI() {

        //Navigation drawer
        mContext = HomeActivity.this;
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        mFrameLayout = findViewById(R.id.container);
        toolbar = findViewById(R.id.toolBar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        appBarTextView = findViewById(R.id.appBarTextView);

        View headerView=navigationView.inflateHeaderView(R.layout.header_home);
        profilePhoto = headerView.findViewById(R.id.profile_photo);

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0,0);
            }
        });

        setSupportActionBar(toolbar);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    Users users=document.toObject(Users.class);

                                    glide.load(users.getProfile_photo())
                                            .apply(requestOptions)
                                            .into(profilePhoto);


                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav_drawer, R.string.close_nav_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

    }


    private void createFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(getString(R.string.category_fragment));

        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commit();
        }

        CategoryFragment categoryFragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString(getString(R.string.field_category), getString(R.string.Football));
        appBarTextView.setText(getString(R.string.Football));
        categoryFragment.setArguments(args);


        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, categoryFragment, getString(R.string.category_fragment));

        transaction.commit();

    }


    private void getUserObjectToDatabase() {

        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.calling_activity))) {


            Log.d(TAG, "getUserObjectToDatabase: " + getIntent().getExtras().getParcelable(getString(R.string.users)).toString());
            Log.d(TAG, "getUserObjectToDatabase: " + getIntent().getExtras().getParcelable(getString(R.string.token)).toString());

            try {

                Users settings = getIntent().getExtras().getParcelable(getString(R.string.users));
                Token token = getIntent().getExtras().getParcelable(getString(R.string.token));
                UserPrivateInfo userPrivateInfo = getIntent().getExtras().getParcelable("userPrivateInfo");


                WriteBatch batch = FirebaseFirestore.getInstance().batch();

                DocumentReference acRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users)).document(settings.getUserId());
                batch.set(acRef, settings);

                DocumentReference acPrivateRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                        .document(settings.getUserId())
                        .collection("private")
                        .document("private");
                batch.set(acPrivateRef, userPrivateInfo);

                Username username = new Username();
                username.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                username.setUsername(settings.getUsername());

                DocumentReference usernameRef = FirebaseFirestore.getInstance().collection("username")
                        .document(username.getUsername());
                batch.set(usernameRef, username);

                DocumentReference tokenRef = FirebaseFirestore.getInstance().collection(getString(R.string.col_users)).document(settings.getUserId())
                        .collection(getString(R.string.col_token)).document(token.getTokenId());
                batch.set(tokenRef, token);

                batch.commit()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                new MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                        .setTitle(getString(R.string.Error))
                                        .setPositiveButton(getString(R.string.OK), null)
                                        .show();

                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                new MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                        .setTitle(getString(R.string.sign_up_success))
                                        .setPositiveButton(getString(R.string.OK), null)
                                        .show();

                            }
                        });

            } catch (NullPointerException e) {

                Log.d(TAG, "getUserObjectToDatabase: " + e);
            }
        }

    }


    @Override
    public void onStart() {

        Log.d(TAG, "onStart: ");
        super.onStart();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try{
            //once in the foreground,cancel notification in the phone
            notificationManager.cancel(FCMService.COMMENT_NOTIFICATION_ID);
            notificationManager.cancel(FCMService.CV_NOTIFICATION_ID);
        } catch (NullPointerException e){

        }
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume: ");
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


    }

    @Override
    protected void onStop() {

        Log.d(TAG, "onStop: ");
        super.onStop();

        if (registration != null) {
            registration.remove();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }


    //receive from View Comment Activity/Reply Fragment to see if any change in comment/giggle and broadcast to category fragment
    // and profile activty/ CV fragment
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: ");

        if (requestCode == HOME_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                FragmentManager fm = getSupportFragmentManager();

                CategoryFragment categoryFragment = (CategoryFragment) fm.findFragmentByTag(getString(R.string.category_fragment));
                if (categoryFragment != null) {

                    categoryFragment.updateRVItem(data.getIntExtra("giggleByCurrentUser", -1), (CV) data.getParcelableExtra("cv"));
                }

            }

        }
    }


    void updateBadge() {

        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.profilebtn);
        badge.setVisible(false);
        if (BottomNavigationClass.cv_giggle_number > 0 || BottomNavigationClass.cv_comment_number > 0 ||
                BottomNavigationClass.comment_giggle_number > 0 || BottomNavigationClass.comment_reply_number > 0) {

            Log.d(TAG, "updateBadge: number>0");
            badge.setVisible(true);
            badge.setBadgeGravity(BadgeDrawable.TOP_END);

        } else {

            badge.setVisible(false);
        }

    }

}
