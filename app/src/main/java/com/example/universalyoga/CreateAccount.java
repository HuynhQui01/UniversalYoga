package com.example.universalyoga;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

public class CreateAccount extends AppCompatActivity {

    EditText edtEmail;
    EditText edtPass;
    EditText edtUserName;
    EditText edtPhone;
    Spinner spRole;
    Button btnCreate;
    Button btnCancel;

    YogaDatabaseHelper dbHelper;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.creat_account);

        Mapping();

        // Khởi tạo database helper
        dbHelper = new YogaDatabaseHelper(this);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString();
                String password = edtPass.getText().toString();
                String phone = edtPhone.getText().toString();
                String userName = edtUserName.getText().toString();
                String role = spRole.getSelectedItem().toString();

                if (validateForm(email, password)) {
                    registerUser(userName, email, password, role, phone);

                    finish();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }


    private void registerUser(String name, String email, String password, String role, String phone) {

        if (dbHelper.getUsernameByEmail(email) != null) {
            Toast.makeText(CreateAccount.this, "Email exists!", Toast.LENGTH_SHORT).show();
            return;
        }
        HashPass hash = new HashPass();
        String hashedpass = hash.hashPassword(password);
        dbHelper.addUser(name, email, hashedpass, role, phone);
        Toast.makeText(CreateAccount.this, "Create account successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }




    private boolean validateForm(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Invalid email!");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            edtPass.setError("Password must be more than 6");
            return false;
        }

        return true;
    }

    void Mapping() {
        edtEmail = findViewById(R.id.edtCAEmail);
        edtPass = findViewById(R.id.edtCAPass);
        btnCreate = findViewById(R.id.btnCACreate);
        btnCancel = findViewById(R.id.btnCACancel);
        database = FirebaseDatabase.getInstance();
        edtUserName = findViewById(R.id.edtCAUserName);
        edtPhone = findViewById(R.id.edtCAPhone);
        spRole = findViewById(R.id.spinnerRole);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, R.layout.textview_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRole.setAdapter(adapter);
    }
}
