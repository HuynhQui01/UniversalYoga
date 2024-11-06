package com.example.universalyoga;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditSessionActivity extends AppCompatActivity {
    Spinner spInstructor;
    Button btnCancel, btnSave;
    EditText edtDate, edtComment;
    YogaDatabaseHelper dbHelper;
    int instructorID;
    int sessionId;
    Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_session);
        Mapping();

        sessionId = getIntent().getIntExtra("SessionId", -1);
        dbHelper = new YogaDatabaseHelper(this);
        session = dbHelper.getSessionById(sessionId);

        if (session != null) {
            edtDate.setText(session.getDate());
            edtComment.setText(session.getComment());

            Yoga yoga = dbHelper.getYogaClassById(session.getClassId());
            if (yoga != null) {
                setupDateClickListener(yoga.getDayOfWeek());
            }
        } else {
            Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<User> lstInstructor = dbHelper.getInstructors();
        List<String> lstInstructorName = GetInstructorName(lstInstructor);

        ArrayAdapter<String> instructorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, lstInstructorName);
        instructorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInstructor.setAdapter(instructorAdapter);

        spInstructor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                User user = lstInstructor.get(i);
                instructorID = user.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveSession());
    }

    private void setupDateClickListener(String dayOfWeek) {
        edtDate.setOnClickListener(v -> showLimitedDayDatePickerDialog(edtDate, ChangeDateOfWeek(dayOfWeek)));
    }

    private void showLimitedDayDatePickerDialog(final EditText editText, final int allowedDay) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(editText.getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year1, month1, dayOfMonth);

                    int dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);

                    if (dayOfWeek == allowedDay) {
                        String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                        editText.setText(selectedDate);
                    } else {
                        Toast.makeText(EditSessionActivity.this, "Please select a valid date for "
                                + getDayName(allowedDay), Toast.LENGTH_SHORT).show();
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveSession() {
        if (session != null) {
            if (dbHelper.updateSession(session, instructorID, edtDate.getText().toString(), edtComment.getText().toString() )) {
                Toast.makeText(this, "Update Session successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Fail to update Session", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
            default:
                return "";
        }
    }

    private int ChangeDateOfWeek(String dayOfWeek) {
        switch (dayOfWeek) {
            case "Monday":
                return Calendar.MONDAY;
            case "Tuesday":
                return Calendar.TUESDAY;
            case "Wednesday":
                return Calendar.WEDNESDAY;
            case "Thursday":
                return Calendar.THURSDAY;
            case "Friday":
                return Calendar.FRIDAY;
            case "Saturday":
                return Calendar.SATURDAY;
            case "Sunday":
                return Calendar.SUNDAY;
            default:
                return 0;
        }
    }

    private List<String> GetInstructorName(List<User> lst) {
        List<String> lstName = new ArrayList<>();
        for (User user : lst) {
            lstName.add(user.getUsername());
        }
        return lstName;
    }

    private void Mapping() {
        spInstructor = findViewById(R.id.spTeacher);
        btnCancel = findViewById(R.id.btnCSCancel);
        btnSave = findViewById(R.id.btnCSCreate);
        edtDate = findViewById(R.id.edtCSDate);
        edtComment = findViewById(R.id.edtCSComment);
        dbHelper = new YogaDatabaseHelper(this);
    }
}
