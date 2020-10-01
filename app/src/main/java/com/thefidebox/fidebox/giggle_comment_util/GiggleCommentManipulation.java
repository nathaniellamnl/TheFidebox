package com.thefidebox.fidebox.giggle_comment_util;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.CV;
import com.thefidebox.fidebox.utils.FirebaseMethods;

import java.util.ArrayList;
import java.util.List;

public class GiggleCommentManipulation {
    private static final String TAG = "GiggleCommentManipulati";
    private static GiggleCommentManipulation giggleCommentManipulation;
    public static int giggleNumber;
    private static int commentNumber;

    private GiggleCommentManipulation(){

    }

    public static GiggleCommentManipulation getInstance(){

        if(giggleCommentManipulation==null){
            return new GiggleCommentManipulation();
        }
        return giggleCommentManipulation;
    }

    public static CV upGiggleNumber(TextView giggleNumber, final CV cv){

        cv.setGiggles(cv.getGiggles()+1);
        giggleNumber.setText(String.valueOf(Integer.parseInt(giggleNumber.getText().toString()) + 1));

        return cv;
    }

    public static CV downGiggleNumber(TextView giggleNumber, final CV cv){

        cv.setGiggles(cv.getGiggles()-1);

        giggleNumber.setText(String.valueOf(Integer.parseInt(giggleNumber.getText().toString()) - 1));

        return cv;
    }


    public static void OnClickAction(final String cvId, final String category, final String posterId, final Context mContext,final SmileyEvil smileyEvil) {

        final String mCurrentUser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final FirebaseMethods mFirebaseMethods = new FirebaseMethods(mContext);

        FirebaseFirestore.getInstance().collection(category)
                .document(cvId)
                .collection(mContext.getString(R.string.field_giggles))
                .whereEqualTo(mContext.getString(R.string.field_userId), mCurrentUser_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + (task.getResult().size()));
                            if (task.getResult().size() == 0) {

                                //case 1: not supported by user
                                mFirebaseMethods.addNewGiggle(cvId, category, posterId,smileyEvil);
                                //   getCommentNumber(TVsupportNo);

                            } else if (task.getResult().size() > 0) {

                                mFirebaseMethods.removeGiggle(cvId, category, posterId, mCurrentUser_id,smileyEvil);
                            }
                        } else {
                            Log.d(TAG, "onCickAction: fail");
                        }
                    }
                });

    }

}
