package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
private Toolbar mainToolbar;
private FirebaseAuth auth;
private BottomNavigationView btmNavView;
private FloatingActionButton addPostButton;
private String current_user_id;
FirebaseFirestore firebaseFirestore;
//Fragments
    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private  AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btmNavView=findViewById(R.id.bottom_navigation_main);
        auth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
     if(auth.getCurrentUser()!= null) {
    //Fragments
    homeFragment = new HomeFragment();
    notificationFragment = new NotificationFragment();
    accountFragment = new AccountFragment();

    mainToolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(mainToolbar);
    getSupportActionBar().setTitle("Photo Blog");

    addPostButton = findViewById(R.id.main_fb);
    addPostButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent postIntent = new Intent(MainActivity.this, NewPostActivity.class);
            startActivity(postIntent);
        }
    });

    replaceFragment(homeFragment);

    btmNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home_btm:

                    replaceFragment(homeFragment);

                    return true;


//                case R.id.notification_btm:
//                replaceFragment(notificationFragment);
//                return true;
//
//                case R.id.account_btm:
//                    replaceFragment(accountFragment);
//                    return true;

                default:
                    return false;
            }

        }
    });

       }

    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            sendToLogin();
     }
      else {
            current_user_id=auth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if (!documentSnapshot.exists()){
                        Intent SetUPintent=new Intent(MainActivity.this,SetupActivity.class);
                        startActivity(SetUPintent);
                        finish();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String Error = e.getMessage();
                    Toast.makeText(MainActivity.this, "Error: "+Error, Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    public void sendToLogin(){
        Intent loginintent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(loginintent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.action_logout_btn:
                logOut();
                return true;


            case  R.id.action_setting_btn:
                Intent SettingsIntent=new Intent(MainActivity.this,SetupActivity.class);
                startActivity(SettingsIntent);
                return true;

                default:
                    return false;
        }




    }

    private void logOut() {
        auth.signOut();
        sendToLogin();
    }


    public void replaceFragment(Fragment fragment){

            FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.main_container,fragment);
            fragmentTransaction.commit();
    }



}
