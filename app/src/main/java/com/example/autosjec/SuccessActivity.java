package com.example.autosjec;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        TextView successMessageTextView = findViewById(R.id.success_message);

        String universityNumber = getIntent().getStringExtra("UNIVERSITY_NUMBER");
        String dateOfBirth = getIntent().getStringExtra("DATE_OF_BIRTH");

        String successMessage = "Login Successful\n\nUniversity Number: " + universityNumber + "\nDate of Birth: " + dateOfBirth;
        successMessageTextView.setText(successMessage);
    }
}
