package com.thefidebox.fidebox.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.resource.file.FileResource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.core.FirestoreClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.Document;
import com.google.firestore.v1.Write;
import com.thefidebox.fidebox.giggle_comment_util.SmileyEvil;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.models.DraftCV;
import com.thefidebox.fidebox.models.Giggle;
import com.thefidebox.fidebox.models.UserPrivateInfo;
import com.thefidebox.fidebox.models.Username;
import com.thefidebox.fidebox.models.Users;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.profile.EditProfileActivity;
import com.thefidebox.fidebox.profile.ProfileActivity;
import com.thefidebox.fidebox.write_n_draft.WriteActivity;
import com.thefidebox.fidebox.write_n_draft.Write_n_draftActivity;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase
    private FirebaseFirestore mFirebaseStore;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    //vars
    Context mContext;
    private float mPhotoUploadProgress = 0;
    private @ServerTimestamp
    Timestamp timestamp;


//    public Activity activity;


    public FirebaseMethods(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();

        mFirebaseStore = FirebaseFirestore.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if (mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     * // Update 'users' node for the current user
     */
    public void updateUserAccountSettings(final String Bio, final String display_name, final String profilePictureUrl
            , final String email, Users userDataBeforeChange,final ProgressBar progressBarMain) {


        //check if the name already exists first
        String userName = display_name.toLowerCase();

        WriteBatch batch=FirebaseFirestore.getInstance().batch();

        DocumentReference userReference=FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        Map<String,Object> usersFields=new HashMap<>();
        usersFields.put("userId",FirebaseAuth.getInstance().getUid());
        usersFields.put("display_name",display_name);
        usersFields.put("username",userName);
        usersFields.put("bio",Bio);
        usersFields.put("profile_photo",profilePictureUrl);

        batch.update(userReference,usersFields);


            UserPrivateInfo userPrivateInfo =new UserPrivateInfo(FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    FirebaseAuth.getInstance().getCurrentUser().getUid());

            DocumentReference userPrivateReference=FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("private")
                    .document("private");

            batch.set(userPrivateReference,userPrivateInfo);

        if(!userDataBeforeChange.getUsername().equals(userName)){
            Username username=new Username();
            username.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
            username.setUsername(userName);

            //delete old username
            DocumentReference deleteOldUserNameReference=FirebaseFirestore.getInstance().collection("username")
                    .document(userDataBeforeChange.getUsername());
            batch.delete(deleteOldUserNameReference);

            //insert new username
            DocumentReference usernameReference=FirebaseFirestore.getInstance().collection("username")
                    .document(userName);
            batch.set(usernameReference,username);
        }

        batch.commit().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressBarMain.setVisibility(View.GONE);
                ((EditProfileActivity)mContext).setSnackBar(mContext.getString(R.string.Error));

                ((AppCompatActivity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Log.d(TAG, "onFailure: "+e.getMessage());

            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressBarMain.setVisibility(View.GONE);
                intentToProfileActivity();
            }
        });

    }


    private String getTimeStamp() {

        //add a "-" to make it negative for query
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Hongkong"));
        return sdf.format(new Date());
    }


    public void addCvToDatabase(final Uri trollPicture, final String name, final String position, final String category, final String date1,
                                final String failure1, final String date2, final String failure2, final String date3, final String failure3,
                                final String draftCVId, final ProgressBar progressBar, final ProgressBar progressBarMain, final TextView percent) {

        if (trollPicture != null) {
            FilePaths filePaths = new FilePaths();
            //new photo

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DocumentReference letterRef = FirebaseFirestore.getInstance().collection("category").document();


            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id  + "/" +(letterRef.getId()));

            UploadTask uploadTask = null;
            uploadTask = storageReference.putFile(trollPicture);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            uploadOtherIntoToDatabase(uri, name, position, category, date1,
                                    failure1, date2, failure2, date3, failure3, draftCVId, progressBarMain);

                            //delete draft
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    progressBar.setVisibility(View.GONE);
                    ToastMaker.showToastMaker(mContext, mContext.getString(R.string.send_failed)+e.getMessage());
                    AppCompatActivity activity = (AppCompatActivity) mContext;
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    float progress = ((float) (10000 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());

                    progressBar.setVisibility(View.VISIBLE);

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

//                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

        } else {
            uploadOtherIntoToDatabase(trollPicture, name, position, category, date1,
                    failure1, date2, failure2, date3, failure3, draftCVId, progressBarMain);
            //delete draft CV
        }
    }

    public void uploadOtherIntoToDatabase(Uri troll_picture, String name, String position, String category, String date1,
                                          String failure1, String date2, String failure2, String date3, String failure3, final String draftCVId,
                                          final ProgressBar progressBarMain) {

        Log.d(TAG, "uploadOtherIntoToDatabase: 1" + mContext);

        if (troll_picture == null) {
            progressBarMain.setVisibility(View.VISIBLE);
        }

        WriteBatch db = FirebaseFirestore.getInstance().batch();
        DocumentReference cvRef = FirebaseFirestore.getInstance().collection(category).document();

        CV CV = new CV();
        CV.setName(name);
        CV.setPosition(position);
        CV.setDate1(date1);
        CV.setFailure1(failure1);
        CV.setDate2(date2);
        CV.setFailure2(failure2);
        CV.setDate3(date3);
        CV.setFailure3(failure3);
        CV.setTimestamp(timestamp);
        CV.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        CV.setCvId(cvRef.getId());
        CV.setCategory(category);
        CV.setGiggles(0);
        CV.setComments(0);

        try {
            CV.setTroll_picture(troll_picture.toString());
        } catch (NullPointerException e) {

        }

        db.set(cvRef, CV);

        //insert into user
        DocumentReference userCVRef = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_cvs))
                .document(CV.getCvId());

        db.set(userCVRef, CV);

        //delete draft CV
        if (draftCVId != null) {

            DocumentReference deleteReference = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection(mContext.getString(R.string.col_draft_cvs))
                    .document(draftCVId);

            db.delete(deleteReference);

            //no need to remove from list in write and draft as will start it as a new activity
        }


        db.commit()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        AppCompatActivity activity = (AppCompatActivity) mContext;
                        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Log.d(TAG, "onFailure:uploadOtherInfo "+e.getMessage());
                        ToastMaker.showToastMaker(mContext, mContext.getString(R.string.send_failed));
                        progressBarMain.setVisibility(View.GONE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                AppCompatActivity activity = (AppCompatActivity) mContext;
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Log.d(TAG, "onSuccess: ");
                Intent intent = new Intent(mContext, Write_n_draftActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("sendResult", "success");
                progressBarMain.setVisibility(View.GONE);
                mContext.startActivity(intent);

            }
        });
    }


    public void updateDraftCVDatabase(String name, String position, String category, String date1,
                                      String failure1, String date2, String failure2, String date3, String failure3,
                                      String draftCVId, final int pos /* position of RV*/) {


        final DraftCV draftCV = new DraftCV();

        draftCV.setName(name);
        draftCV.setPosition(position);
        draftCV.setDate1(date1);
        draftCV.setFailure1(failure1);
        draftCV.setDate2(date2);
        draftCV.setFailure2(failure2);
        draftCV.setDate3(date3);
        draftCV.setFailure3(failure3);
        draftCV.setTimestamp(timestamp);
        draftCV.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        draftCV.setCvId(draftCVId);
        draftCV.setCategory(category);

        final Intent intent = new Intent();
        //update database
        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_draft_cvs))
                .document(draftCVId)
                .set(draftCV)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        intent.putExtra("draft", draftCV);
                        intent.putExtra("pos", pos);
                        intent.putExtra("action", "update");

                        ((WriteActivity) mContext).setResult(((WriteActivity) mContext).RESULT_OK, intent);
                        ((WriteActivity) mContext).finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        intent.putExtra("draftResult", "fail");
                        ((WriteActivity) mContext).setResult(((WriteActivity) mContext).RESULT_OK, intent);
                        ((WriteActivity) mContext).finish();
                    }
                });
    }


    public void addDraftCVToDatabase(String name, final String position, String category, String date1,
                                     String failure1, String date2, String failure2, String date3, String failure3
    ) {
//        WriteBatch db = FirebaseFirestore.getInstance().batch();

        final DraftCV draftCV = new DraftCV();


        //insert into database
        DocumentReference draftCVRef = FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_draft_cvs))
                .document();

        draftCV.setName(name);
        draftCV.setPosition(position);
        draftCV.setDate1(date1);
        draftCV.setFailure1(failure1);
        draftCV.setDate2(date2);
        draftCV.setFailure2(failure2);
        draftCV.setDate3(date3);
        draftCV.setFailure3(failure3);
        draftCV.setTimestamp(timestamp);
        draftCV.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        draftCV.setCvId(draftCVRef.getId());
        draftCV.setCategory(category);

        final Intent intent = new Intent();


        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection(mContext.getString(R.string.col_draft_cvs))
                .document(draftCVRef.getId())
                .set(draftCV)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        intent.putExtra("draft", draftCV);
                        intent.putExtra("action", "add");
                        ((WriteActivity) mContext).setResult(((WriteActivity) mContext).RESULT_OK, intent);
                        ((WriteActivity) mContext).finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        intent.putExtra("draftResult", "fail");
                        ((WriteActivity) mContext).setResult(((WriteActivity) mContext).RESULT_OK, intent);
                        ((WriteActivity) mContext).finish();
                    }
                });
    }

    public void intentToProfileActivity() {
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    public void addNewGiggle(final String letter_id, final String category, String posterID, final SmileyEvil smileyEvil) {

        Giggle support = new Giggle();
        support.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        WriteBatch batch = db.batch();

        DocumentReference supportColRef = db.collection(category).document(letter_id)
                .collection(mContext.getString(R.string.field_giggles)).document(currentUserId);

        batch.set(supportColRef, support);

         DocumentReference userSupportRef = db.collection(mContext.getString(R.string.col_users)).document(posterID)
                .collection(mContext.getString(R.string.col_cvs)).document(letter_id);

        DocumentReference userSupportColRef = userSupportRef.collection(mContext.getString(R.string.col_giggles)).document(currentUserId);
        batch.set(userSupportColRef, support);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                smileyEvil.setGiggleUploading(false);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastMaker.showToastMaker(mContext,e.getMessage());
            }
        });

    }


    public void removeGiggle(final String letter_id, final String category, final String posterID,
                             final String currentUserId, final SmileyEvil smileyEvil) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        WriteBatch batch = db.batch();

        DocumentReference supportColRef = db.collection(category).document(letter_id)
                .collection(mContext.getString(R.string.field_giggles)).document(currentUserId);
        batch.delete(supportColRef);

        DocumentReference userSupportColRef = db.collection(mContext.getString(R.string.col_users)).document(posterID)
                .collection(mContext.getString(R.string.col_cvs)).document(letter_id)
                .collection(mContext.getString(R.string.field_giggles)).document(currentUserId);

        batch.delete(userSupportColRef);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                smileyEvil.setGiggleUploading(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastMaker.showToastMaker(mContext,e.getMessage());
            }
        });

    }

}
