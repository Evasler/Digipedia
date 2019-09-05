package com.evasler.digipediamasteredition;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import static com.evasler.digipediamasteredition.CustomAnimatorListener.Scales.SCALE_IMAGE;
import static com.evasler.digipediamasteredition.CustomAnimatorListener.Scales.SCALE_IN;
import static com.evasler.digipediamasteredition.CustomAnimatorListener.Scales.SCALE_OUT;

public class TransitionAnimators {

    private CustomAnimatorListener scaleInCustomAnimatorListener, scaleOutCustomAnimatorListener, scaleDigimonImageCustomAnimatorListener,
        scaleInRightCustomAnimatorListener, scaleOutLeftCustomAnimatorListener;

    private AnimatorSet scaleIn, scaleOut, scaleDigimonImage, scaleInRight, scaleOutLeft;

    private Activity activity;

    TransitionAnimators(Activity activity) {

        this.activity = activity;

        scaleIn = new AnimatorSet();
        scaleOut = new AnimatorSet();
        scaleDigimonImage = new AnimatorSet();
        scaleInRight = new AnimatorSet();
        scaleOutLeft = new AnimatorSet();

        scaleInCustomAnimatorListener = new CustomAnimatorListener(activity, SCALE_IN);
        scaleOutCustomAnimatorListener = new CustomAnimatorListener(activity, SCALE_OUT);
        scaleDigimonImageCustomAnimatorListener = new CustomAnimatorListener(activity, SCALE_IMAGE);
        scaleInRightCustomAnimatorListener = new CustomAnimatorListener(activity, SCALE_IN);
        scaleOutLeftCustomAnimatorListener = new CustomAnimatorListener(activity, SCALE_OUT);
    }

    void initializeScaleInRightAnimator(int layoutId) {

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(activity.findViewById(layoutId), "scaleX", 1f);

        scaleInRight = new AnimatorSet();
        scaleInRight.play(scaleInX);
        scaleInRight.setDuration(300);

        scaleInRightCustomAnimatorListener.setAnimatedLayout(layoutId);
        scaleInRight.addListener(scaleInRightCustomAnimatorListener);
    }

    void initializeScaleOutLeftAnimator(int layoutId) {

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(activity.findViewById(layoutId), "scaleX", 0f);

        scaleOutLeft = new AnimatorSet();
        scaleOutLeft.play(scaleOutX);
        scaleOutLeft.setDuration(300);

        scaleOutLeftCustomAnimatorListener.setAnimatedLayout(layoutId);
        scaleOutLeft.addListener(scaleOutLeftCustomAnimatorListener);
    }

    void initializeScaleInAnimator(int layoutId) {

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(activity.findViewById(layoutId), "scaleX", 1f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(activity.findViewById(layoutId), "scaleY", 1f);

        scaleIn = new AnimatorSet();
        scaleIn.playTogether(scaleInX, scaleInY);
        scaleIn.setDuration(500);

        scaleInCustomAnimatorListener.setAnimatedLayout(layoutId);
        scaleIn.addListener(scaleInCustomAnimatorListener);
    }

    void initializeScaleOutAnimator(int layoutId) {

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(activity.findViewById(layoutId), "scaleX", 0f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(activity.findViewById(layoutId), "scaleY", 0f);

        scaleOut = new AnimatorSet();
        scaleOut.playTogether(scaleOutX, scaleOutY);
        scaleOut.setDuration(500);

        scaleOutCustomAnimatorListener.setAnimatedLayout(layoutId);
        scaleOut.addListener(scaleOutCustomAnimatorListener);
    }

    void initializeScaleDigimonImageAnimator() {

        float fadeFrom = 0;
        float fadeTo = 0;
        float movementX = 0;
        float movementY = 0;
        float scale = 0;

        ImageView digimonImage = (ImageView) ((RelativeLayout) activity.findViewById(R.id.scalableDigimonImageContainer)).getChildAt(1);
        RelativeLayout digimonImageContainer = activity.findViewById(R.id.digimonImageContainer);
        ImageView baseDigimonImage = activity.findViewById(R.id.digimonImage);

        if (digimonImage.getContentDescription().equals("normal")) {

            fadeFrom = 0;
            fadeTo = 1;
            movementX = (((MainActivity) activity).getWidth() >> 1) - (digimonImage.getWidth() >> 1);
            movementY = (((MainActivity) activity).getHeight() >> 1) - (digimonImage.getHeight() >> 1);
            scale = 2f;
        } else if (digimonImage.getContentDescription().equals("scaled")) {

            RelativeLayout profileContainer = activity.findViewById(R.id.profileContainer);
            profileContainer.measure(0,0);

            fadeFrom = 1;
            fadeTo = 0;
            movementX = (10 + 8 + 5) * activity.getResources().getDisplayMetrics().density + ((digimonImageContainer.getWidth() - baseDigimonImage.getWidth()) >> 1);
            movementY = (float) ((10 + 6 + 2.5) * activity.getResources().getDisplayMetrics().density + profileContainer.getHeight() + ((digimonImageContainer.getHeight() - baseDigimonImage.getHeight()) >> 1));
            scale = 1f;
        }

        ObjectAnimator translationX = ObjectAnimator.ofFloat(digimonImage, "translationX", movementX);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(digimonImage, "translationY", movementY);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(digimonImage, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(digimonImage, "scaleY", scale);
        ObjectAnimator fade = ObjectAnimator.ofFloat(activity.findViewById(R.id.blackWall), "alpha", fadeFrom, fadeTo);

        scaleDigimonImage = new AnimatorSet();
        scaleDigimonImage.playTogether(translationX, translationY, scaleX, scaleY, fade);
        scaleDigimonImage.setDuration(400);

        scaleDigimonImage.addListener(scaleDigimonImageCustomAnimatorListener);
    }

    void startScaleIn() { scaleIn.start(); }

    void startScaleOut() {
        scaleOut.start();
    }

    void startScaleInRight() { scaleInRight.start(); }

    void startScaleOutLeft() { scaleOutLeft.start(); }

    void startScaleDigimonImage() { scaleDigimonImage.start(); }

    AnimatorSet getScaleIn() {
        return scaleIn;
    }

    AnimatorSet getScaleOut() {
        return scaleOut;
    }

    AnimatorSet getScaleInRight() {
        return scaleInRight;
    }

    AnimatorSet getScaleOutLeft() {
        return scaleOutLeft;
    }

    AnimatorSet getScaleDigimonImage() { return scaleDigimonImage; }
}
