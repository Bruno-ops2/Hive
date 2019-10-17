package com.interstellarstudios.hive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.hive.models.User;

import es.dmoral.toasty.Toasty;

public class UserNameActivity extends AppCompatActivity {

    private Context context = this;
    private ImageView imageViewHiveLogo;
    private EditText editTextUsername;
    private Button buttonConfirmUsername;
    private Window window;
    private View container;
    private ImageView imageViewDarkMode;
    private ImageView imageViewLightMode;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);
        editTextUsername = findViewById(R.id.edit_text_username);
        buttonConfirmUsername = findViewById(R.id.button_confirm_username);
        imageViewDarkMode = findViewById(R.id.image_view_dark_mode);
        imageViewLightMode = findViewById(R.id.image_view_light_mode);

        buttonConfirmUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseUsername();
            }
        });

        window = this.getWindow();
        container = findViewById(R.id.container2);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.Primary));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.Primary));
        }

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        imageViewDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                darkMode();
                saveDarkModePreference();
            }
        });

        imageViewLightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lightMode();
                saveLightModePreference();
            }
        });
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

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.Primary));
        }

        imageViewLightMode.setVisibility(View.GONE);
        imageViewDarkMode.setVisibility(View.VISIBLE);

        imageViewHiveLogo.setImageResource(R.drawable.hive_text_logo_dark);

        editTextUsername.setTextColor(ContextCompat.getColor(context, R.color.DarkText));
        editTextUsername.setHintTextColor(ContextCompat.getColor(context, R.color.DarkText));

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(imageViewHiveLogo);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(editTextUsername);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(buttonConfirmUsername);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.Primary));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.Primary));
        }
    }

    private void darkMode() {

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryDarkTheme));
        }

        imageViewDarkMode.setVisibility(View.GONE);
        imageViewLightMode.setVisibility(View.VISIBLE);

        imageViewHiveLogo.setImageResource(R.drawable.hive_text_logo_light);

        editTextUsername.setTextColor(ContextCompat.getColor(context, R.color.LightText));
        editTextUsername.setHintTextColor(ContextCompat.getColor(context, R.color.LightText));

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(imageViewHiveLogo);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(editTextUsername);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(buttonConfirmUsername);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryDarkTheme));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryDarkTheme));
        }
    }

    private void chooseUsername() {

        String username = editTextUsername.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toasty.info(context, "Please enter a username", Toast.LENGTH_LONG, true).show();
            return;
        } else if (username.length() < 6) {
            Toasty.info(context, "Username must be at least 6 characters", Toast.LENGTH_LONG, true).show();
            return;
        }

        CollectionReference userDetailsPath = mFireBaseFireStore.collection(mCurrentUserId);
        userDetailsPath.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);

                                if (user.getUsername() != null) {

                                    if (username.equals(user.getUsername())) {
                                        Toasty.error(context, "This username is taken", Toast.LENGTH_LONG, true).show();
                                    } else {
                                        DocumentReference userDetailsPath = mFireBaseFireStore.collection(mCurrentUserId).document("User");
                                        userDetailsPath.update("username", username).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Do something
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toasty.error(context, "Error, please ensure that there is an active network connection", Toast.LENGTH_LONG, true).show();
                                            }
                                        });
                                    }
                                }
                            }
                        } else {
                            Toasty.error(context, "Error, please ensure that there is an active network connection", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
    }
}
