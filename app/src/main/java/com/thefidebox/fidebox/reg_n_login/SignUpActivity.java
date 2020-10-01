package com.thefidebox.fidebox.reg_n_login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thefidebox.fidebox.databinding.ActivitySignupBinding;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.utils.ShowSnackBar;
import com.thefidebox.fidebox.view_models.SignUpUserViewModel;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";

    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Intent intent=new Intent(SignUpActivity.this,HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        setUpViewModel();
    }


    public void setUpViewModel(){

        ActivitySignupBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        binding.setLifecycleOwner(this);
        SignUpUserViewModel signUpUserViewModel= new ViewModelProvider(this).get(SignUpUserViewModel.class);
        binding.setSignUpUserViewModel(signUpUserViewModel);
    }

    public void snackBar(){
        ShowSnackBar.show((CoordinatorLayout)findViewById(R.id.coordinatorLayout),getString(R.string.send_email_failed));
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
    }


}
