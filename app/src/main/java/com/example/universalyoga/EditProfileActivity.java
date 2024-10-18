package com.example.universalyoga;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditProfileActivity extends AppCompatActivity {

    EditText edtUserName, edtPhone;
    Button btnCancel, btnSave;
    YogaDatabaseHelper dbHelper;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        String email = getIntent().getStringExtra("userEmail");

        edtUserName = findViewById(R.id.edtEditName);
        edtPhone = findViewById(R.id.edtEditPhone);
        btnSave = findViewById(R.id.btnEditSave);
        btnCancel = findViewById(R.id.btnEditCancel);
        dbHelper = new YogaDatabaseHelper(this);

        user = dbHelper.getUserByEmail(email);
        edtUserName.setText(user.getUsername());
        edtPhone.setText(user.getPhone());

        btnSave.setOnClickListener( v -> {
            try {
                dbHelper.UpdateUser(user, edtPhone.getText().toString(), edtUserName.getText().toString());
                Toast.makeText(this, "Success to update profile", Toast.LENGTH_SHORT).show();
                finish();
            }catch (Exception e){
                Toast.makeText(this, "fail to update profile", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            finish();
        });


    }
}