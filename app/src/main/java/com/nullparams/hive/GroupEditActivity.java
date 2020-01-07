package com.nullparams.hive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nullparams.hive.adapters.UserAdapter;
import com.nullparams.hive.models.Group;
import com.nullparams.hive.models.User;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class GroupEditActivity extends AppCompatActivity {

    private Context context = this;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String currentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private String uniqueId;
    private RecyclerView recyclerView;
    private List<String> usersList = new ArrayList<>();
    private List<User> mUsers = new ArrayList<>();
    private UserAdapter adapter;
    private ImageView imageViewGroupPic;
    private TextView textViewGroupName;
    private SharedPreferences sharedPreferences;
    private Window window;
    private View container;
    private TextView textViewAddUsers;
    private ImageView imageViewEditIcon;
    private ImageView imageViewAddUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            uniqueId = bundle.getString("uniqueId");
        }

        mFireBaseFireStore = FirebaseFirestore.getInstance();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        textViewGroupName = findViewById(R.id.text_view_group_name);
        textViewGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGroupName();
            }
        });

        imageViewEditIcon = findViewById(R.id.image_view_edit_icon);
        imageViewEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editGroupName();
            }
        });

        imageViewGroupPic = findViewById(R.id.image_view_group_pic);
        imageViewGroupPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        imageViewAddUsers = findViewById(R.id.image_view_add_users);
        imageViewAddUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, AddUsersActivity.class);
                i.putExtra("uniqueId", uniqueId);
                startActivity(i);
            }
        });

        textViewAddUsers = findViewById(R.id.text_view_add_users);
        textViewAddUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, AddUsersActivity.class);
                i.putExtra("uniqueId", uniqueId);
                startActivity(i);
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        window = this.getWindow();
        container = findViewById(R.id.container);

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        getGroupInfo();
        readChats();
    }

    private void lightMode() {

        textViewAddUsers.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        textViewGroupName.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewEditIcon, ContextCompat.getColorStateList(context, R.color.PrimaryDark));
        ImageViewCompat.setImageTintList(imageViewAddUsers, ContextCompat.getColorStateList(context, R.color.PrimaryDark));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }
    }

    private void darkMode() {

        textViewAddUsers.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewGroupName.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewEditIcon, ContextCompat.getColorStateList(context, R.color.PrimaryLight));
        ImageViewCompat.setImageTintList(imageViewAddUsers, ContextCompat.getColorStateList(context, R.color.PrimaryLight));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }
    }

    private void readChats() {

        CollectionReference chatPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Groups").document(uniqueId).collection("Participants");
        chatPath.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                usersList.clear();

                for (QueryDocumentSnapshot doc : value) {
                    usersList.add(doc.getId());
                }
                readUsers();
            }
        });
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

                    for (String id : usersList) {

                        if (user.getId().equals(id)) {
                            mUsers.add(user);
                        }
                    }
                }
                adapter = new UserAdapter(context, mUsers, false, sharedPreferences, false, true, uniqueId, null);
                recyclerView.setAdapter(adapter);
            }
        });
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

            Picasso.get().load(imageUri).into(imageViewGroupPic);

            byte[] compressedImage = compressImageUri(imageUri);
            uploadImage(compressedImage);
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

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Groups/" + currentUserId + "/Images");
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

                            CollectionReference participantsPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Groups").document(uniqueId).collection("Participants");
                            participantsPath.get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    String participantUserId = document.getId();

                                                    DocumentReference chatPathCurrent = mFireBaseFireStore.collection("Chats").document(participantUserId).collection("Groups").document(uniqueId);
                                                    chatPathCurrent.update("groupPicUrl", imageUrl);
                                                }

                                                Toasty.success(context, "Group picture updated", Toast.LENGTH_LONG, true).show();

                                            } else {
                                                Toasty.error(context, "Error, please ensure that there is an active network connection", Toast.LENGTH_LONG, true).show();
                                            }
                                        }
                                    });
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

    private void getGroupInfo() {

        DocumentReference currentUserRef = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Groups").document(uniqueId);
        currentUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        Group group = document.toObject(Group.class);
                        textViewGroupName.setText(group.getGroupName());

                        if (group.getGroupPicUrl() != null) {
                            Picasso.get().load(group.getGroupPicUrl()).into(imageViewGroupPic);
                        }
                    }
                }
            }
        });
    }

    private void editGroupName() {

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.edit_group_name_prompt, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String groupName = userInput.getText().toString();

                                if (TextUtils.isEmpty(groupName)) {
                                    Toasty.info(context, "Enter a group name", Toast.LENGTH_LONG, true).show();
                                    return;
                                }

                                CollectionReference participantsPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Groups").document(uniqueId).collection("Participants");
                                participantsPath.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        String participantUserId = document.getId();

                                                        DocumentReference groupRef = mFireBaseFireStore.collection("Chats").document(participantUserId).collection("Groups").document(uniqueId);
                                                        groupRef.update("groupName", groupName);
                                                    }

                                                    textViewGroupName.setText(groupName);
                                                    Toasty.success(context, "Group name updated", Toast.LENGTH_LONG, true).show();

                                                } else {
                                                    Toasty.error(context, "Error, please ensure that there is an active network connection", Toast.LENGTH_LONG, true).show();
                                                }
                                            }
                                        });
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
