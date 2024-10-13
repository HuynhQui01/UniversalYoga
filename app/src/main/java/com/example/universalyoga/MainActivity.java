package com.example.universalyoga;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Handler handler;
    private Runnable runnable;
    private int[] imageArray = {R.drawable.yoga1, R.drawable.yoga2, R.drawable.yoga3};
    private int currentIndex = 0;
    private SessionManger sessionManger;
    private Button btnLogin;
    private Button btnCreateAcc;
    private YogaDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);





        Mapping();

//        User user = new User("admin","admin@gmail.com","admin@123","admin","0000000");
//        dbHelper.addUser(user.getUsername(),user.getEmail(),user.getPassword(),user.getRole(),user.getPhone());



        btnCreateAcc.setOnClickListener(view -> {
            Intent createAcc = new Intent(MainActivity.this, CreateAccount.class);
            startActivity(createAcc);
        });

        imageView = findViewById(R.id.imgMain);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                imageView.setImageResource(imageArray[currentIndex]);
                currentIndex = (currentIndex + 1) % imageArray.length;
                handler.postDelayed(this, 3000);
            }
        };

        handler.post(runnable);

        TextView lbAppName = findViewById(R.id.lbAppName);
        lbAppName.setText(R.string.app_name);

        dbHelper = new YogaDatabaseHelper(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ManageLoginState();
    }

    private void ManageLoginState() {


        String username = dbHelper.getUsernameByEmail(sessionManger.getUserEmail(this));

        if (username != null) {
            if(!dbHelper.getUserRoleByEmail(sessionManger.getUserEmail(MainActivity.this)).equals("admin")){
                btnCreateAcc.setVisibility(View.INVISIBLE);
            }
            btnLogin.setText(username);
            btnLogin.setOnClickListener(view -> showLogoutConfirmationDialog());
            Toast.makeText(MainActivity.this, username, Toast.LENGTH_SHORT).show();

        } else {
            btnCreateAcc.setVisibility(View.INVISIBLE);
            Toast.makeText(MainActivity.this, "aaaaaaaaaa", Toast.LENGTH_SHORT).show();

            btnLogin.setText("Login");
            btnLogin.setOnClickListener(view -> {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            });
        }
    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    sessionManger.logoutUser(this);
                    btnLogin.setText("Login");
                    ManageLoginState();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void Mapping() {
        btnLogin = findViewById(R.id.btnMainLogin);
        sessionManger = new SessionManger();
        btnCreateAcc = findViewById(R.id.btnCreateAcc);
        dbHelper = new YogaDatabaseHelper(this);
    }
}
