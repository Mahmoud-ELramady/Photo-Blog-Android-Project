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

public class RegisterActivity extends AppCompatActivity {


    private TextInputEditText email_reg;
    private TextInputEditText pass_reg;
    private TextInputEditText pass_confirm_reg;
    private MaterialButton signup_reg;
    private MaterialButton haveAccount_reg;
    private ProgressBar pr_reg;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email_reg=findViewById(R.id.registeractivity_ed_email);
        pass_reg=findViewById(R.id.registeractivity_ed_password);
        pass_confirm_reg=findViewById(R.id.registeractivity_ed_confirm_password);
        signup_reg=findViewById(R.id.registeractivity_btn_signup);
        haveAccount_reg=findViewById(R.id.registeractivity_btn_have_account);
        pr_reg=findViewById(R.id.registeractivity_progressbar);

        firebaseAuth=FirebaseAuth.getInstance();


        haveAccount_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LoginIntent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(LoginIntent);
                finish();
            }
        });


        signup_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email_SignUp=email_reg.getText().toString();
                String Password_SignUp=pass_reg.getText().toString();
                String PasswordConfirm_SignUp=pass_confirm_reg.getText().toString();

                if (!TextUtils.isEmpty(Email_SignUp) && !TextUtils.isEmpty(Password_SignUp)
                        && !TextUtils.isEmpty(PasswordConfirm_SignUp)){

                    if (Password_SignUp.equals(PasswordConfirm_SignUp)){
                        pr_reg.setVisibility(View.VISIBLE);
                        firebaseAuth.createUserWithEmailAndPassword(Email_SignUp,Password_SignUp)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){

                                    Intent setupIntent=
                                    new Intent(RegisterActivity.this,SetupActivity.class);
                                    startActivity(setupIntent);
                                    finish();

                                }else {
                                    String Error=task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error"+Error, Toast.LENGTH_SHORT).show();
                                }
                                pr_reg.setVisibility(View.INVISIBLE);

                            }
                        });


                    }else {
                        Toast.makeText(RegisterActivity.this, "Confirm Password and Password are not equal",
                                Toast.LENGTH_SHORT).show();
                    }

                }



            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if (currentUser!=null){

            sendToMain();
        }



    }

    private void sendToMain() {
        Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
