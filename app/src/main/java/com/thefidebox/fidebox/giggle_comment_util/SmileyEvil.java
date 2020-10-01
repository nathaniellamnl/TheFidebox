package com.thefidebox.fidebox.giggle_comment_util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class SmileyEvil {

    private static final String TAG = "Heart";

    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();

    private boolean giggleUploading;

    public ImageView giggleBlack, giggleGreen;

    public SmileyEvil(ImageView giggleBlack, ImageView giggleGreen) {
        this.giggleBlack = giggleBlack;
        this.giggleGreen = giggleGreen;
        this.giggleUploading=false;
    }

    public void toggleLike(){
        Log.d(TAG, "toggleLike: toggling hand.");

        AnimatorSet animationSet =  new AnimatorSet();


        if(giggleGreen.getVisibility() == View.VISIBLE){
            Log.d(TAG, "toggleLike: toggling red hand off.");
            giggleGreen.setScaleX(0.1f);
            giggleGreen.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(giggleGreen, "scaleY", 1f, 0f);
            scaleDownY.setDuration(200);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(giggleGreen, "scaleX", 1f, 0f);
            scaleDownX.setDuration(200);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            giggleGreen.setVisibility(View.GONE);
            giggleBlack.setVisibility(View.VISIBLE);

            animationSet.playTogether(scaleDownY, scaleDownX);
        }

        else if(giggleGreen.getVisibility() == View.GONE){
            Log.d(TAG, "toggleLike: toggling red hand on.");
            giggleGreen.setScaleX(0.1f);
            giggleGreen.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(giggleGreen, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(50);
            scaleDownY.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(giggleGreen, "scaleX", 0.1f, 1f);
            scaleDownX.setDuration(50);
            scaleDownX.setInterpolator(DECCELERATE_INTERPOLATOR);

            giggleGreen.setVisibility(View.VISIBLE);
            giggleBlack.setVisibility(View.GONE);

            animationSet.playTogether(scaleDownY, scaleDownX);
        }

        animationSet.start();

    }

    public boolean getGiggleUploading() {
        return giggleUploading;
    }

    public void setGiggleUploading(boolean giggleUploading) {
        this.giggleUploading = giggleUploading;
    }
}
