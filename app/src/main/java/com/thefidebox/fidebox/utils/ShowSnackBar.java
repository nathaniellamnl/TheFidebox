package com.thefidebox.fidebox.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.thefidebox.fidebox.R;

public class ShowSnackBar  {

    public static void show(CoordinatorLayout coordinatorLayout, String string) {
        Snackbar snack = Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = view.findViewById(R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snack.show();
    }

}
