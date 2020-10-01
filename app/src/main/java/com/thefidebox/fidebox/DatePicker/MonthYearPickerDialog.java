package com.thefidebox.fidebox.DatePicker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.fragment.app.DialogFragment;

import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.dialogs.DatePickerDialogFragment;

import java.util.Calendar;
import java.util.Date;

public class MonthYearPickerDialog extends DialogFragment {

    private static final String TAG = "MonthYearPickerDialog";
    private static final String EXTRA_DATE = "DialogDate";
    private static final int MAX_YEAR = 2099;
    private Context context;
    private static Date mDate;
    private static int number;


    public interface OnDateListener {
        void onDateConfirm(int year, int month,int number);
    }

    OnDateListener onDateListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        mDate = (Date) getArguments().getSerializable(EXTRA_DATE);
        number=getArguments().getInt("number");
        Calendar cal = Calendar.getInstance();

        View dialog = inflater.inflate(R.layout.date_picker_dialog, null);
        final NumberPicker monthPicker =  dialog.findViewById(R.id.picker_month);
        final NumberPicker yearPicker = dialog.findViewById(R.id.picker_year);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(cal.get(Calendar.MONTH));

        int year = cal.get(Calendar.YEAR);
        yearPicker.setMinValue(1960);
        yearPicker.setMaxValue(2050);
        yearPicker.setValue(year);

        builder.setTitle(context.getString(R.string.Date))
                .setView(dialog)
                // Add action buttons
                .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        onDateListener.onDateConfirm(yearPicker.getValue(), monthPicker.getValue(),number);

                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MonthYearPickerDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public static MonthYearPickerDialog newInstance(Date date,int number) {
        Bundle args = new Bundle();

        args.putSerializable(EXTRA_DATE, date);
        args.putInt("number",number);

        MonthYearPickerDialog fragment = new MonthYearPickerDialog();
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
        try{
            onDateListener = (OnDateListener)context;
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage() );
        }
    }


}