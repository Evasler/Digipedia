package com.evasler.digipediamasteredition;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

public class LockedHorizontalScrollView extends HorizontalScrollView {


    public LockedHorizontalScrollView(Context context) {
        super(context);
    }

    public LockedHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockedHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                return false && super.onTouchEvent(ev);
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        return false && super.onInterceptTouchEvent(ev);
    }

}