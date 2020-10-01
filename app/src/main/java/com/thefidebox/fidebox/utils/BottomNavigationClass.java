package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.profile.ProfileActivity;
import com.thefidebox.fidebox.search.SearchActivity;
import com.thefidebox.fidebox.write_n_draft.Write_n_draftActivity;


public class BottomNavigationClass extends BottomNavigationView {


    private static final String TAG="BottomNavigationClass";

    public static long cv_giggle_number=0;
    public static long cv_comment_number=0;
    public static long comment_giggle_number=0;
    public static long comment_reply_number=0;
    int mACTIVITY_NUM;
    Context mContext;
    BottomNavigationView mBottomNavigationView;
    BadgeDrawable badge;

    public BottomNavigationClass(Context context, int ACTIVITY_NUM, BottomNavigationView bottomNavigationView){

        super(context);
        Log.d(TAG, "BottomNavigationClass: "+context);
        mContext=context;
        mBottomNavigationView=bottomNavigationView;
        mACTIVITY_NUM=ACTIVITY_NUM;
        badge = mBottomNavigationView.getOrCreateBadge(R.id.profilebtn);
        badge.setVisible(false);

    }


    public void setupBottomNavigation() {

        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = mBottomNavigationView.getMenu();

        MenuItem menuItem = menu.getItem(mACTIVITY_NUM);
        menuItem.setChecked(true);

        if(FirebaseAuth.getInstance().getCurrentUser()==null){

            BadgeDrawable badge = mBottomNavigationView.getOrCreateBadge(R.id.profilebtn);
            badge.setVisible(false);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                case R.id.homebtn:
                    Intent intent1 = new Intent(mContext, HomeActivity.class);
                    switchActivity(intent1);

                    break;
                case R.id.write_n_draftbtn:
                    Intent intent2 = new Intent(mContext, Write_n_draftActivity.class);
                    switchActivity(intent2);
                    break;
                case R.id.searchbtn:
                    Intent intent3 = new Intent(mContext, SearchActivity.class);
                    switchActivity(intent3);
                    break;
                case R.id.profilebtn:
                    Intent intent4 = new Intent(mContext, ProfileActivity.class);
                    switchActivity(intent4);
                    break;
            }
            return false;

        }
    };

    private void switchActivity(Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mContext.startActivity(intent);
        ((AppCompatActivity)mContext).overridePendingTransition(0,0);
    }
}
