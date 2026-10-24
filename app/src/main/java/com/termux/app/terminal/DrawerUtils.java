package com.termux.app.terminal;

import android.app.Activity;
import android.util.DisplayMetrics;

import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import com.termux.shared.logger.Logger;

import java.lang.reflect.Field;

public class DrawerUtils {

    private static final String LOG_TAG = "DrawerUtils";

    /**
     * Expand the drawer drag edge size to allow opening the drawer from a wider area
     * (e.g. left 60% of the screen), bypassing Android system gesture navigation edge area.
     *
     * @param activity The host Activity
     * @param drawerLayout The DrawerLayout instance
     * @param displayWidthPercentage Percentage of screen width for the drag edge (e.g. 0.6f)
     */
    public static void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
        if (activity == null || drawerLayout == null) return;
        try {
            Field leftDraggerField = DrawerLayout.class.getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            if (leftDragger == null) return;

            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            int targetEdgeSize = (int) (dm.widthPixels * displayWidthPercentage);

            // Directly set initially
            leftDragger.setEdgeSize(targetEdgeSize);

            // DrawerLayout#onLayout() resets edgeSize using system gesture insets on Android 10+.
            // Attach an OnPreDrawListener to guarantee the edgeSize stays expanded on every layout pass.
            drawerLayout.getViewTreeObserver().addOnPreDrawListener(() -> {
                try {
                    if (leftDragger.getEdgeSize() < targetEdgeSize) {
                        leftDragger.setEdgeSize(targetEdgeSize);
                    }
                } catch (Exception ignored) {
                }
                return true;
            });

            android.util.Log.e("DRAWER_TEST", "Drawer left edge size locked to " + targetEdgeSize + " px (" + (displayWidthPercentage * 100) + "%)");
            Logger.logInfo(LOG_TAG, "Drawer left edge size locked to " + targetEdgeSize + " px (" + (displayWidthPercentage * 100) + "%)");
        } catch (Exception e) {
            android.util.Log.e("DRAWER_TEST", "Failed to set drawer edge size: " + e.getMessage(), e);
            Logger.logError(LOG_TAG, "Failed to set drawer edge size: " + e.getMessage());
        }
    }

}
