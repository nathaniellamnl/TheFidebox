package com.thefidebox.fidebox.search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.profile.ProfileActivity;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.BottomNavigationClass;
import com.thefidebox.fidebox.utils.UserListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    //vars
    private List<Users> mUserList;
    private UserListAdapter mAdapter;
    private static final String TAG = "Search Activity";
    private static final int ACTIVITY_NUM = 2;
    private Context mContext;
    private BottomNavigationClass bottomNavigationClass;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    ListenerRegistration registration;

    //views
    private EditText mSearchParam;
    private ListView mListView;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchParam = findViewById(R.id.search);
        mListView = findViewById(R.id.listView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mContext = SearchActivity.this;

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        initTextListener();
        hideSoftKeyboard();
        setupFirebaseAuth();
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");

        mUserList = new ArrayList<>();

        mSearchParam.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = mSearchParam.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch(String keyword) {
        Log.d(TAG, "searchForMatch: searching for a match: " + keyword);
        mUserList.clear();
        //update the users list view
        if (keyword.length() == 0) {

        } else {

            FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                    .whereEqualTo(getString(R.string.field_username), keyword)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    mUserList.add(document.toObject(Users.class));
                                    //update the users list view
                                    updateUsersList();
                                }
                            } else {

                            }
                        }
                    });

        }
    }

    private void updateUsersList() {
        Log.d(TAG, "updateUsersList: updating users list");

        mAdapter = new UserListAdapter(SearchActivity.this, R.layout.layout_user_listitem, mUserList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: selected user: " + mUserList.get(position).toString());

                //navigate to profile activity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra("callingClass", "searchActivity");
                intent.putExtra("userId", mUserList.get(position).getUserId());
                startActivity(intent);
            }
        });
    }


    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

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
            this.overridePendingTransition(0,0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        BottomNavigationView bottomNavigationView=findViewById(R.id.bottom_navigation);
        bottomNavigationClass=new  BottomNavigationClass(this, ACTIVITY_NUM, bottomNavigationView);
        bottomNavigationClass.setupBottomNavigation();


        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            registration= FirebaseFirestore.getInstance().collection("users")
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
                                BottomNavigationClass.comment_reply_number=(long) snapshot.getData().get("comment_reply_notifications");
                                BottomNavigationClass.comment_giggle_number = (long) snapshot.getData().get("comment_giggle_notifications");
                                BottomNavigationClass.cv_giggle_number=(long)snapshot.getData().get("cv_giggle_notifications");
                                BottomNavigationClass.cv_comment_number=(long)snapshot.getData().get("cv_comment_notifications");

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
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

        if(registration!=null){
            registration.remove();
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
