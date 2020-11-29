package com.example.photoblog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1beta1.WriteResult;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
public List<BlogPost> blog_list;
public Context context;
private FirebaseAuth mAuth;
private FirebaseFirestore firestore;
private ImageView blogDeletePost;

public PostAdapter(List<BlogPost>blog_list){
    this.blog_list =blog_list;
}


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_post_items,parent,false);
    context=parent.getContext();
    firestore=FirebaseFirestore.getInstance();
    mAuth=FirebaseAuth.getInstance();
    return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);


    final String blogPostId=blog_list.get(position).BlogPostId;
    final String current_user_id=mAuth.getCurrentUser().getUid();
    final String user_id = blog_list.get(position).getUser_id();

    if (user_id .equals( current_user_id)){
        holder.blogDeletePost.setVisibility(View.VISIBLE);
    }


        String desc_data=blog_list.get(position).getDesc();
    holder.setDescText(desc_data);

    String ImageUrl=blog_list.get(position).getImage_uri();
    String image_thump=blog_list.get(position).getImage_thumb();
    holder.setBlogImage(ImageUrl,image_thump);

        firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String userName=task.getResult().getString("name");
                    String userImage=task.getResult().getString("image");
                    holder.setUserData(userName,userImage);

                }else{

                }
            }
        });

    Long millisecond= blog_list.get(position).getTimestamp().getTime();
    String dateString = DateFormat.format("MM/dd/yyy",new Date(millisecond)).toString();
    holder.setTime(dateString);

        //get like count
        firestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    int count = documentSnapshots.size();
                    holder.updateLikeCount(count);

                } else {
                    holder.updateLikeCount(0);
                }
            }
        });


    // Get Likes

        firestore.collection("Posts/" + blogPostId + "/Likes").document(current_user_id).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    holder.blogLikebtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                    //Log.e("MyTag", "Firebase exception", e);
                } else {
                    holder.blogLikebtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                    //Log.e("MyTag", e.toString());
                }
            }
        });

    // Likes Features
    holder.blogLikebtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            firestore.collection("Posts/" + blogPostId + "/Likes").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (!task.getResult().exists()) {
                        Map<String, Object> likeMap = new HashMap<>();
                        likeMap.put("timeStamp", FieldValue.serverTimestamp());
                        firestore.collection("Posts/" + blogPostId + "/Likes").document(current_user_id).set(likeMap);
                    }else{
                        Map<String, Object> likeMap = new HashMap<>();
                        likeMap.put("timeStamp", FieldValue.serverTimestamp());
                        firestore.collection("Posts/" + blogPostId + "/Likes").document(current_user_id).delete();
                    }
                    }
            });


        }
    });

//    // delete Post
//        firestore.collection("Posts/").document(current_user_id+user_id).addSnapshotListener( new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//
//               if (documentSnapshot.exists()) {
//                   if (user_id.equals(current_user_id)) {
//                       blogDeletePost.setVisibility(View.VISIBLE);
//                       Toast.makeText(context, "hhhhh", Toast.LENGTH_SHORT).show();
//
//                   }
//               }
//            }
//        });

    holder.blogDeletePost.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            AlertDialog alertDialog = new AlertDialog.Builder(context)
//set icon
                    .setIcon(android.R.drawable.ic_dialog_alert)
//set title
                    .setTitle("Confirm Delete...")
//set message
                    .setMessage("Are you sure to delete this Post?")
//set positive button
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what would happen when positive button is clicked
                            firestore.collection("Posts").document(blogPostId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        blog_list.remove(position);
                                        Toast.makeText(context, "post was deleted", Toast.LENGTH_SHORT).show();

                                        notifyDataSetChanged();
                                    }else {
                                        Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    })
//set negative button
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //set what should happen when negative button is clicked
                            Toast.makeText(context, "Nothing Happened", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();



        }
    });


    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

     private TextView descView;
     private ImageView blogImageView;
     private TextView blogTimeView;
     private TextView blogUserName;
     private CircleImageView blogUserImage;
     private ImageView blogLikebtn;
     private TextView  blogLikeCounter;
     private View mView;
     private ImageView blogDeletePost;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;
            blogDeletePost=mView.findViewById(R.id.blog_post_delete);
            blogLikebtn=mView.findViewById(R.id.blog_like_button);
        }
         public void setDescText (String descText ){
            descView=mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
         }

         public  void setBlogImage( String downloaduri ,String imageThumb){
            blogImageView =mView.findViewById(R.id.image_post_item);
          //Picasso.get().load(downloaduri).into(blogImageView);

             RequestOptions placeHolder=new RequestOptions();
             placeHolder.placeholder(R.drawable.new_post);

              Glide.with(context).applyDefaultRequestOptions(placeHolder).load(downloaduri).thumbnail(
                      Glide.with(context)  .load(imageThumb))

             .into(blogImageView);
         }

         public void setTime(String Time){
            blogTimeView =mView.findViewById(R.id.blog_post_date);
            blogTimeView.setText(Time);
         }
         public void setUserData(String userName , String UserImage){
            blogUserName=mView.findViewById(R.id.blog_user_name);
            blogUserImage=mView.findViewById(R.id.blog_user_image);

            blogUserName.setText(userName);

             RequestOptions placeHolderOption=new RequestOptions();
             placeHolderOption.placeholder(R.drawable.ic_default_profile2);

             Glide.with(context).applyDefaultRequestOptions(placeHolderOption).load(UserImage).into(blogUserImage);
         }

        public void updateLikeCount(int count) {
            blogLikeCounter = mView.findViewById(R.id.blog_like_count);
            blogLikeCounter.setText(count + " Likes");
        }





    }

}
