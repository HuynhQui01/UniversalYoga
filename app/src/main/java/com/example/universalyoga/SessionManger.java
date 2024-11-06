package com.example.universalyoga;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManger {
    // Method to save the login status
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_NAME = "userEmail";
    private static final String KEY_USER_EMAIL = "userEmail";

    // Method to save the login status
    public void saveLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    // Method to check if the user is logged in
    public boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false); // Default is false if no value is set
    }

    // Method to log out the user
    public void logoutUser(Context context) {
        saveLoginStatus(context, false);

        setUserName(context, null);
        Log.d("SessionManager", "User logged out.");
    }

    // Method to save the user's email
    public void setUserName(Context context, String email) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, email);
        editor.apply();
    }

    public void setUserEmail(Context context, String name) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, name);
        editor.apply();
    }

    public String getUserEmail(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_EMAIL, ""); // Default is null if no value is set
    }
}
