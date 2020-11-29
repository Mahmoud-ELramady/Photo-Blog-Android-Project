package com.example.photoblog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.compiler.PluginProtos;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    private TextInputEditText EdDesc;
    private MaterialButton PostButton;
    private ImageView PostImage;
    private Uri PostImageUri=null;
    private ProgressDialog dialog;
     private String current_user_id;
    FirebaseAuth firebaseAuth;
    private Toolbar PostToolbar;
    private ProgressBar PostPb ;
    StorageReference storageReference;
    FirebaseFirestore firestore;
    Bitmap compressedImageBitmap;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        dialog=new ProgressDialog(getApplicationContext());

        storageReference= FirebaseStorage.getInstance().getReference();

        firestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        current_user_id=firebaseAuth.getCurrentUser().getUid();

        PostToolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(PostToolbar);
        getSupportActionBar().setTitle("Add new Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EdDesc=findViewById(R.id.post_ed_desc);
        PostButton=findViewById(R.id.post_btn_postblog);
        PostImage=findViewById(R.id.image_post);
        PostPb=findViewById(R.id.post_pb);

        PostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1, 1)
                        .start(NewPostActivity.this);
            }
        });

        PostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String desc=EdDesc.getText().toString();
                if (!TextUtils.isEmpty(desc) && PostImageUri!=null){
                    PostPb.setVisibility(View.VISIBLE);
                    final String randomName= UUID.randomUUID().toString();
                    final StorageReference file_path=storageReference.child("Posts Images").child(randomName+".jpg");
                    file_path.putFile(PostImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            file_path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                              @Override
                              public void onSuccess(Uri uri) {
                                  final Uri downloadUri=uri;

                                  File newImageFile=new File(PostImageUri.getPath());
                                  try {
                                      compressedImageBitmap=new Compressor(NewPostActivity.this)
                                              .setMaxHeight(100)
                                              .setMaxWidth(100)
                                              .setQuality(2)
                                      .compressToBitmap(newImageFile);

                                  }catch (IOException e) {
                                           e.printStackTrace();
                                          }
                                  ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();
                                  compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG,100,outPutStream);
                                  byte thumData[] = outPutStream.toByteArray();
                                  UploadTask uploadTask=storageReference.child("post_images/thumbs").child(randomName+".jpg").putBytes(thumData);
                                  uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                      @Override
                                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                          taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                              @Override
                                              public void onSuccess(Uri uri) {
                                                  Uri downloadUriThumb=uri;
                                                  Map<String,Object> postMap=new HashMap<>();

                                                  postMap.put("image_uri",downloadUri.toString());
                                                  postMap.put("image_thumb",downloadUriThumb.toString());
                                                  postMap.put("desc",desc);
                                                  postMap.put("user_id",current_user_id);
                                                  postMap.put("timestamp",FieldValue.serverTimestamp());
                                                  firestore.collection("Posts").add(postMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                      @Override
                                                      public void onSuccess(DocumentReference documentReference) {
                                                          Toast.makeText(NewPostActivity.this, "post was added", Toast.LENGTH_SHORT).show();
                                                          Intent mainIntent = new Intent(NewPostActivity.this,MainActivity.class);
                                                          startActivity(mainIntent);
                                                          finish();
                                                      }
                                                  }).addOnFailureListener(new OnFailureListener() {
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {

                                                      }
                                                  });
                                                  PostPb.setVisibility(View.INVISIBLE);
                                              }
                                          });
                                                                            }
                                  }).addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {

                                      }
                                  });





                              }
                          });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                                   PostPb.setVisibility(View.INVISIBLE);
                        }
                    });


                }

            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                PostImageUri = result.getUri();
                PostImage.setImageURI(PostImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }


    }








}

//try {
//        compressedImageBitmap=new Compressor(NewPostActivity.this)
//        .setMaxWidth(20)
//        .setMaxHeight(20)
//        .setQuality(2)
//        .compressToBitmap(newImageFile);
//
//        }catch (IOException e) {
//        e.printStackTrace();
//        }
//        byte thumData[]=null;
//        ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();
//        compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG,10,outPutStream);
//        thumData = outPutStream.toByteArray();
//
//        UploadTask uploadTask2= storageReference.child("posts_images/thumbs").child(randomName+".jpg").putBytes(thumData);
