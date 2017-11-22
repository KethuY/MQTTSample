package com.atg.onecontrolv3.helpers;

import android.app.Activity;

import com.atg.onecontrolv3.R;

/**
 * Created by Bharath on 30-Aug-17
 */

public class ThemeSelection {

    public final static int THEME_ONE = 0;
    public final static int THEME_TWO = 1;
    public final static int THEME_THREE = 2;
    public final static int THEME_FOUR = 3;
    private static int sTheme;

    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme) {
        sTheme = theme;
        /*activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));*/
        activity.recreate();
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    public static void onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            default:
            case THEME_ONE:
                activity.setTheme(R.style.ThemeOne);
                break;
            case THEME_TWO:
                activity.setTheme(R.style.ThemeTwo);
                break;
            case THEME_THREE:
                activity.setTheme(R.style.ThemeThree);
                break;
            case THEME_FOUR:
                activity.setTheme(R.style.ThemeFour);
                break;
        }
    }
}
