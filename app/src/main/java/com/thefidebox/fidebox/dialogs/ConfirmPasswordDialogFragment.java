package com.thefidebox.fidebox.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.thefidebox.fidebox.R;

public class ConfirmPasswordDialogFragment extends DialogFragment {

    public static final String EXTRA_PASSWORD="DialogPassword";
    private static final String MODEL="Model";

    private String mPassword;
    TextInputEditText etPassword;


    public interface OnPasswordConfirmListener{
        void onPasswordConfirm(String password);
    }
    OnPasswordConfirmListener onPasswordConfirmListener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

      View v= getActivity().getLayoutInflater().inflate(R.layout.dialog_confirm_password,null);

        etPassword = v.findViewById(R.id.et_password);


        return new MaterialAlertDialogBuilder(getActivity(), R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                              .setView(v)
                              .setTitle(getContext().getString(R.string.confirmpw))
                              .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {

                                      String password = etPassword.getText().toString();
                                      if (password!=null && !password.equals("")) {

                                          mPassword=etPassword.getText().toString();
                                          onPasswordConfirmListener.onPasswordConfirm(mPassword);
                                          getDialog().dismiss();

                                      } else {
                                          etPassword.setError(getString(R.string.password_request));
                                      }
                                  }
                              })
                               .setNegativeButton(android.R.string.cancel,null)
                              .create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            onPasswordConfirmListener = (OnPasswordConfirmListener) getActivity();
        }catch (ClassCastException e){
        }
    }

    public static ConfirmPasswordDialogFragment newInstance() {
        ConfirmPasswordDialogFragment fragment = new ConfirmPasswordDialogFragment();

        return fragment;
    }

}
