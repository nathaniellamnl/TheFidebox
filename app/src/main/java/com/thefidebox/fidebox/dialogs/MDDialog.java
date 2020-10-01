package com.thefidebox.fidebox.dialogs;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thefidebox.fidebox.R;

public class MDDialog {
    private Context mContext;

    public MDDialog(Context mContext) {
        this.mContext = mContext;
    }

    public void parseErrorCode(String errorCode) {

        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":

                showAlertDialog(mContext.getString(R.string.alert_invalidemail), mContext.getString(R.string.alert_invalidemailctn));
                break;

            case "ERROR_WRONG_PASSWORD":
            case "ERROR_USER_MISMATCH":

                showAlertDialog(mContext.getString(R.string.alert_wrongpw), mContext.getString(R.string.alert_wrongpwctn));
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
            case "ERROR_EMAIL_ALREADY_IN_USE":
            case "ERROR_CREDENTIAL_ALREADY_IN_USE":

                showAlertDialog(mContext.getString(R.string.alert_emailinuse), mContext.getString(R.string.alert_emailinusectn));
                break;

            case "ERROR_WEAK_PASSWORD":

                showAlertDialog(mContext.getString(R.string.alert_weakpassword), mContext.getString(R.string.alert_weakpasswordctn));
                break;

            case "ERROR_USER_NOT_FOUND":
            case "ERROR_INVALID_USER_TOKEN":
            case "ERROR_OPERATION_NOT_ALLOWED":

                showAlertDialog(mContext.getString(R.string.alert_wrongemail), mContext.getString(R.string.alert_wrongemailctn));
                break;
        }
    }

    public void showAlertDialog(String title, String content) {
        new MaterialAlertDialogBuilder(mContext, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(mContext.getString(R.string.OK), null)
                .show();
    }
}
