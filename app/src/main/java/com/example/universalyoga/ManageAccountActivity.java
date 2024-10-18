package com.example.universalyoga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageAccountActivity extends AppCompatActivity {

    RecyclerView rcvUser;
    UserAdapter userAdapter;
    YogaDatabaseHelper dbHelper;
    Button btnCreateAcc;
    List<User> lstUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_account);

        Mapping();

        try {
            lstUser = dbHelper.getAllUsers();
            userAdapter = new UserAdapter(lstUser);
            rcvUser.setAdapter(userAdapter);

        }catch (Exception e){
            Toast.makeText(this, "Fail to set adapter", Toast.LENGTH_SHORT).show();
        }

        btnCreateAcc.setOnClickListener(v -> {
            Intent createAcc = new Intent(this, CreateAccount.class);
            startActivity(createAcc);
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            lstUser = dbHelper.getAllUsers();
            userAdapter = new UserAdapter(lstUser);
            rcvUser.setAdapter(userAdapter);

        }catch (Exception e){
            Toast.makeText(this, "Fail to set adapter", Toast.LENGTH_SHORT).show();
        }
    }

    void Mapping(){
        rcvUser = findViewById(R.id.rcvUser);
        rcvUser.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new YogaDatabaseHelper(this);
        btnCreateAcc = findViewById(R.id.btnMAddUser);
    }
}