package com.nullparams.hive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nullparams.hive.models.User;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    private Context context = this;
    private ImageView imageViewHiveLogo;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Window window;
    private View container;
    private ImageView imageViewDarkMode;
    private ImageView imageViewLightMode;
    private TextView textViewSignIn;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private String mCurrentUserEmail;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {

            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();

            Intent i = new Intent(context, MainActivity.class);
            startActivity(i);
            finish();
        }

        mProgressDialog = new ProgressDialog(context);
        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        Button buttonSignUp = findViewById(R.id.button_sign_up);
        Button buttonSignUpLater = findViewById(R.id.button_sign_up_later);
        TextView textViewOr = findViewById(R.id.text_view_or);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        buttonSignUpLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Are you sure you don't want to register?")
                        .setMessage("You won't be able to log in on another device.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                guestMode();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        textViewSignIn = findViewById(R.id.text_view_go_to_sign_in);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SignInActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        TextView textViewSignIn2 = findViewById(R.id.text_view_go_to_sign_in_2);
        textViewSignIn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SignInActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        imageViewDarkMode = findViewById(R.id.image_view_dark_mode);
        imageViewLightMode = findViewById(R.id.image_view_light_mode);

        window = this.getWindow();
        container = findViewById(R.id.container2);

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

        imageViewLightMode.setVisibility(View.GONE);
        imageViewDarkMode.setVisibility(View.VISIBLE);

        imageViewHiveLogo.setImageResource(R.drawable.hive_text_logo_dark);

        editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }
    }

    private void darkMode() {

        imageViewDarkMode.setVisibility(View.GONE);
        imageViewLightMode.setVisibility(View.VISIBLE);

        imageViewHiveLogo.setImageResource(R.drawable.hive_text_logo_light);

        editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }
    }

    private void registerUser() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toasty.info(context, "Please enter a valid email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toasty.info(context, "Please enter a password", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toasty.info(context, "Your password must be at least 8 characters and must contain at least 1 number", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toasty.info(context, "Please enter the same password in the confirm password field", Toast.LENGTH_LONG, true).show();
            return;
        }

        Random rand = new Random();
        int num = rand.nextInt(9000000) + 1000000;
        String randomNumber = Integer.toString(num);

        mProgressDialog.setMessage("Registering");
        mProgressDialog.show();

        mFireBaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (mFireBaseAuth.getCurrentUser() != null) {
                                mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
                                mCurrentUser = mFireBaseAuth.getCurrentUser();
                                mCurrentUserEmail = mCurrentUser.getEmail();
                            }

                            DocumentReference userDetailsPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
                            userDetailsPath.set(new User(mCurrentUserId, "user" + randomNumber, null, "online", "Available", mCurrentUserEmail));

                            registerToken();

                            Intent i = new Intent(context, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                            hideKeyboard(RegisterActivity.this);

                        } else {
                            Toasty.error(context, "Registration error, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void guestMode() {

        Random rand = new Random();
        int num = rand.nextInt(9000000) + 1000000;
        String randomNumber = Integer.toString(num);

        String guestEmail = "guest" + randomNumber + "@nullparams.com";
        String guestPassword = md5(guestEmail);

        mProgressDialog.setMessage("Launching");
        mProgressDialog.show();

        mFireBaseAuth.createUserWithEmailAndPassword(guestEmail, guestPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (mFireBaseAuth.getCurrentUser() != null) {
                                mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
                                mCurrentUser = mFireBaseAuth.getCurrentUser();
                                mCurrentUserEmail = mCurrentUser.getEmail();
                            }

                            DocumentReference userDetailsPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
                            userDetailsPath.set(new User(mCurrentUserId, "user" + randomNumber, null, "online", "Available", mCurrentUserEmail));

                            registerToken();

                            Intent i = new Intent(context, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                        } else {
                            Toasty.error(context, "Registration error, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    public static String md5(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")), 0, s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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

    private void registerToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();

                Map<String, Object> userToken = new HashMap<>();
                userToken.put("User_Token_ID", deviceToken);

                DocumentReference userTokenPath = mFireBaseFireStore.collection("User").document(mCurrentUserId).collection("Tokens").document("User_Token");
                userTokenPath.set(userToken);
            }
        });
    }
}

