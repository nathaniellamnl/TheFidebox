package com.thefidebox.fidebox.view_models;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.Observable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.models.Token;
import com.thefidebox.fidebox.models.UserPrivateInfo;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.ToastMaker;

import java.util.HashMap;
import java.util.Map;


public class SignUpUserViewModel extends AndroidViewModel implements Observable {

    private static final String TAG = "UserAccountViewModel";

    public MutableLiveData<String> name = new MutableLiveData<>();
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> reaffirmedPassword = new MutableLiveData<>();
    public Map<String, MutableLiveData<String>> infoMaps = new HashMap<>();
    public MutableLiveData<Boolean> emptyName = new MutableLiveData<>();
    public MutableLiveData<Boolean> emptyEmail = new MutableLiveData<>();
    public MutableLiveData<Boolean> emptyPassword = new MutableLiveData<>();
    public MutableLiveData<Boolean> emptyReaffirmedPassword = new MutableLiveData<>();
    public MutableLiveData<Boolean> upLoading = new MutableLiveData<>();

    public SignUpUserViewModel(@NonNull Application application) {
        super(application);
        infoMaps.put("name", name);
        infoMaps.put("email", email);
        infoMaps.put("password", password);
        infoMaps.put("reaffirmedPassword", reaffirmedPassword);
        upLoading.setValue(false);
    }

    @Bindable
    public MutableLiveData<String> getName() {

        try {
            if (this.name.getValue().contains(" ")) {
                this.name.setValue(this.name.getValue().replaceAll(" ", "_"));

            }
        } catch (NullPointerException e) {

        }

        return this.name;
    }

    @Bindable
    public MutableLiveData<String> getEmail() {

        return this.email;
    }

    @Bindable
    public MutableLiveData<String> getPassword() {

        return this.password;
    }

    @Bindable
    public MutableLiveData<String> getReaffirmedPassword() {

        return this.reaffirmedPassword;
    }


    @Bindable
    public MutableLiveData<Boolean> getUpLoading() {

        return upLoading;
    }

    @Override
    public void addOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

    }

    @Override
    public void removeOnPropertyChangedCallback(OnPropertyChangedCallback callback) {

    }


    public void clickSignUp() {


        for (Map.Entry<String, MutableLiveData<String>> entry : infoMaps.entrySet()) {

            Log.d(TAG, "clickSignUp:2 ");
            try {
                if (entry.getValue().getValue() == null || entry.getValue().getValue().equals("")) {
                    checkEmptyError(entry.getKey());
                } else {
                    cancelEmptyError(entry.getKey());

                    //no empty fields
                    if (entry.getKey().equals("reaffirmedPassword")) {

                        if (password.getValue() != null && reaffirmedPassword != null &&
                                !password.getValue().equals(reaffirmedPassword.getValue())) {

                            ToastMaker.showToastMaker(getApplication(), getApplication().getString(R.string.incongruent_password));

                        } else if (password.getValue()!=null && reaffirmedPassword != null &&
                                password.getValue().equals(reaffirmedPassword.getValue())){

                            upLoading.setValue(true);

                            //Check whether username eixsts in database
                            FirebaseFirestore.getInstance().collection("username")
                                    .document(name.getValue().toLowerCase())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    upLoading.setValue(false);
                                                    ToastMaker.showToastMaker(getApplication(),  getApplication().getString(R.string.usernameExists));

                                                } else {
                                                    createUserAuth();
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {

                                                upLoading.setValue(false);
                                                ToastMaker.showToastMaker(getApplication(),  getApplication().getString(R.string.Error));
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });

                        } else {

                            Log.d(TAG, "Error getting documents:222 ");
                            ToastMaker.showToastMaker(getApplication(),  getApplication().getString(R.string.Error));

                        }

                    }

                }

            } catch (NullPointerException e) {
                checkEmptyError(entry.getKey());

            }

        }

    }

    private void createUserAuth(){
        //no same username, proceed to create account
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getValue(), password.getValue()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    Log.d(TAG, "onComplete: Task Successful");
                    try {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w(TAG, "getInstanceId failed", task.getException());
                                                return;
                                            }

                                            // Get new Instance ID token
                                            String tokenString = task.getResult().getToken();
                                            Token token = new Token(tokenString);

                                            Users userAccountSettings = new Users(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                                    name.getValue(),
                                                    name.getValue().toLowerCase(),
                                                    null,
                                                    null);

                                            UserPrivateInfo userPrivateInfo = new UserPrivateInfo(email.getValue(),
                                                    FirebaseAuth.getInstance().getUid());

                                            sendVerificationEmail(userAccountSettings, token, userPrivateInfo);
                                            upLoading.setValue(false);

                                        }
                                    });

                        }
                    } catch (NullPointerException e) {
                        //user is null
                        Log.d(TAG, "onComplete: fjjgd"+ e.getMessage());
                        upLoading.setValue(false);
                    }

                } else {

                    upLoading.setValue(false);
                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                    ToastMaker.parseErrorCode(getApplication(),errorCode);
                }
            }
        });
    }

    public void clickSignIn() {

        Intent intent = new Intent(getApplication(), SignInActivity.class);
        getApplication().startActivity(intent);

    }


//    put this in home Activity
    private void sendVerificationEmail(final Users userAccountSettings, final Token token, final UserPrivateInfo userPrivateInfo) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {


            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                Intent intent = new Intent(getApplication(), HomeActivity.class);
                                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                intent.putExtra(getApplication().getString(R.string.users), userAccountSettings);
                                intent.putExtra(getApplication().getString(R.string.token), token);
                                intent.putExtra("userPrivateInfo", userPrivateInfo);
                                intent.putExtra(getApplication().getString(R.string.calling_activity), TAG);
                                getApplication().startActivity(intent);

                            } else {
//                                ((SignUpActivity) mContext).snackBar();
                            }
                        }
                    });
        }
    }


    public void checkEmptyError(String key) {

        switch (key) {
            case "name":
                emptyName.setValue(true);
                break;
            case "email":
                emptyEmail.setValue(true);
                break;
            case "password":
                emptyPassword.setValue(true);
                break;
            case "reaffirmedPassword":
                emptyReaffirmedPassword.setValue(true);
                break;
        }
    }

    public void cancelEmptyError(String key) {
        switch (key) {
            case "name":
                emptyName.setValue(false);
                break;
            case "email":
                emptyEmail.setValue(false);
                break;
            case "password":
                emptyPassword.setValue(false);
                break;
            case "reaffirmedPassword":
                emptyReaffirmedPassword.setValue(false);
                break;
        }
    }

    @BindingAdapter("emptyName")
    public static void showEmptyError0(TextInputLayout textInputLayout, Boolean emptyName) {

        try {
            Log.d(TAG, "showEmptyError0: " + emptyName.toString());
            if (emptyName) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("This field cannot be empty");
                textInputLayout.setErrorIconDrawable(R.drawable.ic_error_red);
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        } catch (NullPointerException e) {

        }
    }


    @BindingAdapter("emptyEmail")
    public static void showEmptyError1(TextInputLayout textInputLayout, Boolean emptyEmail) {
        try {
            if (emptyEmail) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("This field cannot be empty");
                textInputLayout.setErrorIconDrawable(R.drawable.ic_error_red);
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        } catch (NullPointerException e) {

        }
    }

    @BindingAdapter("emptyPassword")
    public static void showEmptyError2(TextInputLayout textInputLayout, Boolean emptyPassword) {
        try {
            if (emptyPassword) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("This field cannot be empty");
                textInputLayout.setErrorIconDrawable(R.drawable.ic_error_red);
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        } catch (NullPointerException e) {

        }
    }

    @BindingAdapter("emptyReaffirmedPassword")
    public static void showEmptyError3(TextInputLayout textInputLayout, Boolean emptyReaffirmedPassword) {
        try {
            if (emptyReaffirmedPassword) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError("This field cannot be empty");
                textInputLayout.setErrorIconDrawable(R.drawable.ic_error_red);
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        } catch (NullPointerException e) {

        }
    }


    @BindingAdapter("upLoading")
    public static void setBoolean(ProgressBar progressBar, boolean UpLoading) {

        if (UpLoading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

    }

}
