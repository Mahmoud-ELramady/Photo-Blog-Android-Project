package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
private TextInputEditText loginEmailText;
private TextInputEditText loginpasswordText;
private MaterialButton loginBtn;
private MaterialButton loginRegBtn;
private ProgressBar loginPb;
FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
         firebaseAuth=FirebaseAuth.getInstance();
        loginEmailText=findViewById(R.id.loginactivity_ed_email);
        loginpasswordText=findViewById(R.id.loginactivity_ed_password);
        loginBtn=findViewById(R.id.loginActivity_btn_login);
        loginRegBtn=findViewById(R.id.login_btn_newaccount);
        loginPb=findViewById(R.id.login_progressbar);


        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent RegIntent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(RegIntent);
                finish();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String EmailText=loginEmailText.getText().toString();
                String PasswordText=loginpasswordText.getText().toString();
                if (!TextUtils.isEmpty(EmailText)&& !TextUtils.isEmpty(PasswordText)){
                    loginPb.setVisibility(View.VISIBLE);
                    firebaseAuth.signInWithEmailAndPassword(EmailText,PasswordText)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                sendToMain();
                            }else{
                                String ErrorMassege=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error:"+ErrorMassege, Toast.LENGTH_SHORT).show();
                            }
                            loginPb.setVisibility(View.INVISIBLE);

                        }
                    });

                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser!=null) {
            sendToMain();
        }

        }

    private void sendToMain() {
            Intent mainIntent=new Intent(LoginActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();
    }
}
