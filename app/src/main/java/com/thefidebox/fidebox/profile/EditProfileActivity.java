package com.thefidebox.fidebox.profile;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.dialogs.ConfirmPasswordDialogFragment;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.FilePaths;
import com.thefidebox.fidebox.utils.FirebaseMethods;
import com.thefidebox.fidebox.utils.ImageManager;
import com.thefidebox.fidebox.utils.ProgressBarAnimation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity implements ConfirmPasswordDialogFragment.OnPasswordConfirmListener {

    private static final String TAG = "EditProfile Activity";
    private static final int ACTIVITY_NUM = 5;
    private static final String DIALOG_DATE = "date";

    private TextView edit;
    private TextView save,percent;
    private ProgressBar progressBar,progressBarMain;
    private ImageView profilePicture, backArrow;
    private EditText bio, displayName, email;
    private Context mContext;
    private FirebaseMethods mFirebaseMethods;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //vars
    float mPhotoUploadProgress=0;
    private String emailString, bioString, displayNameString;
    private Users users;
    private Uri mCropImageUri;
    private RequestManager requestManager;
    private RequestOptions requestOptions;

    //shimmer
    private RelativeLayout relativeLayoutContainer;
    private FrameLayout frameLayoutContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        relativeLayoutContainer = findViewById(R.id.container_relLayout);
        frameLayoutContainer = findViewById(R.id.container_frameLayout);
        displayName = findViewById(R.id.etName);
        email = findViewById(R.id.etEmail);
        bio = findViewById(R.id.etBio);
        profilePicture = findViewById(R.id.ivProfilePicture);
        save=findViewById(R.id.tv_save);
        edit=findViewById(R.id.tvEdit);
        progressBar=findViewById(R.id.progress_bar);
        progressBarMain=findViewById(R.id.progress_bar_main);
        percent=findViewById(R.id.percent);
        backArrow=findViewById(R.id.iv_backArrow);

        requestManager=Glide.with(this);

        int dpValue = 115; // margin in dips
        float d = getResources().getDisplayMetrics().density;
        final int margin = (int) (dpValue * d); // margin in pixels

        requestOptions=RequestOptions.overrideOf(margin,margin)
                .circleCrop()
                .placeholder(getResources().getDrawable(R.drawable.ic_account_circle));

        initTextListener();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(EditProfileActivity.this);
                relativeLayoutContainer.requestFocus();

                progressBarMain.setVisibility(View.VISIBLE);

                emailString=email.getText().toString();
                bioString=bio.getText().toString();
                displayNameString=displayName.getText().toString();

                String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
                Pattern pattern = Pattern.compile(regex);

                Matcher matcher = pattern.matcher(emailString);

                if (!matcher.matches() ) {

                    setSnackBar(getString(R.string.emailInvalid));
                    progressBarMain.setVisibility(View.GONE);
                } else {
                    if (!FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(emailString)) {

                        showConfirmPasswordDialog();

                    } else {
                        updateOtherInfo();
                    }
                }
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutContainer.requestFocus();
                startCropActivity();
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayoutContainer.requestFocus();
                startCropActivity();
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mContext = EditProfileActivity.this;
        mFirebaseMethods = new FirebaseMethods(mContext);

        setupFirebaseAuth();

        try{
            //user may not have logged in
            setUpUser();
        } catch (NullPointerException e){
            Log.d(TAG, "onCreate: "+e.getMessage());
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mCropImageUri = result.getUri();

                requestManager
                        .load(mCropImageUri)
                        .apply(requestOptions)
                        .into(profilePicture);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Log.d(TAG, "onActivityResult: " + CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE);
                setSnackBar(getString(R.string.Error) + CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE);
            }
        }
    }

    public void intentToProfileActivity() {
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void setSnackBar(String string) {
        Snackbar snack = Snackbar.make(findViewById(R.id.relLayout3), string, Snackbar.LENGTH_SHORT);
        View view = snack.getView();
        TextView tv = view.findViewById(R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();
    }

    //Firebasemethods

    private void setUpUser() {

        email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        Log.d(TAG, "setUpUser:11 "+FirebaseAuth.getInstance().getCurrentUser().getEmail());
        FirebaseFirestore.getInstance().collection(getString(R.string.users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        users = document.toObject(Users.class);

                        displayName.setText(users.getDisplay_name());
                        bio.setText(users.getBio());

                        requestManager
                                .load(users.getProfile_photo())
                                .apply(requestOptions)
                                .into(profilePicture);

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });


    }


    public void hideLayout() {
        frameLayoutContainer.setVisibility(View.VISIBLE);
        relativeLayoutContainer.setVisibility(View.GONE);
    }

    public void showLayout() {
        frameLayoutContainer.setVisibility(View.GONE);
        relativeLayoutContainer.setVisibility(View.VISIBLE);
    }


    @Override
    public void onPasswordConfirm(String password) {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), password);

        // Prompt the user to re-provide their sign-in credentials

        ////////////////   Prompt the user to re-provide their sign-in credentials
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");
                            relativeLayoutContainer.requestFocus();

                            //////check to see if the email is not already present in the databse
                            mAuth.fetchSignInMethodsForEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {

                                        if (task.getResult().getSignInMethods().size() == 1) {
                                            Log.d(TAG, "onComplete: that email is already in use.");

                                            progressBarMain.setVisibility(View.GONE);
                                            setSnackBar(mContext.getString(R.string.emailInUse));
                                        } else {
                                            updateEmail();
                                            Log.d(TAG, "onComplete: That email is available.");
                                        }

                                    } else {
                                        Log.e(TAG, "onComplete: error " + task.getException().getMessage());
                                        progressBarMain.setVisibility(View.GONE);
                                        setSnackBar(mContext.getString(R.string.emailInvalid));
                                    }
                                }
                            });
                        } else {
                            Log.d(TAG, "onComplete: re-authentication failed.");
                            progressBarMain.setVisibility(View.GONE);
                            ((EditProfileActivity) mContext).setSnackBar(mContext.getString(R.string.wrong_password));
                        }
                    }
                });
    }


    private void updateEmail() {

        FirebaseAuth.getInstance().getCurrentUser().updateEmail(email.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");
                            updateOtherInfo();

                        } else {
                            setSnackBar(getString(R.string.emailInvalid));
                        }
                    }
                });

    }

    private void showConfirmPasswordDialog() {

        FragmentManager fm = getSupportFragmentManager();

        ConfirmPasswordDialogFragment dialog;
        if (fm.findFragmentByTag(getString(R.string.confirm_password_dialog)) != null) {
            dialog = (ConfirmPasswordDialogFragment) fm.findFragmentByTag(getString(R.string.confirm_password_dialog));
        } else {
            dialog = ConfirmPasswordDialogFragment.newInstance();
        }

        dialog.setCancelable(false);

        if (!dialog.isAdded()) {

            dialog.show(fm, getString(R.string.confirm_password_dialog));
        }
    }


    public void startCropActivity() {

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setAspectRatio(1, 1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setBorderLineThickness(0)
                    .start(this);

    }

    private void updateOtherInfo() {

        if(!displayName.getText().toString().equals(users.getDisplay_name())){
            //check first whether username exists
            FirebaseFirestore.getInstance().collection("username")
                    .document(displayName.getText().toString().toLowerCase())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                //username already exists
                                if (document.exists()) {
                                    setSnackBar(getString(R.string.usernameExists));
                                    progressBarMain.setVisibility(View.GONE);
                                } else {
                                    //then if photo changed first, then other info
                                    if(mCropImageUri==null){
                                        mFirebaseMethods.updateUserAccountSettings(bioString, displayNameString, users.getProfile_photo(), emailString,users,progressBarMain);
                                    } else{
                                        uploadPhotoThenOtherInfo();
                                    }
                                }
                            } else {
                                progressBarMain.setVisibility(View.GONE);
                                setSnackBar(getString(R.string.usernameExists));
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

        } else{
            //then if photo changed first, then other info
            if(mCropImageUri==null){

                mFirebaseMethods.updateUserAccountSettings(bioString, displayNameString, users.getProfile_photo(), emailString,users,progressBarMain);
            } else{
                uploadPhotoThenOtherInfo();
            }
        }

/*
        //check if photo changed first, then other info
        if(mCropImageUri==null){

            mFirebaseMethods.updateUserAccountSettings(bioString, displayNameString, users.getProfile_photo(), emailString,users);
        } else{
            uploadPhotoThenOtherInfo();
        }*/
    }

    private void uploadPhotoThenOtherInfo() {

        FilePaths filePaths = new FilePaths();


        Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");

        String user_id = FirebaseAuth.getInstance().getUid();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

        //convert image url to bitmap
        Bitmap bm = com.thefidebox.fidebox.utils.ImageManager.getBitmap(mCropImageUri);
        byte[] bytes = ImageManager.getBytesFromBitmap(bm, 85);

        UploadTask uploadTask = null;
        uploadTask = storageReference.putBytes(bytes);

        progressBarMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        percent.setVisibility(View.VISIBLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        mFirebaseMethods.updateUserAccountSettings(bio.getText().toString(), displayName.getText().toString(),
                                uri.toString(), email.getText().toString(),users,progressBarMain);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                percent.setVisibility(View.GONE);
                            }
                        },300);

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                percent.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                setSnackBar(getString(R.string.Error));

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {

                float progress =  ((float) (10000 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());


                if (progress - 200 > mPhotoUploadProgress) {

                    ProgressBarAnimation anim = new ProgressBarAnimation(progressBar, mPhotoUploadProgress, progress);
                    anim.setDuration(1000);
                    progressBar.startAnimation(anim);

                    String a=String.valueOf((int) progress/100);
                    final String percentage=a+"%";

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            percent.setVisibility(View.VISIBLE);
                            percent.setText(percentage);

                        }
                    },300);


                    mPhotoUploadProgress = progress;
                }
            }
        });
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");

        displayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                displayName.removeTextChangedListener(this);

                displayName.setText(displayName.getText().toString().replaceAll(" ","_"));
                displayName.setSelection(displayName.getText().toString().length());

                displayName.addTextChangedListener(this);


            }
        });
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

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is signed in
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(FCMService.COMMENT_NOTIFICATION_ID);
            notificationManager.cancel(FCMService.CV_NOTIFICATION_ID);
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
