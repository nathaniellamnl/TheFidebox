package com.thefidebox.fidebox.settings;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.utils.ShowSnackBar;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChangePasswordFragment extends Fragment {

    private static final String TAG="ChangePasswordFragment";

    String oldPassword,newPassword,reaffirmPassword;
    EditText etNewPassword,etOldPassword,etReaffirmPassword;
    TextView tvConfirm,forgotPassword;
    TextInputLayout textInputLayout1,textInputLayout2,textInputLayout3;
    CoordinatorLayout coordinatorLayout;
    ImageView backArrow;
    Context mContext;
    ProgressBar progressBarMain;


    public ChangePasswordFragment() {
            super();
            setArguments(new Bundle());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_change_password,container,false);
        etNewPassword=view.findViewById(R.id.et_new_password);
        etOldPassword=view.findViewById(R.id.et_old_password);
        etReaffirmPassword=view.findViewById(R.id.etReaffirmPassword);
        tvConfirm=view.findViewById(R.id.tv_confirm);
        coordinatorLayout=view.findViewById(R.id.coordinatorLayout);
        forgotPassword=view.findViewById(R.id.tv_forgot_password);
        textInputLayout1=view.findViewById(R.id.text_input_layout_1);
        textInputLayout2=view.findViewById(R.id.text_input_layout_2);
        textInputLayout3=view.findViewById(R.id.text_input_layout_3);
        backArrow=view.findViewById(R.id.iv_backArrow);
        progressBarMain=view.findViewById(R.id.progress_bar_main);
        mContext=getContext();

        progressBarMain.setVisibility(View.GONE);

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBarMain.setVisibility(View.VISIBLE);
                hideKeyboard(getActivity());
                coordinatorLayout.requestFocus();

                 newPassword=etNewPassword.getText().toString();
                 reaffirmPassword=etReaffirmPassword.getText().toString();
                 oldPassword=etOldPassword.getText().toString();

                 coordinatorLayout.requestFocus();

                if(TextUtils.isEmpty(etOldPassword.getText().toString())){

                    progressBarMain.setVisibility(View.GONE);
                    textInputLayout1.setErrorEnabled(true);
                    textInputLayout1.setError(getContext().getString(R.string.empty_password));
                } else{

                    textInputLayout1.setErrorEnabled(false);
                }

                if(TextUtils.isEmpty(etNewPassword.getText().toString())){

                    progressBarMain.setVisibility(View.GONE);
                    textInputLayout2.setErrorEnabled(true);
                    textInputLayout2.setError(getContext().getString(R.string.empty_password));

                } else{
                    textInputLayout2.setErrorEnabled(false);
                }

                if(TextUtils.isEmpty(etReaffirmPassword.getText().toString())){

                    progressBarMain.setVisibility(View.GONE);
                    textInputLayout3.setErrorEnabled(true);
                    textInputLayout3.setError(getContext().getString(R.string.empty_password));

                } else{
                    textInputLayout3.setErrorEnabled(false);
                }


                if(!TextUtils.isEmpty(etNewPassword.getText().toString()) &&
                        !TextUtils.isEmpty(etOldPassword.getText().toString()) &&
                        !TextUtils.isEmpty(etReaffirmPassword.getText().toString()) ){

                    if (reaffirmPassword.equals(newPassword)){
                        validatePassword();
                    } else {


                        progressBarMain.setVisibility(View.GONE);
                        textInputLayout3.setErrorEnabled(true);
                        textInputLayout3.setError(getContext().getString(R.string.incongruent_password));
                    }

                }
            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                ShowSnackBar.show(coordinatorLayout,getActivity().getString(R.string.reset_password_link));
                            }
                        });
            }
        });


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SettingsActivity)mContext).showLayout();
            }
        });

        return view;
    }

    private void validatePassword(){

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(), oldPassword);

        ////////////////   Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");

                            FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User password updated.");

                                                progressBarMain.setVisibility(View.GONE);
                                                setSnackBar(getContext().getString(R.string.change_pw_success));

                                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

//                                                mAuth.setLanguageCode("fr");
                                                FirebaseAuth.getInstance().sendPasswordResetEmail(mAuth.getCurrentUser().getEmail())
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    getActivity().getSupportFragmentManager().popBackStack();
                                                                    ((SettingsActivity)getContext()).showLayout();

                                                                }
                                                            }
                                                        });

                                            } else{
                                                setSnackBar(getContext().getString(R.string.Error));

                                            }
                                        }
                                    });
                        } else {

                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                            Log.d(TAG, "onComplete: re-authentication failed.");
                            setSnackBar(mContext.getString(R.string.wrong_password));

                            progressBarMain.setVisibility(View.GONE);

                        }
                    }
                });

    }

    public void setSnackBar(String string) {
        Snackbar snack = Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = view.findViewById(R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
