package com.example.universalyoga;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManger {
    // Method to save the login status
    public void saveLoginStatus(Context context, boolean isLoggedIn) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn);
        editor.apply();
    }

    // Method to check if the user is logged in
    public boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false); // Default is false if no value is set
    }

    // Method to log out the user
    public void logoutUser(Context context) {
        saveLoginStatus(context, false);
        Log.d("SessionManager", "User logged out.");
    }
}
