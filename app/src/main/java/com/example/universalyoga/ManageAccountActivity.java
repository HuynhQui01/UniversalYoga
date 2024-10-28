package com.example.universalyoga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;
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
    List<User> lstUser, filteredList;
    SearchView searchView;
    ImageButton btnCreateAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        Mapping();


        try {
            lstUser = dbHelper.getAllUsers();
            filteredList = new ArrayList<>(lstUser);
            userAdapter = new UserAdapter(filteredList);
            rcvUser.setAdapter(userAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Fail to set adapter", Toast.LENGTH_SHORT).show();
        }

        searchView.setOnClickListener( v ->{
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterUsersByName(newText);
                return true;
            }
        });

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
            filteredList = new ArrayList<>(lstUser);
            userAdapter = new UserAdapter(filteredList);
            rcvUser.setAdapter(userAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Fail to set adapter", Toast.LENGTH_SHORT).show();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterUsersByName(newText);
                return true;
            }
        });

        searchView.setOnClickListener( v ->{
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        });

    }

    private void filterUsersByName(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(lstUser);
        } else {
            for (User user : lstUser) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    void Mapping() {
        rcvUser = findViewById(R.id.rcvUser);
        rcvUser.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new YogaDatabaseHelper(this);
        searchView = findViewById(R.id.searchUser);
        btnCreateAcc = findViewById(R.id.btnMAddUser);
    }
}