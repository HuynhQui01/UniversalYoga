package com.example.universalyoga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageSessionActivity extends AppCompatActivity {
    SearchView searchView;
    ImageButton btnAdd;
    RecyclerView recyclerView;
    List<Session> lstSession;
    YogaDatabaseHelper dbHelper;
    ManageSessionAdapter manageSessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_session);

        Mapping();
        lstSession = dbHelper.getAllSession();
        manageSessionAdapter = new ManageSessionAdapter(lstSession);
        recyclerView.setAdapter(manageSessionAdapter);

        btnAdd.setOnClickListener(v -> {
            Intent createSession = new Intent(this, CreateSessionActivity.class);
            startActivity(createSession);
        });
        searchView.setOnClickListener( v ->{
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSession(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSession(newText);
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        lstSession = dbHelper.getAllSession();
        manageSessionAdapter.updateSessionList(lstSession);
    }

    void Mapping() {
        recyclerView = findViewById(R.id.rcvSession);
        btnAdd = findViewById(R.id.btnMAddSession);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new YogaDatabaseHelper(this);
        searchView = findViewById(R.id.searchSession);
    }


    private void filterSession(String query) {
        List<Session> filteredList = new ArrayList<>();
        for (Session session : lstSession) {
            Yoga yogaClass = dbHelper.getYogaClassById(session.getClassId());
            if (yogaClass.getType().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(session);
            }
        }
        manageSessionAdapter.updateSessionList(filteredList);
    }
}
