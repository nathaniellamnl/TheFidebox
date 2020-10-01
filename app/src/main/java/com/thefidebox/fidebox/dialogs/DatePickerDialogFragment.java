package com.thefidebox.fidebox.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.thefidebox.fidebox.R;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DatePickerDialogFragment extends DialogFragment  {

    private static final String TAG = "DialogFragment";
    private static final String EXTRA_DATE = "DialogDate";
    private static final String FRAGMENT = "Fragment";
    private static Date mDate;
    private static int number;


    public interface OnDateConfirmListener {
        void onDateConfirm(Date date,int number);
    }

    OnDateConfirmListener onDateConfirmListener;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        mDate = (Date) getArguments().getSerializable(EXTRA_DATE);
        number=getArguments().getInt("number");

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog);

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.getTimeInMillis();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
        constraintsBuilder.setEnd(c.getTimeInMillis());

        builder.setCalendarConstraints(constraintsBuilder.build());

        MaterialDatePicker<Long> picker = builder.build();

        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {

            @Override
            public void onPositiveButtonClick(Long selection) {

                Calendar b=Calendar.getInstance();
                b.setTimeInMillis(selection);

                mDate = b.getTime();
                Log.d(TAG, "onDateSet: "+mDate);

                onDateConfirmListener.onDateConfirm(mDate,number);
                getArguments().putSerializable(EXTRA_DATE, mDate);
            }
        });


        DatePickerDialog dialog = new DatePickerDialog(getContext());

        return dialog;

    }

    public static DatePickerDialogFragment newInstance(Date date,int number) {
        Bundle args = new Bundle();

        args.putSerializable(EXTRA_DATE, date);
        args.putInt("number",number);

        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            onDateConfirmListener = (OnDateConfirmListener) getTargetFragment();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }

}
