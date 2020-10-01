package com.thefidebox.fidebox.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.profile.EditProfileActivity;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG="SettingsActivity";

    private Context mContext=this;
    private ListView listView;
    private RelativeLayout container;
    private FrameLayout frameLayout;
    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        listView = findViewById(R.id.listview);
        container=findViewById(R.id.container_relLayout);
        frameLayout=findViewById(R.id.container_frameLayout);
        backArrow=findViewById(R.id.iv_backArrow);

        String[] settingsStrings = getResources().getStringArray(R.array.settings_items);
        int[] drawableIds = {R.drawable.ic_profile_edit,R.drawable.ic_bell_outline,
                R.drawable.ic_account_circle_black, R.drawable.ic_mail_outline,
                R.drawable.ic_info,R.drawable.ic_info, R.drawable.ic_star};

        SettingsListViewAdapter settingsListViewAdapter = new SettingsListViewAdapter(this, settingsStrings, drawableIds);
        listView.setAdapter(settingsListViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();

                switch (position) {
                    case 0:
                        Intent intent=new Intent(SettingsActivity.this, EditProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        break;
                    case 1:
                        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                            NotificationSettingFragment notificationSettingFragment=new NotificationSettingFragment();
                            transaction.addToBackStack("NotificationSettingFragment");
                            transaction.replace(R.id.container_frameLayout,notificationSettingFragment,"NotificationSettingFragment");
                            hideLayout();
                            transaction.commit();

                        } else{
                            transitionToSignIn();
                        }

                        break;
                    case 2:
                        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

                            ChangePasswordFragment fragment=new ChangePasswordFragment();
                            transaction.addToBackStack(getString(R.string.ChangePasswordFragment));
                            transaction.replace(R.id.container_frameLayout,fragment,getString(R.string.ChangePasswordFragment));
                            hideLayout();
                            transaction.commit();
                        } else {
                            transitionToSignIn();
                        }
                        break;

                    case 3:

                        Intent i = new Intent(Intent.ACTION_SENDTO);
                        i.setData(Uri.parse("mailto:")); // only email apps should handle this

                        String[] addresses={"support@thefidebox.com"};
                        i.putExtra(Intent.EXTRA_EMAIL, addresses);
                        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Feedback));

                        i.putExtra(Intent.EXTRA_TEXT,"OS version: "+System.getProperty("os.version")+
                               "\nAPI Level: " +Build.VERSION.SDK_INT +
                                "\nDevice: "+ android.os.Build.DEVICE+
                                "\nModel: "+android.os.Build.MODEL+
                                "\nProduct: "+android.os.Build.PRODUCT);

                        if (i.resolveActivity(getPackageManager()) != null) {
                            startActivity(i);
                        }
                        break;

                    case 4:
                        goToUrl("https://thefidebox.com/privacypolicy.html");
                        break;

                    case 5:
                        goToUrl("https://thefidebox.com/termsofservice.html");
                        break;

                    case 6:

                        AttributionFragment attributionFragment=new AttributionFragment();
                        transaction.addToBackStack("AttributionFragment");
                        transaction.replace(R.id.container_frameLayout,attributionFragment,"AttributionFragment");
                        hideLayout();
                        transaction.commit();
                        startActivity(new Intent(SettingsActivity.this, OssLicensesMenuActivity.class));
                        break;
                    case 7:

                        if(FirebaseAuth.getInstance().getCurrentUser()!=null){

                            new MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                                    .setMessage(getString(R.string. dialog_signout))
                                    .setPositiveButton(getString(R.string.Sign_out), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseAuth.getInstance().signOut();
                                            Intent intent=new Intent(SettingsActivity.this, HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.Cancel), null)
                                    .show();
                        }
                        break;

                }

            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void showLayout(){
        container.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);

    }

    public void hideLayout(){
        container.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(FCMService.COMMENT_NOTIFICATION_ID);
            notificationManager.cancel(FCMService.CV_NOTIFICATION_ID);
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
        }
    }

    private void transitionToSignIn(){
        Intent intent = new Intent(mContext, SignInActivity.class);
        finish();
        startActivity(intent);
        this.overridePendingTransition(0,0);
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(container.getVisibility()==View.INVISIBLE ||frameLayout.getVisibility()==View.VISIBLE){
            showLayout();
        }
    }
}
