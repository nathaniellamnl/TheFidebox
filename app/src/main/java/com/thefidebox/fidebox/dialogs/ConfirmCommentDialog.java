package com.thefidebox.fidebox.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thefidebox.fidebox.R;


public class ConfirmCommentDialog extends DialogFragment {
   private static final String TAG = "ConfirmPasswordDialog";



    public interface OnConfirmCommentListener{
        void onConfirmComment(Boolean confirm);
   }

   OnConfirmCommentListener mOnConfirmCommentListener;

   private ConfirmCommentDialog(){

   }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        return new MaterialAlertDialogBuilder(getActivity(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setMessage(getActivity().getString(R.string.dialog_confirm_comment))
                .setPositiveButton(getString(R.string.Discard), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOnConfirmCommentListener.onConfirmComment(true);
                    }
                })
                .setNegativeButton(getString(R.string.Cancel),null)
                .create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            mOnConfirmCommentListener = (OnConfirmCommentListener) getTargetFragment();
        }catch (ClassCastException e){
        }
    }

    public static ConfirmCommentDialog newInstance() {

        ConfirmCommentDialog fragment = new ConfirmCommentDialog();

        return fragment;
    }

}

