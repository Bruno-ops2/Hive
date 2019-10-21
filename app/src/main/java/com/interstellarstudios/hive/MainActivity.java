package com.interstellarstudios.hive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.interstellarstudios.hive.firestore.GetData;
import com.interstellarstudios.hive.fragments.ChatsFragment;
import com.interstellarstudios.hive.fragments.ProfileFragment;
import com.interstellarstudios.hive.fragments.UsersFragment;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.repository.Repository;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private Context context = this;
    private Repository repository;
    private Window window;
    private View container;
    private TextView textViewFragmentTitle;
    private ImageView imageViewProfilePic;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.navigation_chats:
                            selectedFragment = new ChatsFragment();
                            textViewFragmentTitle.setText("Chats");
                            break;
                        case R.id.navigation_users:
                            selectedFragment = new UsersFragment();
                            textViewFragmentTitle.setText("Users");
                            break;
                        case R.id.navigation_profile:
                            selectedFragment = new ProfileFragment();
                            textViewFragmentTitle.setText("Profile");
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame, selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new Repository(getApplication());

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        imageViewProfilePic = findViewById(R.id.image_view_profile_pic);

        window = this.getWindow();
        container = findViewById(R.id.container);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewFragmentTitle = findViewById(R.id.text_view_fragment_title);
        textViewFragmentTitle.setText("Chats");

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_frame,
                    new ChatsFragment()).commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        bottomNav.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);

        profilePicOperations();
    }

    private void profilePicOperations() {

        DocumentReference currentUserRef = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    User user = snapshot.toObject(User.class);
                    if (user.getProfilePicUrl() != null) {
                        Picasso.get().load(user.getProfilePicUrl()).into(imageViewProfilePic);
                        GetData.currentUser(mFireBaseFireStore, mCurrentUserId, repository);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        chatPath.update("onlineOffline", "online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        chatPath.update("onlineOffline", "offline");
    }
}
