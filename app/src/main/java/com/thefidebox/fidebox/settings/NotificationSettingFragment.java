package com.thefidebox.fidebox.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.NotificationSettings;

public class NotificationSettingFragment extends Fragment {

    public NotificationSettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static final String TAG = "NotionSettingFragment";

    private ToggleButton toggleButton1, toggleButton2;
    private RelativeLayout relativeLayout1, relativeLayout2;
    private ProgressBar progressBarMain;
    private ImageView backArrow;

    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification_setting, container, false);
        toggleButton1 = view.findViewById(R.id.toggle_button_1);
        toggleButton2 = view.findViewById(R.id.toggle_button_2);
        relativeLayout1 = view.findViewById(R.id.relLayout_1);
        relativeLayout2 = view.findViewById(R.id.relLayout_2);
        progressBarMain = view.findViewById(R.id.progress_bar_main);
        backArrow=view.findViewById(R.id.iv_backArrow);
        mContext=getContext();

        relativeLayout1.setVisibility(View.INVISIBLE);
        relativeLayout2.setVisibility(View.INVISIBLE);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SettingsActivity)mContext).showLayout();
            }
        });

        setUpButtons();
        return view;
    }

    public void setUpButtons() {

        FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(getString(R.string.col_settings))
                .document(getString(R.string.notificationSettings))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                NotificationSettings notificationSettings = document.toObject(NotificationSettings.class);
                                Log.d(TAG, "onComplete: noti" + notificationSettings.isChange_on_cv());
                                Log.d(TAG, "onComplete: noti2" + notificationSettings.isChange_on_comment());

                                toggleButton1.setChecked(notificationSettings.isChange_on_cv());
                                toggleButton2.setChecked(notificationSettings.isChange_on_comment());

                                relativeLayout1.setVisibility(View.VISIBLE);
                                relativeLayout2.setVisibility(View.VISIBLE);
                                progressBarMain.setVisibility(View.GONE);

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void onPause() {
        super.onPause();

        //make sure the original setting is loaded first
        if (relativeLayout1.getVisibility() == View.VISIBLE && relativeLayout2.getVisibility() == View.VISIBLE) {

            NotificationSettings notificationSettings = new NotificationSettings();

            if (toggleButton1.isChecked()) {
                notificationSettings.setChange_on_cv(true);
            } else {
                notificationSettings.setChange_on_cv(false);
            }

            if (toggleButton2.isChecked()) {
                notificationSettings.setChange_on_comment(true);
            } else {
                notificationSettings.setChange_on_comment(false);
            }

            FirebaseFirestore.getInstance().collection(getContext().getString(R.string.users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(getContext().getString(R.string.col_settings))
                    .document(getContext().getString(R.string.doc_notificationSettings))
                    .set(notificationSettings)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "onComplete: ");
                        }
                    });

        }
    }
}
