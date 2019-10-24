package com.interstellarstudios.hive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.hive.adapters.ChatAdapter;
import com.interstellarstudios.hive.database.ChatUserEntity;
import com.interstellarstudios.hive.database.CurrentUserEntity;
import com.interstellarstudios.hive.models.Message;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.repository.Repository;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private Context context = this;
    private String altUserId;
    private EditText editTextMessage;
    private String currentUserId;
    private ChatAdapter chatAdapter;
    private List<Message> mChat;
    private RecyclerView recyclerView;
    private ListenerRegistration seenListener;
    private FirebaseFirestore mFireBaseFireStore;
    private String currentUsername;
    private ImageView imageViewProfilePic;
    private TextView textViewUsername;
    private Window window;
    private View container;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mFireBaseFireStore = FirebaseFirestore.getInstance();

        repository = new Repository(getApplication());
        List<CurrentUserEntity> currentUserEntityList = repository.getCurrentUser();
        for (CurrentUserEntity currentUserEntity : currentUserEntityList) {
            currentUsername = currentUserEntity.getUsername();
        }

        ImageView searchImageView = findViewById(R.id.image_view_search);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

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
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ImageView imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageViewProfilePic = findViewById(R.id.image_view_profile_pic);
        textViewUsername = findViewById(R.id.text_view_username);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            altUserId = bundle.getString("userId");
        }

        ImageView imageViewSendMessage = findViewById(R.id.image_view_send_message);
        editTextMessage = findViewById(R.id.edit_text_message);

        imageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = editTextMessage.getText().toString();

                if (!message.equals("")) {
                    sendMessage(message);
                } else {
                    Toast.makeText(context, "You cannot send blank messages", Toast.LENGTH_LONG).show();
                }
                editTextMessage.setText("");
            }
        });

        getChatUserData();
        readMessages();
        seenMessage();
        updateToRead();
    }

    private void sendMessage(String message) {

        long timeStamp = System.currentTimeMillis();

        DocumentReference chatPathCurrent = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages").document();
        chatPathCurrent.set(new Message(message, timeStamp, true, false, currentUserId, altUserId, currentUsername, true));

        Map<String, Object> documentValidation = new HashMap<>();
        documentValidation.put("Validate document", "This is a document");

        DocumentReference userInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId);
        userInitialDocChatPath.set(documentValidation);

        DocumentReference chatPathAlt = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages").document();
        chatPathAlt.set(new Message(message, timeStamp, false, false, currentUserId, altUserId, currentUsername, false));

        DocumentReference altUserInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId);
        altUserInitialDocChatPath.set(documentValidation);
    }

    private void readMessages() {

        mChat = new ArrayList<>();

        CollectionReference usersPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages");
        usersPath.orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                mChat.clear();

                for (QueryDocumentSnapshot doc : value) {

                    Message message = doc.toObject(Message.class);

                    mChat.add(message);

                    chatAdapter = new ChatAdapter(context, mChat, repository);
                    recyclerView.setAdapter(chatAdapter);
                }
            }
        });
    }

    private void seenMessage() {

        CollectionReference seenPath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages");
        seenListener = seenPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {

                    DocumentReference messagePath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages").document(doc.getId());
                    messagePath.update("seen", true);
                }
            }
        });
    }

    private void updateToRead() {

        CollectionReference seenPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages");
        seenPath.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference messagePath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages").document(document.getId());
                                messagePath.update("isRead", true);
                            }
                        }
                    }
                });
    }

    private void getChatUserData() {

        DocumentReference chatUserRef = mFireBaseFireStore.collection("User").document(altUserId);
        chatUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    User user = snapshot.toObject(User.class);

                    repository.deleteChatUser();
                    ChatUserEntity chatUserEntity = new ChatUserEntity(user.getId(), user.getUsername(), user.getProfilePicUrl(), user.getStatus(), user.getEmailAddress());
                    repository.insert(chatUserEntity);

                    textViewUsername.setText(user.getUsername());

                    if (user.getProfilePicUrl() != null) {
                        Picasso.get().load(user.getProfilePicUrl()).into(imageViewProfilePic);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
        hideKeyboard(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(currentUserId);
        chatPath.update("onlineOffline", "online");

        currentUser(altUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(currentUserId);
        chatPath.update("onlineOffline", "offline");

        currentUser("none");

        seenListener.remove();
    }

    private void currentUser(String userId) {

        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString("chatUserId", userId);
        prefsEditor.apply();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
