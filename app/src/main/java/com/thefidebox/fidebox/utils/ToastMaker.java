package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thefidebox.fidebox.R;

public class ToastMaker {

    private static final String TAG = "ToastMaker";

    public static void showToastMaker(Context mContext, String text){

        Log.d(TAG, "showToastMaker: ");
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast, null);

        TextView textView =  layout.findViewById(R.id.text);
        textView.setText(text);

        Toast toast = new Toast(mContext.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }

    public static void parseErrorCode(Context context,String errorCode) {

        switch (errorCode) {
            case "ERROR_INVALID_EMAIL":

                showToastMaker(context,context.getString(R.string.alert_invalidemailctn));
                break;

            case "ERROR_WRONG_PASSWORD":
            case "ERROR_USER_MISMATCH":

                showToastMaker(context, context.getString(R.string.alert_wrongpwctn));
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
            case "ERROR_EMAIL_ALREADY_IN_USE":
            case "ERROR_CREDENTIAL_ALREADY_IN_USE":

                showToastMaker(context , context.getString(R.string.alert_emailinusectn));
                break;

            case "ERROR_WEAK_PASSWORD":

                showToastMaker(context, context.getString(R.string.alert_weakpasswordctn));
                break;

            case "ERROR_USER_NOT_FOUND":
            case "ERROR_INVALID_USER_TOKEN":
            case "ERROR_OPERATION_NOT_ALLOWED":

                showToastMaker(context, context.getString(R.string.alert_wrongemailctn));
                break;
        }
    }


}
