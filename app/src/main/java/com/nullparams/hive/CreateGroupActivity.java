package com.nullparams.hive;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nullparams.hive.adapters.UserAdapter;
import com.nullparams.hive.models.Group;
import com.nullparams.hive.models.GroupParticipant;
import com.nullparams.hive.models.User;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CreateGroupActivity extends AppCompatActivity {

    private Context context = this;
    private RecyclerView recyclerView;
    private List<User> mUsers = new ArrayList<>();
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private EditText editTextGroupName;
    private ArrayList<String> groupUsersList = new ArrayList<>();
    private String uniqueId;
    private SharedPreferences sharedPreferences;
    private Window window;
    private View container;
    private Toolbar toolbar;
    private TextView textViewTitle;
    private ImageView imageViewBack;
    private ImageView imageViewCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            uniqueId = bundle.getString("uniqueId");
        }

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        textViewTitle = findViewById(R.id.text_view_username);

        editTextGroupName = findViewById(R.id.edit_text_group_name);

        imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageViewCheck = findViewById(R.id.image_view_check);
        imageViewCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup();
            }
        });

        window = this.getWindow();
        container = findViewById(R.id.container);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        readUsers();
    }

    private void lightMode() {

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewCheck, ContextCompat.getColorStateList(context, R.color.PrimaryDark));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        editTextGroupName.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextGroupName.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
    }

    private void darkMode() {

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewCheck, ContextCompat.getColorStateList(context, R.color.PrimaryLight));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        editTextGroupName.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextGroupName.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    private void createGroup() {

        String groupName = editTextGroupName.getText().toString().trim();

        if (TextUtils.isEmpty(groupName)) {
            Toasty.info(context, "Please enter a group name", Toast.LENGTH_LONG, true).show();
            return;
        }

        groupUsersList.add(mCurrentUserId);

        for (String userId : groupUsersList) {

            DocumentReference chatGroupRef = mFireBaseFireStore.collection("Chats").document(userId).collection("Groups").document(uniqueId);
            chatGroupRef.set(new Group(groupName, null, "", mCurrentUserId));

            for (String userIdParticipants : groupUsersList) {

                DocumentReference participantsRef = mFireBaseFireStore.collection("Chats").document(userId).collection("Groups").document(uniqueId).collection("Participants").document(userIdParticipants);
                participantsRef.set(new GroupParticipant(userIdParticipants));
            }
        }

        finish();
    }

    private void readUsers() {

        CollectionReference usersPath = mFireBaseFireStore.collection("User");
        usersPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                mUsers.clear();

                for (QueryDocumentSnapshot doc : value) {

                    User user = doc.toObject(User.class);

                    if (!user.getId().equals(mCurrentUserId)) {
                        mUsers.add(user);
                    }
                }

                recyclerView.setAdapter(new UserAdapter(context, mUsers, false, sharedPreferences, true, false, null, new UserAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(User user, ImageView imageView) {

                        String selectedUserId = user.getId();

                        if (imageView.getVisibility() == View.INVISIBLE) {

                            groupUsersList.add(selectedUserId);
                            imageView.setVisibility(View.VISIBLE);

                        } else {

                            groupUsersList.remove(selectedUserId);
                            imageView.setVisibility(View.INVISIBLE);
                        }
                    }
                }));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
