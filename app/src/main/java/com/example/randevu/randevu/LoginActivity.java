package com.example.randevu.randevu;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button btnLogin;
    private Button btnRegister;
    private String mEmail;
    private String mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editEmail = (EditText) findViewById(R.id.edit_email);
        editPassword = (EditText) findViewById(R.id.edit_password);
        btnLogin = (Button) findViewById(R.id.button_login);
        btnRegister = (Button) findViewById(R.id.button_register);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    changeActivity();
                } else {
                    // User is signed out
                }
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mEmail = editEmail.getText().toString();
                mPassword = editPassword.getText().toString();
                login(mEmail, mPassword);
            }
        });


    }

    public void login(String email, String password) {
        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, go to main app
                            FirebaseUser user = mAuth.getCurrentUser();
                            changeActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, getString(R.string.login_error_message),
                                    Toast.LENGTH_SHORT).show();
                        }
/*
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_error_message),Toast.LENGTH_LONG).show();
                        }
                        // [END_EXCLUDE]*/
                    }
                });
        // [END sign_in_with_email]

    }

    public void changeActivity(){
        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public boolean validateForm() {
        boolean valid = true;
        String email = mEmail;
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Required.");
            valid = false;
        } else {
            editEmail.setError(null);
        }

        String password = mPassword;
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Required.");
            valid = false;
        } else {
            editPassword.setError(null);
        }

        return valid;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
