package com.thefidebox.fidebox.ScrollingBehaviour;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomViewScrollingBehaviour  extends CoordinatorLayout.Behavior<BottomNavigationView> {

    public BottomViewScrollingBehaviour() {
        super();
    }

    public BottomViewScrollingBehaviour(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, BottomNavigationView child, @NonNull View dependency) {
        boolean dependsOn = dependency instanceof FrameLayout;
        return dependsOn;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull BottomNavigationView child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {

//        super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
        if(type==ViewCompat.TYPE_TOUCH){

            return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
        } else{
           return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type);
        }

    }


    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull BottomNavigationView child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {

        if(type==ViewCompat.TYPE_TOUCH /*TYPE_TOUCH*/) {

            if (dy < 0) {
                showBottomNavigationView(child);
            } else if (dy > 0) {
                hideBottomNavigationView(child);
            }

        }
    }

    private void hideBottomNavigationView(BottomNavigationView view) {
        view.animate().translationY(view.getHeight());
    }

    private void showBottomNavigationView(BottomNavigationView view) {
        view.animate().translationY(0);
    }
}
