package com.example.universalyoga;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateClassActivity extends AppCompatActivity {

    Spinner spDayOfWeek;
    EditText edtTime;
    EditText edtCapacity;
    EditText edtDuration;
    EditText edtPrice;
    Spinner spYogaType;
    EditText edtDescription;
    Button btnCreate;
    Button btnCancel;
    YogaDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_class);

        Mapping();

        edtTime.setOnClickListener(v -> showTimePickerDialog());

        btnCreate.setOnClickListener(v -> {
            String dayOfWeek = spDayOfWeek.getSelectedItem().toString();
            String time = edtTime.getText().toString();
            Integer capacity = Integer.parseInt(edtCapacity.getText().toString());
            Integer duration = Integer.parseInt(edtDuration.getText().toString());
            Double price = Double.parseDouble(edtPrice.getText().toString());
            String yogatype = spYogaType.getSelectedItem().toString();
            String description = edtDescription.getText().toString();

            dbHelper.addYogaClass(dayOfWeek, time, capacity, duration, price, yogatype, description);
            Toast.makeText(this, "Create Successfully", Toast.LENGTH_LONG).show();
            finish();
        });

        btnCancel.setOnClickListener(v -> finish());
    }

    private void showTimePickerDialog() {
        int hour = 12;
        int minute = 0;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {

            edtTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    void Mapping(){
        spDayOfWeek = findViewById(R.id.spDayOfWeek);
        edtTime = findViewById(R.id.edtTimeStart);
        edtCapacity = findViewById(R.id.edtCapacity);
        edtDuration = findViewById(R.id.edtDuration);
        edtPrice = findViewById(R.id.edtPrice);
        spYogaType = findViewById(R.id.spYogaType);
        edtDescription = findViewById(R.id.edtDescription);
        btnCreate = findViewById(R.id.btnCLCreate);
        btnCancel = findViewById(R.id.btnCLCancel);
        dbHelper = new YogaDatabaseHelper(this);
    }
}