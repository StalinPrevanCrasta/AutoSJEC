package com.example.autosjec;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

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

        LinearLayout registeredUsersContainer = findViewById(R.id.registered_users_container);
        List<String> registeredUsers = dbHelper.getAllUsers();
        for (String user : registeredUsers) {
            LinearLayout userLayout = new LinearLayout(this);
            userLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView userTextView = new TextView(this);
            userTextView.setText("USN: " + user);
            userTextView.setTextColor(Color.WHITE);
            userTextView.setTextSize(22);
            userTextView.setPadding(16, 16, 16, 16);
            userTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String dateOfBirth = dbHelper.getDateOfBirth(user);
                    Intent intent = new Intent(MainActivity.this, SuccessActivity.class);
                    intent.putExtra("UNIVERSITY_NUMBER", user);
                    intent.putExtra("DATE_OF_BIRTH", dateOfBirth);
                    startActivity(intent);
                }
            });

            Button deleteButton = new Button(this);
            deleteButton.setText("Delete");
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dbHelper.deleteUser(user);
                    recreate();
                }
            });

            userLayout.addView(userTextView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            userLayout.addView(deleteButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            registeredUsersContainer.addView(userLayout);
        }

        Button registerButton = findViewById(R.id.login_button);
        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                EditText universityNumberEditText = findViewById(R.id.university_number);
                EditText dateOfBirthEditText = findViewById(R.id.date_of_birth);
                String universityNumber = universityNumberEditText.getText().toString();
                String dateOfBirth = dateOfBirthEditText.getText().toString();
                if (universityNumber.isEmpty() || dateOfBirth.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isInserted = dbHelper.insertData(universityNumber, dateOfBirth);
                    recreate();
                    if (isInserted) {
                        Toast.makeText(MainActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                        universityNumberEditText.setText("");
                        dateOfBirthEditText.setText("");
                    } else {
                        Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}