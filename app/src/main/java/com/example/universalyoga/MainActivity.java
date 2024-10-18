package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.Intent;
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





        dbHelper = new YogaDatabaseHelper(this);
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
