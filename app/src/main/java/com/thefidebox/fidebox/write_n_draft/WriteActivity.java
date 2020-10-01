package com.thefidebox.fidebox.write_n_draft;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.thefidebox.fidebox.DatePicker.MonthYearPickerDialog;
import com.thefidebox.fidebox.firebasecloudmessaging.FCMService;
import com.thefidebox.fidebox.models.DraftCV;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.reg_n_login.SignInActivity;
import com.thefidebox.fidebox.utils.FirebaseMethods;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class WriteActivity extends AppCompatActivity implements MonthYearPickerDialog.OnDateListener{

    @Override
    public void onDateConfirm(int year, int month, int number) {
        String failureDate = month + "/" + year ;

        if (number == 1) {
            date1.setText(failureDate);
        } else if (number == 2) {
            date2.setText(failureDate);
        } else if (number == 3) {
            date3.setText(failureDate);
        }
    }


    final static private String TAG = "WriteActivity";
    private int CURRENT_DRAFT_POSITION;
    public static final int PICK_IMAGE = 1;

    //variable
    String mCategory, mName, mPosition, mDate1, mDate2, mDate3, mFailure1, mFailure2, mFailure3;
    DraftCV mDraftCV;
    FirebaseMethods mFirebasemethods;
    Uri uri;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private RequestManager glide;
    RequestOptions requestOptions;

    //Views
    ImageView placeHolder, plusCircle1, minusCircle2, minusCircle3, backArrow, typeName, arrow1, arrow2, arrow3;
    AutoCompleteTextView etFilledExposedDropdownCategory, etFilledExposedDropdownName, etFilledExposedDropdownPosition;
    TextInputLayout textInputLayoutCategory, textInputLayoutName, textInputLayoutPosition;
    TextView date1, date2, date3, failure1, failure2, failure3, send,percent;
    RelativeLayout relLayout_failure1, relLayout_failure2, relLayout_failure3,relativeLayout_parent;
    RelativeLayout relativeLayoutPicture;
    ProgressBar progressBar,progressBarMain;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write);

        typeName = findViewById(R.id.iv_type_name);
        backArrow = findViewById(R.id.iv_backArrow);
        send = findViewById(R.id.tv_send);
        placeHolder = findViewById(R.id.ivProfilePicturePlaceHolder);
        date1 = findViewById(R.id.et_date_1);
        date2 = findViewById(R.id.et_date_2);
        date3 = findViewById(R.id.et_date_3);
        failure1 = findViewById(R.id.et_failure_1);
        failure2 = findViewById(R.id.et_failure_2);
        failure3 = findViewById(R.id.et_failure_3);
        plusCircle1 = findViewById(R.id.iv_plus_1);
        minusCircle2 = findViewById(R.id.iv_minus_2);
        minusCircle3 = findViewById(R.id.iv_minus_3);
        arrow1 = findViewById(R.id.arrow_1);
        arrow2 = findViewById(R.id.arrow_2);
        arrow3 = findViewById(R.id.arrow_3);
        relLayout_failure1 = findViewById(R.id.relLayout_failure_1);
        relLayout_failure2 = findViewById(R.id.relLayout_failure_2);
        relLayout_failure3 = findViewById(R.id.relLayout_failure_3);
        relativeLayout_parent=findViewById(R.id.relLayout_parent);
        relativeLayoutPicture = findViewById(R.id.relativeLayout_profile_picture);
        progressBar = findViewById(R.id.progress_bar);
        progressBarMain=findViewById(R.id.progress_bar_main);
        percent=findViewById(R.id.percent);

        progressBarMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        //Category
        etFilledExposedDropdownCategory = findViewById(R.id.filled_exposed_dropdown_category);
        ArrayAdapter<CharSequence> adapterCategory = ArrayAdapter.createFromResource(this, R.array.categories,
                R.layout.dropdown_menu_popup_item);

        etFilledExposedDropdownCategory.setAdapter(adapterCategory);
        //Name
        etFilledExposedDropdownName = findViewById(R.id.filled_exposed_dropdown_name);
        //Position
        etFilledExposedDropdownPosition = findViewById(R.id.filled_exposed_dropdown_position);

        //
        initTextListener();

        mFirebasemethods = new FirebaseMethods(this);

        textInputLayoutCategory = findViewById(R.id.text_input_category);
        textInputLayoutName = findViewById(R.id.text_input_layout_name);
        textInputLayoutPosition = findViewById(R.id.text_input_layout_position);

        setInvisibility();

// getting draftletter
        try {
            Intent intent = getIntent();

            mDraftCV = intent.getParcelableExtra("draftCV");
            CURRENT_DRAFT_POSITION = intent.getIntExtra("position", -1);
            displayDraftContent();

        } catch (NullPointerException e) {

        }

        if (mDraftCV != null) {

            try {
                mCategory = mDraftCV.getCategory();
                Log.d(TAG, "Finding spinner position " + mCategory);
                etFilledExposedDropdownCategory.setText(mDraftCV.getCategory());
            } catch (NullPointerException e) {

            }
        }

        //set glide image size
        int dpValue = 115; // margin in dips
        float d = getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d); // margin in pixels

        glide = Glide.with(this);
        requestOptions = RequestOptions.overrideOf(margin, margin)
                .circleCrop()
                .fallback(R.drawable.ic_fail)
                .placeholder(R.drawable.ic_grey_circle)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        setupFirebaseAuth();
        setOnClickListeners();
    }



    private void setInvisibility() {
        arrow2.setVisibility(View.INVISIBLE);
        date2.setVisibility(View.INVISIBLE);
        relLayout_failure2.setVisibility(View.INVISIBLE);
        minusCircle2.setVisibility(View.INVISIBLE);

        arrow3.setVisibility(View.INVISIBLE);
        date3.setVisibility(View.INVISIBLE);
        relLayout_failure3.setVisibility(View.INVISIBLE);
        minusCircle3.setVisibility(View.INVISIBLE);
    }

    private void setOnClickListeners() {


        arrow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRotation(arrow1, failure1);
            }
        });

        arrow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRotation(arrow2, failure2);
            }
        });

        arrow3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRotation(arrow3, failure3);
            }
        });


        relativeLayoutPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");

                startActivityForResult(pickIntent, PICK_IMAGE);
            }
        });


        typeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText input = new EditText(WriteActivity.this);

                new MaterialAlertDialogBuilder(WriteActivity.this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                        .setView(input)
                        .setPositiveButton(getString(R.string.Confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                etFilledExposedDropdownName.setText(input.getText().toString());
                            }
                        })
                        .setNegativeButton(getString(R.string.Cancel), null)
                        .show();

            }
        });

        date1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickDatePicker(1);
            }
        });

        date2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickDatePicker(2);
            }
        });

        date3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickDatePicker(3);
            }
        });

        plusCircle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (relLayout_failure2.getVisibility() == View.INVISIBLE && relLayout_failure3.getVisibility() == View.INVISIBLE) {

                    //failure2
                    arrow2.setVisibility(View.VISIBLE);
                    date2.setVisibility(View.VISIBLE);
                    relLayout_failure2.setVisibility(View.VISIBLE);
                    minusCircle2.setVisibility(View.VISIBLE);

                } else if (relLayout_failure2.getVisibility() == View.VISIBLE && relLayout_failure3.getVisibility() == View.INVISIBLE) {

                    plusCircle1.setVisibility(View.INVISIBLE);
                    minusCircle2.setVisibility(View.INVISIBLE);

                    //failure3
                    arrow3.setVisibility(View.VISIBLE);
                    date3.setVisibility(View.VISIBLE);
                    relLayout_failure3.setVisibility(View.VISIBLE);
                    minusCircle3.setVisibility(View.VISIBLE);

                }

            }
        });

        minusCircle2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //failure2
                arrow2.setVisibility(View.INVISIBLE);
                date2.setVisibility(View.INVISIBLE);
                relLayout_failure2.setVisibility(View.INVISIBLE);
                failure2.setText("");
                date2.setText("");
                minusCircle2.setVisibility(View.INVISIBLE);
            }
        });

        minusCircle3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minusCircle2.setVisibility(View.VISIBLE);
                plusCircle1.setVisibility(View.VISIBLE);

                //failure3
                failure3.setText("");
                date3.setText("");
                arrow3.setVisibility(View.INVISIBLE);
                date3.setVisibility(View.INVISIBLE);
                relLayout_failure3.setVisibility(View.INVISIBLE);
                minusCircle3.setVisibility(View.INVISIBLE);

            }
        });


        //to be amended
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }

                saveBeforeBackNavigation();

            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                relativeLayout_parent.requestFocus();

                boolean emptyCVcontent = checkIfEmptyFailureContent();
                mDate1 = date1.getText().toString();
                mDate2 = date2.getText().toString();
                mDate3 = date3.getText().toString();
                mFailure1 = failure1.getText().toString();
                mFailure2 = failure2.getText().toString();
                mFailure3 = failure3.getText().toString();

                try {

                    //check if necessary info is filled out
                    if (!etFilledExposedDropdownCategory.getText().toString().equals("") &&
                            etFilledExposedDropdownCategory.getText().toString().length() > 0) {

                        textInputLayoutCategory.setErrorEnabled(false);

                        String category = etFilledExposedDropdownCategory.getText().toString();
                        String name = etFilledExposedDropdownName.getText().toString().trim();

                        if (category.equals(getString(R.string.Football))) {

                            String position = etFilledExposedDropdownPosition.getText().toString();


                            Log.d(TAG, "onClick: " + emptyCVcontent);
                            if (!name.equals("") && name.length() > 0 && !position.equals("") && position.length() > 0
                                    && !emptyCVcontent) {
                                //send to database
                                sendToDatabase(uri, name, position, category, mDate1,
                                        mFailure1, mDate2, mFailure2, mDate3, mFailure3);
                            }

                            if (name.equals("") || name.length() == 0) {
                                textInputLayoutName.setErrorEnabled(true);
                                textInputLayoutName.setError(getString(R.string.empty_field));
                            } else {
                                textInputLayoutName.setErrorEnabled(false);
                            }

                            if (position.equals("") || position.length() == 0) {
                                textInputLayoutPosition.setErrorEnabled(true);
                                textInputLayoutPosition.setError(getString(R.string.empty_field));
                            } else {
                                textInputLayoutPosition.setErrorEnabled(false);
                            }


                        } else if (category.equals(getString(R.string.Politics))) {
                            if (!name.equals("") && name.length() > 0 && !emptyCVcontent) {
                                //send to database
                                sendToDatabase(uri, name, "", category, mDate1,
                                        mFailure1, mDate2, mFailure2, mDate3, mFailure3);
                            }

                        } else {

                            if (!emptyCVcontent) {
                                //send to database
                                sendToDatabase(uri, "", "", category, mDate1,
                                        mFailure1, mDate2, mFailure2, mDate3, mFailure3);

                            }
                        }

                    } else {
                        textInputLayoutCategory.setErrorEnabled(true);
                        textInputLayoutCategory.setError(getString(R.string.empty_field));
                    }

                } catch (NullPointerException e) {

                }
            }
        });

    }


    private void setRotation(ImageView arrow, TextView textView) {

        if (textView.getVisibility() == View.VISIBLE) {
            textView.setVisibility(View.GONE);
            arrow.animate().rotationBy(180).setDuration(50).start();
        } else {
            textView.setVisibility(View.VISIBLE);
            arrow.animate().rotationBy(-180).setDuration(50).start();
        }
    }

    private void initTextListener() {
        Log.d(TAG, "initTextListener: initializing");

        etFilledExposedDropdownCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = etFilledExposedDropdownCategory.getText().toString();

                if (text.equals(getString(R.string.Football))) {
                    //Name
                    textInputLayoutName.setVisibility(View.VISIBLE);
                    typeName.setVisibility(View.VISIBLE);
                    etFilledExposedDropdownName.setText("");
                    ArrayAdapter<CharSequence> adapterName = ArrayAdapter.createFromResource(WriteActivity.this, R.array.football_players,
                            R.layout.dropdown_menu_popup_item);
                    etFilledExposedDropdownName.setAdapter(adapterName);

                    //Position
                    textInputLayoutPosition.setVisibility(View.VISIBLE);
                    ArrayAdapter<CharSequence> adapterPosition = ArrayAdapter.createFromResource(WriteActivity.this, R.array.football_positions,
                            R.layout.dropdown_menu_popup_item);

                    etFilledExposedDropdownPosition.setAdapter(adapterPosition);

                } else if (text.equals(getString(R.string.Life)) || text.equals(getString(R.string.Relationship))) {
                    //Name
                    textInputLayoutName.setVisibility(View.INVISIBLE);
                    typeName.setVisibility(View.GONE);

                    //Position
                    textInputLayoutPosition.setVisibility(View.INVISIBLE);
                } else if (text.equals(getString(R.string.Politics))) {
                    //Name
                    textInputLayoutName.setVisibility(View.VISIBLE);
                    etFilledExposedDropdownName.setText("");
                    typeName.setVisibility(View.VISIBLE);
                    ArrayAdapter<CharSequence> adapterName = ArrayAdapter.createFromResource(WriteActivity.this, R.array.politicians,
                            R.layout.dropdown_menu_popup_item);
                    etFilledExposedDropdownName.setAdapter(adapterName);

                    //Position
                    textInputLayoutPosition.setVisibility(View.INVISIBLE);
                }

            }
        });
    }


    @Override
    public void onBackPressed() {

            saveBeforeBackNavigation();

    }

    private void saveBeforeBackNavigation() {

        final String name = etFilledExposedDropdownName.getText().toString().trim();
        final String position = etFilledExposedDropdownPosition.getText().toString().trim();
        final String category = etFilledExposedDropdownCategory.getText().toString().trim();
        final String firstDate = date1.getText().toString().trim();
        final String firstFailure = failure1.getText().toString().trim();
        final String secondDate = date2.getText().toString().trim();
        final String secondFailure = failure2.getText().toString().trim();
        final String thirdDate = date3.getText().toString().trim();
        final String thirdFailure = failure3.getText().toString().trim();
        boolean empty = true;

        if (firstDate.length() > 0 || firstFailure.length() > 0 ||
                secondDate.length() > 0 || secondFailure.length() > 0 ||
                thirdDate.length() > 0 || thirdFailure.length() > 0) {
            empty = false;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog)
                .setCancelable(true);

        if (uri == null) {

            builder.setMessage(getString(R.string.save_draft_question))
                    .setNegativeButton(getString(R.string.Discard), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            WriteActivity.super.onBackPressed();
                        }
                    });


            if (mDraftCV != null) {

                if (!checkIfSameAsOldDraft(name, position, category, firstDate, firstFailure,
                        secondDate, secondFailure,
                        thirdDate, thirdFailure)) {

                    builder
                            .setPositiveButton(getString(R.string.Save), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressBarMain.setVisibility(View.VISIBLE);
                                    mFirebasemethods.updateDraftCVDatabase(name, position, category, firstDate, firstFailure,
                                            secondDate, secondFailure,
                                            thirdDate, thirdFailure,
                                            mDraftCV.getCvId(), CURRENT_DRAFT_POSITION);
                                }
                            })
                            .show();

                } else{
                    super.onBackPressed();
                }

            } else {

                if (!empty) {

                    builder.setPositiveButton(getString(R.string.Save), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressBarMain.setVisibility(View.VISIBLE);

                            mFirebasemethods.addDraftCVToDatabase(name, position, category, firstDate, firstFailure,
                                    secondDate, secondFailure,
                                    thirdDate, thirdFailure);

                        }
                    }).show();

                } else {
                    super.onBackPressed();
                }
            }

        } else {

            builder
                    .setMessage(getString(R.string.save_draft_picture_question))
                    .setPositiveButton(getString(R.string.Discard), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            WriteActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(getString(R.string.Cancel), null)
                    .show();

        }

    }


    private void clickDatePicker(final int number) {

        FragmentManager fm = getSupportFragmentManager();

        MonthYearPickerDialog monthYearPickerDialog=null;

        try {
            monthYearPickerDialog = (MonthYearPickerDialog) fm.findFragmentByTag("datePicker");

        } catch (ClassCastException e) {

        }


        if (monthYearPickerDialog == null) {

             monthYearPickerDialog =MonthYearPickerDialog.newInstance(Calendar.getInstance().getTime(),number);
        }

        if (!monthYearPickerDialog.isAdded()) {

            monthYearPickerDialog.show(getSupportFragmentManager(), "datePicker");
        }

    }

    private boolean checkIfEmptyFailureContent() {

        boolean empty = false;

        String firstDate = date1.getText().toString();
        String firstFailure = failure1.getText().toString().trim();
        String secondDate = date2.getText().toString();
        String secondFailure = failure2.getText().toString().trim();
        String thirdDate = date3.getText().toString();
        String thirdFailure = failure3.getText().toString().trim();

        if (firstDate.equals("") || firstDate.length() == 0) {
            date1.setBackgroundResource(R.drawable.error_borders);
            empty = true;
        } else {
            date1.setBackgroundResource(R.drawable.grey_border_top);
        }

        if (firstFailure.equals("") || firstFailure.length() == 0) {
            failure1.setBackgroundResource(R.drawable.error_borders);
            empty = true;
        } else {
            failure1.setBackground(null);
        }

        if (secondDate.length() > 0 && secondFailure.length() == 0) {
            failure2.setBackgroundResource(R.drawable.error_borders);
            date2.setBackgroundResource(R.drawable.grey_border_top);
            empty = true;
        } else if (secondDate.length() == 0 && secondFailure.length() > 0) {
            date2.setBackgroundResource(R.drawable.error_borders);
            failure2.setBackground(null);
            empty = true;
        } else {
            date2.setBackgroundResource(R.drawable.grey_border_top);
            failure2.setBackground(null);
        }


        if (thirdDate.length() > 0 && thirdFailure.length() == 0) {
            failure3.setBackgroundResource(R.drawable.error_borders);
            date3.setBackgroundResource(R.drawable.grey_border_top);
            empty = true;
        } else if (secondDate.length() == 0 && secondFailure.length() > 0) {
            date3.setBackgroundResource(R.drawable.error_borders);
            failure3.setBackground(null);
            empty = true;
        } else {
            date3.setBackgroundResource(R.drawable.grey_border_top);
            failure3.setBackground(null);
        }

        return empty;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE) {
            //TODO: action
            if(resultCode== Activity.RESULT_OK){

                uri=data.getData();
                glide.load(data.getData())
                        .apply(requestOptions)
                        .into(placeHolder);

            }
        }

    }

    public void sendToDatabase(Uri troll_picture, String name, String position, String category, String date1,
                               String failure1, String date2, String failure2, String date3, String failure3
    ) {

        Log.d(TAG, "sendToDatabase: ");
        String draftCVId = null;

        try {

            draftCVId = mDraftCV.getCvId();
        } catch (NullPointerException e) {

        }


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mFirebasemethods.addCvToDatabase(troll_picture, name, position, category, date1,
                failure1, date2, failure2, date3, failure3, draftCVId,progressBar,progressBarMain,percent);


    }

    private void displayDraftContent() {

        try {
            etFilledExposedDropdownCategory.setText(mDraftCV.getCategory());
        } catch (NullPointerException e) {

        }

        try {
            etFilledExposedDropdownName.setText(mDraftCV.getName());
        } catch (NullPointerException e) {

        }
        try {

            etFilledExposedDropdownPosition.setText(mDraftCV.getPosition());
        } catch (NullPointerException e) {

        }
        try {
            date1.setText(mDraftCV.getDate1());
        } catch (NullPointerException e) {

        }


        try {
            failure1.setText(mDraftCV.getFailure1());

        } catch (NullPointerException e) {

        }

        try {
            if (mDraftCV.getDate2().length() > 0) {

                date2.setText(mDraftCV.getDate2());
                arrow2.setVisibility(View.VISIBLE);
                date2.setVisibility(View.VISIBLE);
                relLayout_failure2.setVisibility(View.VISIBLE);
            }

        } catch (NullPointerException e) {

        }

        try {
            if (mDraftCV.getFailure2().length() > 0) {

                failure2.setText(mDraftCV.getFailure2());
                arrow2.setVisibility(View.VISIBLE);
                relLayout_failure2.setVisibility(View.VISIBLE);
                date2.setVisibility(View.VISIBLE);
            }

        } catch (NullPointerException e) {

        }
        try {
            if (mDraftCV.getDate3().length() > 0) {

                date3.setText(mDraftCV.getDate3());
                date3.setVisibility(View.VISIBLE);
                arrow3.setVisibility(View.VISIBLE);
                relLayout_failure3.setVisibility(View.VISIBLE);
            }

        } catch (NullPointerException e) {

        }
        try {
            if (mDraftCV.getFailure3().length() > 0) {

                failure3.setText(mDraftCV.getFailure3());
                relLayout_failure3.setVisibility(View.VISIBLE);
                arrow3.setVisibility(View.VISIBLE);
                date3.setVisibility(View.VISIBLE);
            }

        } catch (NullPointerException e) {

        }
    }

    public boolean checkIfSameAsOldDraft(final String name,final String position,final String category,final String firstDate,
                                      final String firstFailure,final String secondDate,final String secondFailure,
                                      final String thirdDate,final String thirdFailure){

        boolean mSame=true;

        if(!name.equals(mDraftCV.getName())){
            mSame=false;
        }
        if(!position.equals(mDraftCV.getPosition())){
            mSame=false;
        }
        if(!category.equals(mDraftCV.getCategory())){
            mSame=false;
        }

        if(!firstDate.equals(mDraftCV.getDate1())){
            mSame=false;
        }

        if(!firstFailure.equals(mDraftCV.getFailure1())){
            mSame=false;
        }

        if(!secondDate.equals(mDraftCV.getDate2())){
            mSame=false;
        }

        if(!secondFailure.equals(mDraftCV.getFailure2())){
            mSame=false;
        }

        if(!thirdDate.equals(mDraftCV.getDate3())){
            mSame=false;
        }

        if(!thirdFailure.equals(mDraftCV.getFailure3())){
            mSame=false;
        }

        return mSame;
    }

          /*
    ------------------------------------ Firebase ---------------------------------------------
     */


    /**
     * Setup the firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                checkCurrentUser(firebaseAuth.getCurrentUser());
            }
        };

    }

    private void checkCurrentUser(FirebaseUser user) {
        Log.d(TAG, "checkCurrentUser: checking if user is logged in.");


        if (user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            finish();
            startActivity(intent);
            this.overridePendingTransition(0,0);
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is signed in
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(FCMService.COMMENT_NOTIFICATION_ID);
            notificationManager.cancel(FCMService.CV_NOTIFICATION_ID);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
