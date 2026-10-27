package com.termux.app.terminal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;

/**
 * A custom {@link DrawerLayout} that:
 * 1. Expands the swipe drag edge to 60% of the screen width to avoid modern Android 10+
 *    gesture navigation back zones (0-25dp).
 * 2. Cancels the internal 160ms mPeekRunnable immediately after ACTION_DOWN, completely
 *    preventing long-press events on the terminal screen from falsely triggering drawer peeking.
 */
public class TermuxDrawerLayout extends DrawerLayout {

    private ViewDragHelper mLeftDragger;
    private Runnable mLeftPeekRunnable;

    public TermuxDrawerLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public TermuxDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TermuxDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        try {
            Field draggerField = DrawerLayout.class.getDeclaredField("mLeftDragger");
            draggerField.setAccessible(true);
            mLeftDragger = (ViewDragHelper) draggerField.get(this);

            Field callbackField = DrawerLayout.class.getDeclaredField("mLeftCallback");
            callbackField.setAccessible(true);
            Object leftCallback = callbackField.get(this);
            if (leftCallback != null) {
                Field peekField = leftCallback.getClass().getDeclaredField("mPeekRunnable");
                peekField.setAccessible(true);
                mLeftPeekRunnable = (Runnable) peekField.get(leftCallback);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = super.onInterceptTouchEvent(ev);
        // Cancel the 160ms auto-peek runnable right after ACTION_DOWN,
        // so stationary long-presses never trigger the drawer!
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && mLeftPeekRunnable != null) {
            removeCallbacks(mLeftPeekRunnable);
        }
        return intercepted;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mLeftDragger != null) {
            int width = getWidth();
            if (width > 0) {
                // Expand edge size to 60% of screen to avoid gesture navigation back zone
                mLeftDragger.setEdgeSize((int) (width * 0.6f));
            }
        }
    }

}
