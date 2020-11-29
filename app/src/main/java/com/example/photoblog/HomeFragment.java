package com.example.photoblog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private RecyclerView rvPostsHome;
    private List<BlogPost> blog_list;
    private FirebaseFirestore firestore;
    private PostAdapter postAdapter;
    private FirebaseAuth firebaseAuth;
    private Boolean isFirtPageFirstLoad = true;
    private DocumentSnapshot lastVisible;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_home, container, false);
        blog_list =new ArrayList<>();
        rvPostsHome=view.findViewById(R.id.rv_posts_home);
        firebaseAuth=FirebaseAuth.getInstance();

        postAdapter=new PostAdapter(blog_list);
        rvPostsHome.setAdapter(postAdapter);
        rvPostsHome.setLayoutManager(new LinearLayoutManager(getActivity()));
        firebaseAuth=FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {

            firestore = FirebaseFirestore.getInstance();
            rvPostsHome.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachBottom=!recyclerView.canScrollVertically(1);
                    if (reachBottom){
                        String desc=lastVisible.getString("desc");
                        Toast.makeText(container.getContext(), "Reached : " + desc, Toast.LENGTH_SHORT).show();
                        loadMorePost();
                    }

                }
            });

            Query firstQuery=firestore.collection("Posts").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);

            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d(TAG, "Error: " + e.getMessage());
                    } else {
                        if (isFirtPageFirstLoad) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        }

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostId = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                if (isFirtPageFirstLoad) {

                                    blog_list.add(blogPost);
                                } else {
                                    blog_list.add(0, blogPost);
                                }
                                postAdapter.notifyDataSetChanged();
                            }
                        }
                        isFirtPageFirstLoad = false;
                    }

                }
            });
        }
        // Inflate the layout for this fragment
        return view;
    }

    public void loadMorePost(){

            Query nextQuery = firestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d(TAG, "Error:" + e.getMessage());
                    } else {
                        if (!queryDocumentSnapshots.isEmpty()) {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPostId = doc.getDocument().getId();
                                    BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                    blog_list.add(blogPost);
                                    postAdapter.notifyDataSetChanged();
                                }
                            }

                        }
                    }
                }
            });

    }
}















//<LinearLayout
//    android:layout_width="match_parent"
//            android:layout_height="wrap_content"
//            android:orientation="vertical"
//            >
//
//<LinearLayout
//         android:layout_width="wrap_content"
//                 android:layout_height="wrap_content"
//                 android:orientation="horizontal"
//                 >
//<de.hdodenhof.circleimageview.CircleImageView
//        android:layout_width="wrap_content"
//        android:layout_height="wrap_content"
//        android:layout_margin="10dp"
//        android:src="@drawable/circle_image"
//        android:layout_marginEnd="10dp"
//        android:background="@color/colorAccent"
//        />
//<LinearLayout
//             android:layout_width="wrap_content"
//                     android:layout_height="wrap_content"
//                     android:orientation="vertical"
//                     >
//
//<TextView
//                 android:layout_width="wrap_content"
//                         android:layout_height="wrap_content"
//                         android:text="Mahmoud ELramady"
//                         android:layout_marginTop="11dp"
//                         android:layout_marginStart="5dp"
//                         />
//
//<TextView
//                 android:layout_width="wrap_content"
//                         android:layout_height="wrap_content"
//                         android:text="26 Juli, 2020"
//                         android:layout_marginTop="2dp"
//                         android:layout_marginStart="5dp"
//
//                         />
//
//</LinearLayout>
//
//<LinearLayout
//             android:layout_width="match_parent"
//                     android:layout_height="wrap_content"
//
//
//
//
//</LinearLayout>
//
//</LinearLayout>







