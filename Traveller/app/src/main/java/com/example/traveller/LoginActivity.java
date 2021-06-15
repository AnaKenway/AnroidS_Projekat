package com.example.traveller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    private static final String FIREBASE_CHILD ="traveller-users";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        LinearLayout loginLayout=findViewById(R.id.lLogin);
        LinearLayout registerLayout=findViewById(R.id.lRegister);

        Intent i= getIntent();
        boolean isLogin=i.getBooleanExtra("isLogin",true);

        if(isLogin){
            loginLayout.setVisibility(View.VISIBLE);
            registerLayout.setVisibility(View.GONE);

            Button loginButton=(Button)findViewById(R.id.cirLoginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final EditText usernameEditText = findViewById(R.id.editTextLoginUsername);
                    final EditText passwordEditText = findViewById(R.id.editTextPassword);

                    //logika
                    //myRef.child("traveller-user").

                    //zameniti usernameEditText sa email
                    signIn(usernameEditText.getText().toString(),passwordEditText.getText().toString());

                }
            });

        }

        else{
            loginLayout.setVisibility(View.GONE);
            registerLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        }
    }



    private void reload() { }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Successfully logged in",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            // Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }
}