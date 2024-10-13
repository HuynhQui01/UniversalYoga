package com.example.universalyoga;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Handler handler;
    private Runnable runnable;
    private int[] imageArray = {R.drawable.yoga1, R.drawable.yoga2, R.drawable.yoga3};
    private int currentIndex = 0;
    private SessionManger sessionManger;
    private Button btnLogin;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.avatar), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Mapping();

//        btnLogin.setOnClickListener(v -> ManageLogin());


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

        YogaDatabaseHelper dbHelper = new YogaDatabaseHelper(this);
    }

protected void onStart(){
        super.onStart();
    FirebaseUser curUser = auth.getCurrentUser();
    if(curUser!=null){
        btnLogin.setText("Logout");
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                btnLogin.setText("Login");
            }
        });
    }else {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(messageIntent);
                btnLogin.setText("Logout");
            }
        });
    }
}

protected void onStop(){
        super.onStop();
    FirebaseUser curUser = auth.getCurrentUser();
    if(curUser!=null){
        btnLogin.setText("Logout");
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                btnLogin.setText("Login");
            }
        });
    }else {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(messageIntent);
                btnLogin.setText("Logout");
            }
        });
    }
}

//    void ManageLogin(){
//        FirebaseUser curUser = auth.getCurrentUser();
//        if(curUser!=null){
//            btnLogin.setText("Logout");
//            btnLogin.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    FirebaseAuth.getInstance().signOut();
//                    btnLogin.setText("Login");
//                }
//            });
//        }else {
//            btnLogin.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent messageIntent = new Intent(MainActivity.this, LoginActivity.class);
//                    startActivity(messageIntent);
//                    btnLogin.setText("Logout");
//                }
//            });
//        }
//    }

    void Mapping(){
        btnLogin = findViewById(R.id.btnMainLogin);
        sessionManger = new SessionManger();
        auth = FirebaseAuth.getInstance();
    }
}