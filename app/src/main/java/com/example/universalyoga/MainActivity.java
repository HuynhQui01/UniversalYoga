package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {


    private SessionManger sessionManger;
    private ImageButton btnLogin;

    private YogaDatabaseHelper dbHelper;

    private final Handler handler = new Handler();
    private final int SYNC_INTERVAL = 30 * 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Mapping();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                if (item.getItemId() == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                }
                else if (item.getItemId() == R.id.nav_class) {
                    selectedFragment = new ClassFragment();
                } else if (item.getItemId() == R.id.nav_manage) {
                    if(dbHelper.getUserRoleByEmail(sessionManger.getUserEmail(MainActivity.this)) == null){
                        selectedFragment = new HomeFragment();
                        Toast.makeText(MainActivity.this, "You do not have permission to go to this!", Toast.LENGTH_SHORT).show();
                    }else{
                        if(dbHelper.getUserRoleByEmail(sessionManger.getUserEmail(MainActivity.this)).equals("admin")){
                            selectedFragment = new ManageFragment();
                        }else{
                            Toast.makeText(MainActivity.this, "You do not have permission to go to this!", Toast.LENGTH_SHORT).show();
                            selectedFragment = new HomeFragment();
                        }
                    }

                } else if(item.getItemId() == R.id.nav_profile){
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }

                return true;
            }
        });


        startDataSync();


        dbHelper = new YogaDatabaseHelper(this);
    }

    private void startDataSync() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isInternetAvailable()) {
                    dbHelper.syncAllDataToFirebase();
                    dbHelper.syncAllDataFromFirebaseToSQLite();

                }
                handler.postDelayed(this, SYNC_INTERVAL);
            }
        }, SYNC_INTERVAL);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }



    private void Mapping() {

        sessionManger = new SessionManger();

        dbHelper = new YogaDatabaseHelper(this);
    }
}
