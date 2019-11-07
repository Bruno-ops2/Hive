package com.interstellarstudios.hive.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import com.interstellarstudios.hive.database.HiveDatabase;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.repository.Repository;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

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
    private SharedPreferences sharedPreferences;
    private ConstraintLayout layout;
    private TextView textViewDarkMode;
    private TextView textViewEditStatus;
    private TextView textViewSearchHistory;
    private TextView textViewEditUsername;
    private TextView textViewLogout;
    private ImageView imageViewCameraIcon;
    private Toolbar toolbar;
    private TextView textViewFragmentTitle;
    private Window window;
    private BottomNavigationView bottomNav;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        context = getActivity();

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);

        repository = new Repository(getActivity().getApplication());

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        layout = view.findViewById(R.id.container2);
        textViewDarkMode = view.findViewById(R.id.text_view_dark_mode);

        imageViewProfilePic = view.findViewById(R.id.image_view_profile_pic);
        textViewUsername = view.findViewById(R.id.text_view_username);
        textViewStatus = view.findViewById(R.id.text_view_status);
        imageViewCameraIcon = view.findViewById(R.id.image_view_camera_icon);

        imageViewProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        ImageView imageViewEditStatus = view.findViewById(R.id.image_view_change_status);
        imageViewEditStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, StatusActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        textViewEditStatus = view.findViewById(R.id.text_view_change_status);
        textViewEditStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, StatusActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        ImageView clearSearchHistory = view.findViewById(R.id.image_view_bin_icon);

        ImageView imageViewClearSearchHistoryIcon = view.findViewById(R.id.image_view_search_history_icon);
        imageViewClearSearchHistoryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                repository.deleteAllRecentSearches();
                Toasty.success(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

        textViewSearchHistory = view.findViewById(R.id.text_view_search_history);
        textViewSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                repository.deleteAllRecentSearches();
                Toasty.success(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

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

        textViewEditUsername = view.findViewById(R.id.text_view_change_username);
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

        textViewLogout = view.findViewById(R.id.text_view_logout);
        textViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogOut();
            }
        });

        ImageView imageViewLogout = view.findViewById(R.id.image_view_logout);
        imageViewLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLogOut();
            }
        });

        window = getActivity().getWindow();

        toolbar = getActivity().findViewById(R.id.toolbar);
        textViewFragmentTitle = getActivity().findViewById(R.id.text_view_fragment_title);
        bottomNav = getActivity().findViewById(R.id.bottom_nav);

        Switch switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchDarkMode.setChecked(darkModeOn);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    darkMode();
                    saveDarkModePreference();
                } else {
                    lightMode();
                    saveLightModePreference();
                }
            }
        });

        userDetailsOperations();

        return view;
    }

    private void saveLightModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("darkModeOn", false);
        prefsEditor.apply();
    }

    private void saveDarkModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("darkModeOn", true);
        prefsEditor.apply();
    }

    private void lightMode() {

        View container = getActivity().findViewById(R.id.container);

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector));

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewDarkMode.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewSearchHistory.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewEditUsername.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewEditStatus.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewLogout.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        imageViewCameraIcon.setImageResource(R.drawable.camera_icon);
    }

    private void darkMode() {

        View container = getActivity().findViewById(R.id.container);

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        bottomNav.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(context, R.color.bottom_nav_selector_light));

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));

        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewDarkMode.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewSearchHistory.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewEditUsername.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewEditStatus.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewLogout.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        imageViewCameraIcon.setImageResource(R.drawable.camera_icon_dark);
    }

    private void startLogOut() {

        new AlertDialog.Builder(context)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        logOut();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
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
                            userDetailsPath.update("profilePicUrl", profilePicUrl);
                        }
                    }
                });
    }

    private void userDetailsOperations() {

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
                    }

                    textViewUsername.setText(user.getUsername());
                    textViewStatus.setText(user.getStatus());
                }
            }
        });
    }
}
