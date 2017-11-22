package com.atg.onecontrolv3.preferances;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.ContextThemeWrapper;

import com.atg.onecontrolv3.R;

/**
 * Created by Bharath on 1/7/2016
 */
public class MyPreferences {

    public static void add(PrefType name, int value, Context context) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name.toString(), value);
        editor.apply();
    }

    public static void add(PrefType name, float value, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(name.toString(), value);
        editor.apply();
    }

    public static void add(PrefType name, String value, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name.toString(), value);
        editor.apply();
    }

    public static void add(PrefType name, boolean value, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name.toString(), value);
        editor.apply();
    }

   /* public static void add(PrefType name, ArrayList<String> value, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name.toString(), String.valueOf(value));
        editor.commit();
    }*/

    public static int getInt(PrefType name, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getInt(name.toString(), 0);
    }

    public static double getDouble(PrefType name, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getFloat(name.toString(), 0f);
    }

    public static String getString(PrefType name, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getString(name.toString(), null);
    }

    public static boolean getBoolean(PrefType name, Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return settings.getBoolean(name.toString(), false);
    }

    public static boolean hasString(PrefType name, Context context) {
        return (getString(name, context) != null);
    }

    /*Theme Preferences*/
    public static void applyTheme(ContextThemeWrapper contextThemeWrapper) {
        switch (MyPreferences.getInt(PrefType.THEME_COLOR, contextThemeWrapper)) {
            case 1:
                contextThemeWrapper.setTheme(R.style.ThemeOne);
                break;
            case 2:
                contextThemeWrapper.setTheme(R.style.ThemeTwo);
                break;
            case 3:
                contextThemeWrapper.setTheme(R.style.ThemeThree);
                break;
            case 4:
                contextThemeWrapper.setTheme(R.style.ThemeFour);
                break;
            default:
        }
    }

    /**
     * Types of preferences
     */
    public enum PrefType {
        THEME_COLOR,
        IMEI,
        MAC_ID,
        LOV,
        USER_MODE,
        MAC_PIN,
        GATEWAY_NAME,
        GATEWAY_POS,
        DEFAULT_AC_KEY,
        SHOW_IN_START_UP,
        MacName
    }
}
