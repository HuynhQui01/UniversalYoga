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

                if (validateForm(email, password)) {
                    registerUser(email, password);
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

    // Thay vì dùng Firebase Authentication, sử dụng SQLite
    private void registerUser(String email, String password) {
        // Kiểm tra xem email đã tồn tại trong SQLite chưa
        if (dbHelper.getUsernameByEmail(email) != null) {
            Toast.makeText(CreateAccount.this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu người dùng mới vào SQLite
        dbHelper.addUser("user", email, password, "customer", null);

        // Sau khi lưu vào SQLite, đẩy lên Firebase Realtime Database
        saveUserToFirebase(email, password);
    }

    // Hàm lưu người dùng lên Firebase Realtime Database
    private void saveUserToFirebase(String email, String password) {
        User newUser = new User("user", email, password, "customer", null);

        FirebaseDatabase.getInstance().getReference("users")
                .push()
                .setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateAccount.this, "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(CreateAccount.this, "Tạo tài khoản thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm(String email, String password) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Vui lòng nhập email hợp lệ");
            return false;
        }

        if (password.isEmpty() || password.length() < 6) {
            edtPass.setError("Mật khẩu phải có ít nhất 6 ký tự");
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
