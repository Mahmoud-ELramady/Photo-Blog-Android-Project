package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

   public class SetupActivity extends AppCompatActivity {
        private Toolbar setupToolbar;
       private CircleImageView ImageSetUp;
       private ProgressBar SetupPb;
       private boolean isChanged=false;
       private StorageReference image_path;
       private Uri ImagemainUri = null;
       private String user_id;
       private TextInputEditText setUpName;
       private MaterialButton setUpButton;
       private StorageReference storageReference;
       private FirebaseAuth auth;
       private FirebaseFirestore firestore;

       @Override
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.activity_setup);


           setupToolbar = findViewById(R.id.setup_toolbar);
           setSupportActionBar(setupToolbar);
           getSupportActionBar().setTitle("Account Setting");
           ImageSetUp = findViewById(R.id.circle_image_setup);
           setUpName = findViewById(R.id.setup_ed_name);
           setUpButton = findViewById(R.id.setup_btn_save);
           SetupPb = findViewById(R.id.setup_pb);


           auth = FirebaseAuth.getInstance();
           storageReference = FirebaseStorage.getInstance().getReference();
           firestore = FirebaseFirestore.getInstance();
           user_id = auth.getCurrentUser().getUid();
            SetupPb.setVisibility(View.VISIBLE);
            setUpButton.setEnabled(false);

           firestore.collection("Users").document(user_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
               @Override
               public void onSuccess(DocumentSnapshot documentSnapshot) {
                   if(documentSnapshot.exists()){
                       String name=documentSnapshot.getString("name");
                       String image=documentSnapshot.getString("image");
                       ImagemainUri=Uri.parse(image);
                       setUpName.setText(name);

                       RequestOptions requestOptions =new RequestOptions();
                       requestOptions.placeholder(R.drawable.ic_default_profile2);
                       Glide.with(SetupActivity.this).setDefaultRequestOptions(requestOptions).load(image).into(ImageSetUp);
                   }
               }
           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   String Error = e.getMessage();
                   Toast.makeText(SetupActivity.this, "Error: "+Error, Toast.LENGTH_SHORT).show();

               }
           });
           SetupPb.setVisibility(View.INVISIBLE);
           setUpButton.setEnabled(true);



           setUpButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   final String user_name = setUpName.getText().toString();
                   if (!TextUtils.isEmpty(user_name) && ImagemainUri != null) {
                       SetupPb.setVisibility(View.VISIBLE);

                   if (isChanged) {

                           user_id = auth.getCurrentUser().getUid();
                           image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                           image_path.putFile(ImagemainUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                               @Override
                               public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                                   storeFireStore(user_name);
                               }

                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   String error = e.getMessage();
                                   Toast.makeText(SetupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                   SetupPb.setVisibility(View.INVISIBLE);

                               }
                           });

                       } else{
                       storeFireStore(user_name);
                   }


                   }
               }
           });



           ImageSetUp.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                       if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                           ActivityCompat.requestPermissions(SetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                           Toast.makeText(SetupActivity.this, "Permission is denied", Toast.LENGTH_SHORT).show();

                       } else {
                           BringImagePicker();
                       }

                   } else {
                       BringImagePicker();
                   }


               }
           });


       }

       private void storeFireStore(final String user_name) {
           image_path = storageReference.child("profile_images").child(user_id + ".jpg");
           image_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

               @Override
               public void onSuccess(Uri uri) {

                   final Uri downloadurl = uri;
                   Map<String,String> userMap=new HashMap<>();
                   userMap.put("name",user_name);
                   userMap.put("image",downloadurl.toString());
                   firestore.collection("Users").document(user_id).set(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           Toast.makeText(SetupActivity.this, "The user setting are updated", Toast.LENGTH_SHORT).show();
                           Intent mainIntenet=new Intent(SetupActivity.this,MainActivity.class);
                           startActivity(mainIntenet);
                           finish();
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                           String Error = e.getMessage();
                           Toast.makeText(SetupActivity.this, "Error: "+Error, Toast.LENGTH_SHORT).show();

                       }
                   });
                   SetupPb.setVisibility(View.INVISIBLE);

               }
           });
       }

       public void BringImagePicker() {
           CropImage.activity()
                   .setGuidelines(CropImageView.Guidelines.ON)
                   .setAspectRatio(1, 1)
                   .start(SetupActivity.this);
       }




       @Override
       protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
           super.onActivityResult(requestCode, resultCode, data);

           if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

               CropImage.ActivityResult result = CropImage.getActivityResult(data);

               if (resultCode == RESULT_OK) {

                   ImagemainUri = result.getUri();
                   ImageSetUp.setImageURI(ImagemainUri);
                   isChanged=true;

               } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                   Exception error = result.getError();
               }
           }


       }

//    public void uploadImage() {
//        String name = setUpName.getText().toString();
//
//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setMessage("Uploading");
//        pd.show();
//        if (ImagemainUri != null && TextUtils.isEmpty(name)) {
//            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_image").child(System.currentTimeMillis() + ".jpg");
//            storageReference.putFile(ImagemainUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            String url = uri.toString();
//                            Log.d("dowloaded", url);
//                            pd.dismiss();
//                            Toast.makeText(SetupActivity.this, "Image done", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            });
//        } else {
//
//            Toast.makeText(SetupActivity.this, "Error", Toast.LENGTH_SHORT).show();
//        }
//    }

   }