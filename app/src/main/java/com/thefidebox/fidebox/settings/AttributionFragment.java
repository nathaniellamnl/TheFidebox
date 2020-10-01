package com.thefidebox.fidebox.settings;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.thefidebox.fidebox.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttributionFragment extends Fragment {

    TextView thirdPartySoftware;

    public AttributionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_attribution, container, false);
        thirdPartySoftware=view.findViewById(R.id.third_party_software);

        thirdPartySoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OssLicensesMenuActivity.class));
            }
        });

        return view;
    }

}
