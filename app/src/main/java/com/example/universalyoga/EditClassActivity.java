package com.example.universalyoga;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class EditClassActivity extends AppCompatActivity {
    EditText edtTime, edtDuration, edtDes, edtCapacity, edtPrice;
    Button btnCancel, btnSave;
    Spinner spDayOfWeek, spType;
    YogaDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_class);

        Mapping();
        int classId = getIntent().getIntExtra("ClassId", -1);
        Yoga currentYogaClass = dbHelper.getYogaClassById(classId);

        if (currentYogaClass != null) {
            edtTime.setText(currentYogaClass.getTime());
            edtDuration.setText(String.valueOf(currentYogaClass.getDuration()));
            edtDes.setText(currentYogaClass.getDescription());
            edtCapacity.setText(String.valueOf(currentYogaClass.getCapacity()));
            edtPrice.setText(String.valueOf(currentYogaClass.getPrice()));
            spType.setSelection(getIndexOfSpinner(spType, currentYogaClass.getType()));
            spDayOfWeek.setSelection(getIndexOfSpinner(spDayOfWeek, currentYogaClass.getDayOfWeek()));
        }

        boolean hasSessionRef = dbHelper.hasClassInstanceForYogaClass(classId);

        if (hasSessionRef) {

            edtTime.setEnabled(true);
            edtDuration.setEnabled(true);
            edtDes.setEnabled(true);

            edtCapacity.setEnabled(false);
            edtPrice.setEnabled(false);
            spDayOfWeek.setEnabled(false);
            spType.setEnabled(false);
        } else {

            edtTime.setEnabled(true);
            edtDuration.setEnabled(true);
            edtDes.setEnabled(true);
            edtCapacity.setEnabled(true);
            edtPrice.setEnabled(true);
            spDayOfWeek.setEnabled(true);
            spType.setEnabled(true);
        }

        edtTime.setOnClickListener(v -> showTimePickerDialog());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> {
            String time = edtTime.getText().toString();
            String durationStr = edtDuration.getText().toString();

            if (!time.isEmpty() && !durationStr.isEmpty()) {
                int duration = Integer.parseInt(durationStr);

                if (hasSessionRef) {

                    dbHelper.updateClassByIdWithIncludingSession(classId, time, duration, edtDes.getText().toString());
                } else {
                    dbHelper.updateClassByIdWithNoSession(classId, time, duration,
                            Double.parseDouble(edtPrice.getText().toString()),
                            spType.getSelectedItem().toString(),
                            spDayOfWeek.getSelectedItem().toString(),
                            edtDes.getText().toString(),
                            Integer.parseInt(edtCapacity.getText().toString()));
                }
                finish();
            } else {
                Toast.makeText(this, "Time or duration must not be null", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    String selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    edtTime.setText(selectedTime);
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private int getIndexOfSpinner(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }

    void Mapping(){
        edtTime = findViewById(R.id.edtECTimeStart);
        edtDuration = findViewById(R.id.edtECDuration);
        edtDes = findViewById(R.id.edtECDescription);
        edtCapacity = findViewById(R.id.edtECCapacity);
        edtPrice = findViewById(R.id.edtECPrice);
        btnCancel = findViewById(R.id.btnECCancel);
        spType = findViewById(R.id.spECYogaType);
        spDayOfWeek = findViewById(R.id.spECDayOfWeek);
        btnSave = findViewById(R.id.btnECSave);
        dbHelper = new YogaDatabaseHelper(this);
    }
}