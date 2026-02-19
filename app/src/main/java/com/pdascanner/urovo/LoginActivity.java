package com.pdascanner.urovo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class LoginActivity extends AppCompatActivity {

    private TextInputLayout layoutUsername;
    private TextInputLayout layoutPassword;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        layoutUsername = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.layoutPassword);
        editUsername   = findViewById(R.id.editUsername);
        editPassword   = findViewById(R.id.editPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String username = editUsername.getText() != null
                ? editUsername.getText().toString().trim() : "";
        String password = editPassword.getText() != null
                ? editPassword.getText().toString().trim() : "";

        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            layoutUsername.setError(getString(R.string.error_username_required));
            valid = false;
        } else {
            layoutUsername.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError(getString(R.string.error_password_required));
            valid = false;
        } else {
            layoutPassword.setError(null);
        }

        if (!valid) return;

        // Dummy auth — any non-empty username + password is accepted
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish(); // Remove login from back stack
    }
}
