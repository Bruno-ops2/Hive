package com.nullparams.hive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nullparams.hive.adapters.ChatAdapter;
import com.nullparams.hive.database.ChatUserEntity;
import com.nullparams.hive.database.MessageEntity;
import com.nullparams.hive.models.Message;
import com.nullparams.hive.models.User;
import com.nullparams.hive.repository.Repository;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.sjl.foreground.Foreground;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ChatActivity extends AppCompatActivity implements Foreground.Listener {

    private Context context = this;
    private String altUserId;
    private EditText editTextMessage;
    private String currentUserId;
    private ChatAdapter chatAdapter;
    private List<Message> mChat;
    private RecyclerView recyclerView;
    private FirebaseFirestore mFireBaseFireStore;
    private String currentUsername;
    private ImageView imageViewProfilePic;
    private TextView textViewUsername;
    private Window window;
    private View container;
    private Repository repository;
    private Foreground.Binding listenerBinding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int PICK_DOCUMENT_REQUEST = 4;
    private static final int PERMISSION_USE_CAMERA_REQUEST = 12;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST = 13;
    private File photoFile = null;
    private String pathToFile;
    private AutoCompleteTextView searchField;
    private String filePath;
    private String fileName;
    private Toolbar toolbar;
    private ImageView imageViewBack;
    private SharedPreferences sharedPreferences;
    private ConstraintLayout sendMessageContainer;
    private ImageView searchImageView;
    private ImageView backArrowImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        currentUsername = sharedPreferences.getString("username", "");

        mFireBaseFireStore = FirebaseFirestore.getInstance();

        repository = new Repository(getApplication());

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        window = this.getWindow();
        container = findViewById(R.id.container);
        sendMessageContainer = findViewById(R.id.send_message_container);

        toolbar = findViewById(R.id.toolbar);
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

        searchField = findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals("")) {
                    chatAdapter.getFilter().filter(s);
                }
            }
        });

        imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imageViewProfilePic = findViewById(R.id.image_view_profile_pic);
        imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ProfileViewActivity.class);
                startActivity(i);
            }
        });

        textViewUsername = findViewById(R.id.text_view_username);
        textViewUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ProfileViewActivity.class);
                startActivity(i);
            }
        });

        backArrowImageView = findViewById(R.id.image_view_back_arrow);

        searchImageView = findViewById(R.id.image_view_search);
        searchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewBack.setVisibility(View.GONE);
                imageViewProfilePic.setVisibility(View.GONE);
                textViewUsername.setVisibility(View.GONE);
                searchImageView.setVisibility(View.GONE);
                searchField.setVisibility(View.VISIBLE);
                backArrowImageView.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.BounceInRight)
                        .duration(300)
                        .playOn(searchField);

                YoYo.with(Techniques.BounceInLeft)
                        .duration(300)
                        .playOn(backArrowImageView);
            }
        });

        backArrowImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewBack.setVisibility(View.VISIBLE);
                imageViewProfilePic.setVisibility(View.VISIBLE);
                textViewUsername.setVisibility(View.VISIBLE);
                searchImageView.setVisibility(View.VISIBLE);
                searchField.setVisibility(View.GONE);
                backArrowImageView.setVisibility(View.GONE);

                YoYo.with(Techniques.BounceInRight)
                        .duration(300)
                        .playOn(textViewUsername);

                YoYo.with(Techniques.BounceInLeft)
                        .duration(300)
                        .playOn(imageViewProfilePic);

                YoYo.with(Techniques.BounceInRight)
                        .duration(300)
                        .playOn(searchImageView);

                YoYo.with(Techniques.BounceInLeft)
                        .duration(300)
                        .playOn(imageViewBack);
            }
        });

        ImageView imageViewAddCamera = findViewById(R.id.image_view_add_camera);
        ImageView imageViewAddImage = findViewById(R.id.image_view_add_image);
        ImageView imageViewAddAttachment = findViewById(R.id.image_view_add_attachment);
        ImageView imageViewMinusMedia = findViewById(R.id.image_view_minus_media);
        ImageView imageViewAddMedia = findViewById(R.id.image_view_add_media);

        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();

                imageViewAddMedia.setVisibility(View.VISIBLE);
                imageViewMinusMedia.setVisibility(View.INVISIBLE);
                imageViewAddCamera.setVisibility(View.GONE);
                imageViewAddImage.setVisibility(View.GONE);
                imageViewAddAttachment.setVisibility(View.GONE);

                YoYo.with(Techniques.FlipInX)
                        .duration(300)
                        .playOn(imageViewAddMedia);
            }
        });

        imageViewAddCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToUseCamera();
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();

                    imageViewAddMedia.setVisibility(View.VISIBLE);
                    imageViewMinusMedia.setVisibility(View.INVISIBLE);
                    imageViewAddCamera.setVisibility(View.GONE);
                    imageViewAddImage.setVisibility(View.GONE);
                    imageViewAddAttachment.setVisibility(View.GONE);

                    YoYo.with(Techniques.FlipInX)
                            .duration(300)
                            .playOn(imageViewAddMedia);
                }
            }
        });

        imageViewAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else {
                    new MaterialFilePicker()
                            .withActivity(ChatActivity.this)
                            .withRequestCode(PICK_DOCUMENT_REQUEST)
                            .withHiddenFiles(true)
                            .start();

                    imageViewAddMedia.setVisibility(View.VISIBLE);
                    imageViewMinusMedia.setVisibility(View.INVISIBLE);
                    imageViewAddCamera.setVisibility(View.GONE);
                    imageViewAddImage.setVisibility(View.GONE);
                    imageViewAddAttachment.setVisibility(View.GONE);

                    YoYo.with(Techniques.FlipInX)
                            .duration(300)
                            .playOn(imageViewAddMedia);
                }
            }
        });

        imageViewAddMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewAddMedia.setVisibility(View.INVISIBLE);
                imageViewMinusMedia.setVisibility(View.VISIBLE);
                imageViewAddCamera.setVisibility(View.VISIBLE);
                imageViewAddImage.setVisibility(View.VISIBLE);
                imageViewAddAttachment.setVisibility(View.VISIBLE);

                YoYo.with(Techniques.FlipInX)
                        .duration(300)
                        .playOn(imageViewMinusMedia);

                YoYo.with(Techniques.FlipInX)
                        .duration(300)
                        .playOn(imageViewAddCamera);

                YoYo.with(Techniques.FlipInX)
                        .duration(300)
                        .playOn(imageViewAddImage);

                YoYo.with(Techniques.FlipInX)
                        .duration(300)
                        .playOn(imageViewAddAttachment);

            }
        });

        imageViewMinusMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewAddMedia.setVisibility(View.VISIBLE);
                imageViewMinusMedia.setVisibility(View.INVISIBLE);
                imageViewAddCamera.setVisibility(View.GONE);
                imageViewAddImage.setVisibility(View.GONE);
                imageViewAddAttachment.setVisibility(View.GONE);

                YoYo.with(Techniques.FlipInX)
                        .duration(300)
                        .playOn(imageViewAddMedia);
            }
        });

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
                }
                editTextMessage.setText("");
            }
        });

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        getChatUserData();
        readMessages();
        seenMessage();
        updateToRead();
        searchSetup();

        listenerBinding = Foreground.get(getApplication()).addListener(this);
    }

    private void lightMode() {

        sendMessageContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(backArrowImageView, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(searchImageView, ContextCompat.getColorStateList(context, R.color.PrimaryDark));

        searchField.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        searchField.setHintTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(context, R.color.SecondaryDark));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }
    }

    private void darkMode() {

        sendMessageContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(backArrowImageView, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(searchImageView, ContextCompat.getColorStateList(context, R.color.PrimaryLight));

        searchField.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        searchField.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(context, R.color.PrimaryLight));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }
    }

    private void sendMessage(String message) {

        long timeStamp = System.currentTimeMillis();
        String messageId = Long.toString(timeStamp);

        DocumentReference chatPathCurrent = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages").document(messageId);
        chatPathCurrent.set(new Message(messageId, message, timeStamp, true, false, currentUserId, altUserId, currentUsername, true, "text", "", ""));

        Map<String, Object> documentValidation = new HashMap<>();
        documentValidation.put("Validate document", "This is a document");

        DocumentReference userInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId);
        userInitialDocChatPath.set(documentValidation);

        DocumentReference chatPathAlt = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages").document(messageId);
        chatPathAlt.set(new Message(messageId, message, timeStamp, false, false, currentUserId, altUserId, currentUsername, false, "text", "", ""));

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

                    chatAdapter = new ChatAdapter(context, mChat, repository, sharedPreferences);
                    recyclerView.setAdapter(chatAdapter);
                }
            }
        });
    }

    private void seenMessage() {

        CollectionReference seenPath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages");
        seenPath.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                DocumentReference messagePath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages").document(document.getId());
                                messagePath.update("seen", true);
                            }
                        }
                    }
                });
    }

    /*private void seenMessage() {

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
    }*/

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

        repository.deleteChatUser();

        DocumentReference chatUserRef = mFireBaseFireStore.collection("User").document(altUserId);
        chatUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        User user = document.toObject(User.class);

                        ChatUserEntity chatUserEntity = new ChatUserEntity(user.getId(), user.getUsername(), user.getProfilePicUrl(), user.getStatus(), user.getEmailAddress());
                        repository.insert(chatUserEntity);

                        textViewUsername.setText(user.getUsername());

                        if (user.getProfilePicUrl() != null) {
                            Picasso.get().load(user.getProfilePicUrl()).into(imageViewProfilePic);
                        }

                        chatAdapter = new ChatAdapter(context, mChat, repository, sharedPreferences);
                        recyclerView.setAdapter(chatAdapter);
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
        currentUser(altUserId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUser("none");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listenerBinding.unbind();
    }

    @Override
    public void onBecameForeground() {
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(currentUserId);
        chatPath.update("onlineOffline", "online");
    }

    @Override
    public void onBecameBackground() {
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(currentUserId);
        chatPath.update("onlineOffline", "offline");
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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            byte[] compressedImage = compressImageUri(imageUri);
            uploadImage(compressedImage);
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            Uri imageUri = Uri.fromFile(photoFile);

            byte[] compressedImage = compressImageUri(imageUri);
            uploadImage(compressedImage);
        }

        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK) {

            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

            attachmentUpload();
        }
    }

    private byte[] compressImageUri(Uri imageUri) {

        Bitmap bmp = null;
        try {
            bmp = handleSamplingAndRotationBitmap(context, imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bmp.getWidth() > 1024) {
            int nh = (int) (bmp.getHeight() * (1024.0 / bmp.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bmp, 1024, nh, true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 40, stream);
            byte[] byteArray = stream.toByteArray();
            try {
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return byteArray;
        } else {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 40, stream);
            byte[] byteArray = stream.toByteArray();
            try {
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return byteArray;
        }
    }

    private void uploadImage(byte[] compressedImage) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Users/" + currentUserId + "/Chat_images");
        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");

        UploadTask uploadTask = fileReference.putBytes(compressedImage);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {

                            Uri downloadUri = task.getResult();
                            String imageUrl = downloadUri.toString();

                            long timeStamp = System.currentTimeMillis();
                            String messageId = Long.toString(timeStamp);

                            DocumentReference chatPathCurrent = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages").document(messageId);
                            chatPathCurrent.set(new Message(messageId, "", timeStamp, true, false, currentUserId, altUserId, currentUsername, true, "image", imageUrl, messageId));

                            Map<String, Object> documentValidation = new HashMap<>();
                            documentValidation.put("Validate document", "This is a document");

                            DocumentReference userInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId);
                            userInitialDocChatPath.set(documentValidation);

                            DocumentReference chatPathAlt = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages").document(messageId);
                            chatPathAlt.set(new Message(messageId, "", timeStamp, false, false, currentUserId, altUserId, currentUsername, false, "image", imageUrl, messageId));

                            DocumentReference altUserInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId);
                            altUserInitialDocChatPath.set(documentValidation);
                        }
                    }
                });
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage) throws IOException {

        int MAX_HEIGHT = 768;
        int MAX_WIDTH = 1024;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public void getPermissionToUseCamera() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to access Camera")
                .setMessage("This permission is needed in order to take a photo immediately for use in Notes. Manually enable in Settings > Apps & notifications > Hive > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_USE_CAMERA_REQUEST);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    public void getPermissionToWriteStorage() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to Write to External Storage")
                .setMessage("This permission is needed in order save images taken with the camera when accessed by the App. Manually enable in Settings > Apps & notifications > Hive > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_USE_CAMERA_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "Camera permission granted", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(context, "Camera permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "External storage permission granted", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(context, "External storage permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = createPhotoFile();

            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(context, "com.nullparams.hive.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createPhotoFile() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String name = sdf.format(calendar.getTime());

        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;

        try {
            image = File.createTempFile(name, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pathToFile);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void searchSetup() {

        repository.deleteAllMessages();

        CollectionReference messagesPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages");
        messagesPath.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Message message = document.toObject(Message.class);

                                MessageEntity messageEntity = new MessageEntity(message.getMessageId(), message.getMessage(), message.getTimeStamp(), message.getIsSender(), message.getSeen(), message.getSenderId(), message.getReceiverId(), message.getSenderUsername(), message.getIsRead(), message.getMessageType(), message.getImageUrl(), message.getImageFileName());
                                repository.insert(messageEntity);
                            }
                        }
                    }
                });
    }

    private void attachmentUpload() {

        final Uri file = Uri.fromFile(new File(filePath));
        StorageReference AttachmentStorageRef = FirebaseStorage.getInstance().getReference("Users/" + currentUserId + "/Attachments");
        final StorageReference fileReference = AttachmentStorageRef.child(fileName);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UploadTask uploadTask = fileReference.putFile(file);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();
                                String imageUrl = downloadUri.toString();

                                long timeStamp = System.currentTimeMillis();
                                String messageId = Long.toString(timeStamp);

                                DocumentReference chatPathCurrent = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages").document(messageId);
                                chatPathCurrent.set(new Message(messageId, "", timeStamp, true, false, currentUserId, altUserId, currentUsername, true, "attachment", imageUrl, messageId));

                                Map<String, Object> documentValidation = new HashMap<>();
                                documentValidation.put("Validate document", "This is a document");

                                DocumentReference userInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId);
                                userInitialDocChatPath.set(documentValidation);

                                DocumentReference chatPathAlt = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId).collection("Messages").document(messageId);
                                chatPathAlt.set(new Message(messageId, "", timeStamp, false, false, currentUserId, altUserId, currentUsername, false, "attachment", imageUrl, messageId));

                                DocumentReference altUserInitialDocChatPath = mFireBaseFireStore.collection("Chats").document(altUserId).collection("Single").document(currentUserId);
                                altUserInitialDocChatPath.set(documentValidation);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
