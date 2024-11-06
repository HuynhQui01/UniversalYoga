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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateSessionActivity extends AppCompatActivity {

    Spinner spYogaClass;
    Spinner spInstructor;
    EditText edtCSDate;
    EditText edtCSComment;
    Button btnCancel;
    Button btnCreate;
    YogaDatabaseHelper dbHelper;
    int instructorID ;
    int classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_session);

        Mapping();

        List<User> lstInstructor = dbHelper.getInstructors();
        List<String> lstInstructorName = GetInstructorName(lstInstructor);
        List<Yoga> lstYogaClasses = dbHelper.getAllYogaClasses();
        List<String> lstYogaName = GetYogaName(lstYogaClasses);
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, lstYogaName);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYogaClass.setAdapter(classAdapter);

        spYogaClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Yoga selectedYogaClass = lstYogaClasses.get(position);
                classId = lstYogaClasses.get(position).getId();

                edtCSDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showLimitedDayDatePickerDialog(edtCSDate,
                                ChangeDateOfWeek(selectedYogaClass.getDayOfWeek()));
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

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

        btnCreate.setOnClickListener(v -> {
            try {
                dbHelper.addSession(edtCSDate.getText().toString(),instructorID
                        ,edtCSComment.getText().toString(), classId);
                finish();
            }catch (Exception e){
                Toast.makeText(this, "Fail to create new session", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            finish();
        });
    }

    List<String> GetYogaName(List<Yoga> lst) {
        List<String> lstName = new ArrayList<>();

        for (int i = 0; i < lst.size(); i++) {
            lstName.add(lst.get(i).getType());
        }
        return lstName;
    }

    List<String> GetInstructorName(List<User> lst){
        List<String> lstName = new ArrayList<>();

        for (int i = 0; i < lst.size(); i++) {
            lstName.add(lst.get(i).getUsername());
        }
        return lstName;
    }

    // Method to convert day of the week to an integer
    int ChangeDateOfWeek(String dayOfWeek) {
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

    // Method to show a date picker dialog with a limited day selection
    public void showLimitedDayDatePickerDialog(final EditText editText, final int allowedDay) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        DatePickerDialog datePickerDialog = new DatePickerDialog(editText.getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(year, month, dayOfMonth);

                        int dayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);

                        if (dayOfWeek == allowedDay) {
                            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                            editText.setText(selectedDate);
                        } else {
                            Toast.makeText(CreateSessionActivity.this, "Please select a valid date for "
                                    + getDayName(allowedDay), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(today);
        datePickerDialog.show();
    }


    // Helper method to convert integer day to string
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

    // Method to initialize UI components
    void Mapping() {
        spYogaClass = findViewById(R.id.spClass);
        spInstructor = findViewById(R.id.spTeacher);
        edtCSDate = findViewById(R.id.edtCSDate);
        edtCSComment = findViewById(R.id.edtCSComment);
        btnCancel = findViewById(R.id.btnCSCancel);
        btnCreate = findViewById(R.id.btnCSCreate);
        dbHelper = new YogaDatabaseHelper(this);
    }
}
