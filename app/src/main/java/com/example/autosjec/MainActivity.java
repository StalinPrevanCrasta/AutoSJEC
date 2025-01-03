package com.example.autosjec;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        TextView registeredUsersTextView = findViewById(R.id.registered_users_textview);
        List<String> registeredUsers = dbHelper.getAllUsers();
        StringBuilder usersText = new StringBuilder();
        for (String user : registeredUsers) {
            usersText.append(user).append("\n");
        }
        registeredUsersTextView.setText(usersText.toString());
        registeredUsersTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SuccessActivity.class);
            startActivity(intent);
        });

        Button registerButton = findViewById(R.id.login_button);
        registerButton.setOnClickListener(v -> {
            EditText universityNumberEditText = findViewById(R.id.university_number);
            EditText dateOfBirthEditText = findViewById(R.id.date_of_birth);

            String universityNumber = universityNumberEditText.getText().toString();
            String dateOfBirth = dateOfBirthEditText.getText().toString();

            if (universityNumber.isEmpty() || dateOfBirth.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInserted = dbHelper.insertData(universityNumber, dateOfBirth);
                if (isInserted) {
                    Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    recreate(); // Refresh the activity to show the new user
                } else {
                    Toast.makeText(MainActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}