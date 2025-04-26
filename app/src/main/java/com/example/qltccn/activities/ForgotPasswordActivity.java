package com.example.qltccn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qltccn.R;
import com.example.qltccn.utils.AuthUtils;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnNextStep, btnSignUp;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        initViews();
        
        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        edtEmail = findViewById(R.id.edtEmail);
        btnNextStep = findViewById(R.id.btnNextStep);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setClickListeners() {
        btnNextStep.setOnClickListener(v -> {
            if (validateEmail()) {
                resetPassword();
            }
        });

        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateEmail() {
        String email = edtEmail.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Please enter your email");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Please enter a valid email");
            return false;
        }

        return true;
    }

    private void resetPassword() {
        String email = edtEmail.getText().toString().trim();

        // Show progress
        btnNextStep.setEnabled(false);
        
        // Send password reset email
        AuthUtils.resetPassword(this, email, task -> {
            btnNextStep.setEnabled(true);
            
            if (task.isSuccessful()) {
                Toast.makeText(ForgotPasswordActivity.this, 
                        "Password reset email sent to " + email, 
                        Toast.LENGTH_LONG).show();
                
                // Navigate back to login screen
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : "Failed to send reset email";
                Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}