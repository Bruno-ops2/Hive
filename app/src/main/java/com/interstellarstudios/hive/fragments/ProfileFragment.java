package com.interstellarstudios.hive.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.interstellarstudios.hive.R;
import com.interstellarstudios.hive.RegisterActivity;
import com.interstellarstudios.hive.StatusActivity;
import com.interstellarstudios.hive.UserNameActivity;
import com.interstellarstudios.hive.database.CurrentUserEntity;
import com.interstellarstudios.hive.database.HiveDatabase;
import com.interstellarstudios.hive.firestore.GetData;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.repository.Repository;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private Context context;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private FirebaseAuth mFireBaseAuth;
    private Repository repository;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewProfilePic;
    private TextView textViewUsername;
    private TextView textViewStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        context = getActivity();

        repository = new Repository(getActivity().getApplication());

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        List<CurrentUserEntity> currentUserEntityList = repository.getCurrentUser();

        String username = null;
        String profilePicUrl = null;
        String status = null;
        for (CurrentUserEntity currentUserEntity : currentUserEntityList) {
            profilePicUrl = currentUserEntity.getProfilePicUrl();
            username = currentUserEntity.getUsername();
            status = currentUserEntity.getStatus();
        }

        imageViewProfilePic = view.findViewById(R.id.image_view_profile_pic);
        if (profilePicUrl != null) {
            Picasso.get().load(profilePicUrl).into(imageViewProfilePic);
        }
        textViewUsername = view.findViewById(R.id.text_view_username);
        textViewUsername.setText(username);

        imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        textViewStatus = view.findViewById(R.id.text_view_status);
        textViewStatus.setText(status);

        ImageView imageViewEditStatus = view.findViewById(R.id.image_view_change_status);
        imageViewEditStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, StatusActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        TextView textViewEditStatus = view.findViewById(R.id.text_view_change_status);
        textViewEditStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, StatusActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        ImageView clearSearchHistory = view.findViewById(R.id.image_view_bin_icon);
        clearSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                repository.deleteAllRecentSearches();
                Toasty.success(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

        TextView textViewEditUsername = view.findViewById(R.id.text_view_change_username);
        textViewEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserNameActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        ImageView imageViewEditUsername = view.findViewById(R.id.image_view_change_username);
        imageViewEditUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, UserNameActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        TextView textViewLogout = view.findViewById(R.id.text_view_logout);
        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        ImageView imageViewLogout = view.findViewById(R.id.image_view_logout);
        imageViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        usernameOperations();

        return view;
    }

    private void logOut() {

        DocumentReference userPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        userPath.update("onlineOffline", "offline");

        DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("User").document(mCurrentUserId).collection("Tokens").document("User_Token");
        userTokenDocumentPath.delete();

        mFireBaseAuth.signOut();

        HiveDatabase hiveDatabase = HiveDatabase.getInstance(context);
        hiveDatabase.clearAllTables();

        Intent i = new Intent(context, RegisterActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        getActivity().finish();
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
            Picasso.get().load(imageUri).into(imageViewProfilePic);

            byte[] compressedImage = compressImageUri(imageUri);
            uploadProfilePic(compressedImage);
        }
    }

    private byte[] compressImageUri(Uri imageUri) {

        InputStream imageStream = null;
        try {
            imageStream = getActivity().getContentResolver().openInputStream(
                    imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        byte[] byteArray = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private void uploadProfilePic(byte[] compressedImage) {

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Users/" + mCurrentUserId + "/Profile_Pic");
        StorageReference fileReference = storageRef.child("profile_pic.jpeg");

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
                            String profilePicUrl = downloadUri.toString();

                            DocumentReference userDetailsPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
                            userDetailsPath.update("profilePicUrl", profilePicUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    GetData.currentUser(mFireBaseFireStore, mCurrentUserId, repository);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toasty.error(context, "Error, please ensure that there is an active network connection", Toast.LENGTH_LONG, true).show();
                                }
                            });
                        }
                    }
                });
    }

    private void usernameOperations() {

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
                    textViewUsername.setText(user.getUsername());
                    textViewStatus.setText(user.getStatus());
                }
            }
        });
    }
}
