package com.thefidebox.fidebox.reg_n_login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.dialogs.MDDialog;
import com.thefidebox.fidebox.firebasecloudmessaging.SharedPrefManager;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.models.NotificationSettings;
import com.thefidebox.fidebox.models.Token;
import com.thefidebox.fidebox.models.UserPrivateInfo;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.utils.ShowSnackBar;
import com.thefidebox.fidebox.utils.ToastMaker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";

    private EditText Name, Password;
    private Button signin;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView forgotPassword;
    private TextView signup;
    private CoordinatorLayout coordinatorLayout;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_signin);
        setupUIViews();

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        SharedPreferences initing = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Name.setText(initing.getString("PREF_USERID", null));

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });


        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText editText = new EditText(SignInActivity.this);

                SharedPreferences initing = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                editText.setText(initing.getString("PREF_USERID", null));

                new MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setView(editText)
                        .setMessage(getString(R.string.enter_email_to_reset_password))
                        .setNegativeButton(mContext.getString(R.string.Cancel), null)
                        .setPositiveButton(mContext.getString(R.string.Reset), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
                                Pattern pattern = Pattern.compile(regex);

                                Matcher matcher = pattern.matcher(editText.getText().toString());

                                if (matcher.matches()) {

                                    FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    ShowSnackBar.show(coordinatorLayout, getString(R.string.reset_password_link));
                                                }
                                            });
                                } else {

                                    ShowSnackBar.show(coordinatorLayout, getString(R.string.please_provide_valid_email));
                                }

                            }
                        })
                        .show();

                //       startActivity(new Intent(SignInActivity.this, PasswordActivity.class));
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                coordinatorLayout.requestFocus();
                hideKeyboard(SignInActivity.this);

                String password = Password.getText().toString().trim();
                String email = Name.getText().toString().trim();

                validate(email, password);
            }
        });
    }

    private void setupUIViews() {
        Name = findViewById(R.id.etName);
        Password = findViewById(R.id.etPassword);
        signin = findViewById(R.id.btnsignin);
        signup = findViewById(R.id.tvsignup);
        forgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progress_bar_main);
        progressBar.setVisibility(View.GONE);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
    }


    private void validate(String userName, String userPassword) {
        progressBar.setVisibility(View.VISIBLE);

        if (userName == null || userName.trim().length() == 0 || userPassword == null || userPassword.trim().length() == 0) {

            new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                    .setMessage(getString(R.string.alert_emptyctn))
                    .setTitle(R.string.alert_empty)
                    .setPositiveButton(getString(R.string.OK), null)
                    .show();

            progressBar.setVisibility(View.GONE);
        } else {
            signinfirebase(userName, userPassword);
        }
    }

    private void signinfirebase(final String userName, String userPassword) {


        Log.d(TAG, "signinfirebase: ");
        FirebaseAuth.getInstance().signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    //no need to check email verification
                    //        checkEmailVerification(userName);

                    SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    setting.edit()
                            .putString("PREF_USERID", userName)
                            .apply();


                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {

                                    Log.d(TAG, "onComplete: 2345");
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "getInstanceId failed", task.getException());
                                        progressBar.setVisibility(View.GONE);

                                        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                        return;
                                    }
                                    // Get new Instance ID token
                                  final String tokenId = task.getResult().getToken();
                                    Token token = new Token(tokenId);
                                    SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getApplicationContext());

                                    Log.d(TAG, "onComplete: shared"+sharedPrefManager.getToken());
                                    Log.d(TAG, "onComplete: instance"+token.getTokenId());

                                    try {
                                        if (!token.getTokenId().equals(sharedPrefManager.getToken())) {

                                            FirebaseFirestore.getInstance().collection(getString(R.string.col_users))
                                                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .collection(getString(R.string.col_token)).document(tokenId).set(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Log.d(TAG, "onComplete: 789");

                                                    SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(getApplicationContext());
                                                    sharedPrefManager.storeToken(tokenId);

                                                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            });
                                        } else {
                                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }

                                    } catch (NullPointerException e){
                                        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }
                            });


                } else {
                    progressBar.setVisibility(View.GONE);

                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                    MDDialog mdDialog = new MDDialog(mContext);
                    mdDialog.parseErrorCode(errorCode);

                }
            }
        });
    }



    public void checkIfFirstTime(final String name, final String email, final String photoUrl) {

        FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();

        if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {


        } else {
            // This is an existing user, show them a welcome back screen.
        }

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
