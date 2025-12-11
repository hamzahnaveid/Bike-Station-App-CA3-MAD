package com.example.bikestationapp_ca3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bikestationapp_ca3.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    FirebaseAuth auth;
    EditText etEmail, etPassword, etName;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        auth = FirebaseAuth.getInstance();
        sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        etEmail = findViewById(R.id.et_registeremail);
        etPassword = findViewById(R.id.et_registerpassword);
        etName = findViewById(R.id.et_name);
    }

    public void register(View view) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String name = etName.getText().toString();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this,
                    "Please fill in all of the required fields",
                    Toast.LENGTH_SHORT
            ).show();
            etEmail.setText("");
            etPassword.setText("");
            etName.setText("");
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this,
                    "Password must be at least 6 characters long",
                    Toast.LENGTH_SHORT
            ).show();
        }

        User user = new User(email, password, name);
        String userKey = addUserToDb(user);

        SharedPreferences.Editor editor = sp.edit();

        editor.putString("USER", userKey);
        editor.commit();

        Intent intent = new Intent(RegisterActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public String addUserToDb(User user) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("users");

        String key = ref.push().getKey();

        ref.child(key).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                addUserToAuth(user.getEmail(), user.getPassword());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this,
                        e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                e.printStackTrace();
            }
        });
        return key;
    }

    public void addUserToAuth(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                auth.signInWithEmailAndPassword(email, password);
                Toast.makeText(RegisterActivity.this,
                        "User successfully registered",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}