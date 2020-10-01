package com.thefidebox.fidebox.dialogs;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.home.HomeActivity;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.FirebaseMethods;


public class ConfirmSignoutDialog extends DialogFragment {
   private static final String TAG = "ConfirmPasswordDialog";

   //vars
   FirebaseMethods mfirebasemethods;
   Context mContext;

   public ConfirmSignoutDialog(){

   }

   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
       View view =inflater.inflate(R.layout.dialog_confirm_signout,container,false);
       Log.d(TAG,"onCreateView:started.");

       mContext=getActivity();
       mfirebasemethods=new FirebaseMethods(getContext());

       TextView confirmDialog = (TextView)view.findViewById(R.id.dialogConfirm);
       confirmDialog.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               getDialog().dismiss();


               final FirebaseAuth.AuthStateListener listener=new FirebaseAuth.AuthStateListener() {
                   @Override
                   public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                       Log.d(TAG, "onAuthStateChanged: trial");
                       if(firebaseAuth.getCurrentUser()==null){
                           getActivity().finish();
                           firebaseAuth.removeAuthStateListener(this);
                           Log.d(TAG, "onAuthStateChanged: Signout success");
                       }
                   }
               };

               Log.d(TAG, "onClick: Signout");
               FirebaseAuth.getInstance().addAuthStateListener(listener);
               FirebaseAuth.getInstance().signOut();
           }
       });

       TextView cancelDialog = (TextView)view.findViewById(R.id.dialogCancel);
       cancelDialog.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Log.d(TAG,"onClick: closing the dialog");
               getDialog().dismiss();
           }
       });
       return view;
   }


}

