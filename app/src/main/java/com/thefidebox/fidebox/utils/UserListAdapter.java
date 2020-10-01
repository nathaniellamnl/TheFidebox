package com.thefidebox.fidebox.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.thefidebox.fidebox.R;
import com.thefidebox.fidebox.models.Users;

import java.util.List;

public class UserListAdapter extends ArrayAdapter {

    private static final String TAG = "UserListAdapter";


    private LayoutInflater mInflater;
    private List<Users> mUsers = null;
    private int layoutResource;
    private Context mContext;
    private RequestManager glide;
    RequestOptions requestOptions;


    public UserListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Users> objects) {
        super(context, resource, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mUsers = objects;

        this.glide = Glide.with(mContext);


        //set glide image size
        int dpValue = 60; // margin in dips
        float d = mContext.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d); // margin in pixels

        requestOptions = RequestOptions.overrideOf(margin, margin)
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

    }

    private static class ViewHolder {
        TextView username, bio;
        ImageView profileImage;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.username);
            holder.bio =  convertView.findViewById(R.id.tv_bio);
            holder.profileImage =  convertView.findViewById(R.id.profile_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        Users userAccountSettings = (Users) getItem(position);

        holder.username.setText(userAccountSettings.getUsername());
        holder.bio.setText(userAccountSettings.getBio());

        FirebaseFirestore.getInstance().collection(mContext.getString(R.string.col_users))
                .whereEqualTo(mContext.getString(R.string.field_userId), userAccountSettings.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                Users userAc=document.toObject(Users.class);

                                glide.load(userAc.getProfile_photo())
                                        .apply(requestOptions)
                                        .into(holder.profileImage);

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return convertView;
    }
}
