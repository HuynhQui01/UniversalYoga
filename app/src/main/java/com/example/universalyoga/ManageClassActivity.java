package com.example.universalyoga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageClassActivity extends AppCompatActivity {

    RecyclerView rcvClass;
    SearchView searchView;
    ImageButton btnAdd;
    YogaDatabaseHelper dbHelper;
    List<Yoga> lstYoga, filteredList;
    ClassAdapter classAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_class);

        Mapping();

        try {
            lstYoga = dbHelper.getAllYogaClasses();
            filteredList = new ArrayList<>(lstYoga);
            classAdapter = new ClassAdapter(lstYoga);
            rcvClass.setAdapter(classAdapter);
        }catch (Exception e){
            Toast.makeText(this, "Fail to get classes", Toast.LENGTH_SHORT).show();
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

                filterClassByType(newText);
                return true;
            }
        });

        btnAdd.setOnClickListener(v->{
            Intent create = new Intent(this, CreateClassActivity.class);
            startActivity(create);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        lstYoga.clear();
        lstYoga.addAll(dbHelper.getAllYogaClasses());
        filteredList = new ArrayList<>(lstYoga);
        classAdapter.notifyDataSetChanged();
    }

    private void filterClassByType(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(lstYoga);
        } else {
            for (Yoga y : lstYoga) {
                if (y.getType().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(y);
                }
            }
        }
        classAdapter.notifyDataSetChanged();
    }

    void Mapping(){
        rcvClass = findViewById(R.id.rcvClass);
        rcvClass.setLayoutManager(new LinearLayoutManager(this));
        dbHelper = new YogaDatabaseHelper(this);
        searchView = findViewById(R.id.searchClass);
        btnAdd = findViewById(R.id.btnMAddClass);
    }
}