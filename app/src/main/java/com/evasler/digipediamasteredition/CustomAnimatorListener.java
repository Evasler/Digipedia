package com.evasler.digipediamasteredition;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import static com.evasler.digipediamasteredition.CustomAnimatorListener.Scales.SCALE_IMAGE;
import static com.evasler.digipediamasteredition.CustomAnimatorListener.Scales.SCALE_IN;
import static com.evasler.digipediamasteredition.CustomAnimatorListener.Scales.SCALE_OUT;

class CustomAnimatorListener implements Animator.AnimatorListener {

    enum Scales {
        SCALE_IN, SCALE_OUT, SCALE_IMAGE, SCALE_IN_X, SCALE_OUT_X}

    private Activity activity;
    private Scales scaling;
    private int animatedViewId;

    CustomAnimatorListener(Activity activity, Scales scaling) {
        this.activity = activity;
        this.scaling = scaling;
    }

    void setAnimatedLayout(int animatedViewId) {
        this.animatedViewId = animatedViewId;
    }

    @Override
    public void onAnimationStart(Animator animation) {
        switch (scaling) {
            case SCALE_IN:
                activity.findViewById(animatedViewId).setVisibility(View.VISIBLE);
                break;
            case SCALE_OUT:
            case SCALE_IMAGE:
                ((MainActivity) activity).setBackBlocked(true);
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                break;
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        switch (scaling) {
            case SCALE_OUT:
                activity.findViewById(animatedViewId).setVisibility(View.GONE);
                break;
            case SCALE_IMAGE:
                if (activity.findViewById(R.id.scalableDigimonImage).getContentDescription().equals("normal")) {
                    Log.d("contentDescription", "changed");
                    activity.findViewById(R.id.scalableDigimonImage).setContentDescription("scaled");
                } else if (activity.findViewById(R.id.scalableDigimonImage).getContentDescription().equals("scaled")) {
                    activity.findViewById(R.id.scalableDigimonImage).setContentDescription("normal");
                }
            case SCALE_IN:
                ((MainActivity) activity).setBackBlocked(false);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                break;
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
