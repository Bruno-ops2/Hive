package com.nullparams.hive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sjl.foreground.Foreground;

import es.dmoral.toasty.Toasty;

public class StatusActivity extends AppCompatActivity implements Foreground.Listener {

    private Context context = this;
    private ImageView imageViewHiveLogo;
    private EditText editTextStatus;
    private Button buttonConfirmStatus;
    private Window window;
    private View container;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private Foreground.Binding listenerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        imageViewHiveLogo = findViewById(R.id.image_view_hive_logo);
        editTextStatus = findViewById(R.id.edit_text_status);
        buttonConfirmStatus = findViewById(R.id.button_confirm_status);

        buttonConfirmStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseStatus();
            }
        });

        window = this.getWindow();
        container = findViewById(R.id.container2);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        listenerBinding = Foreground.get(getApplication()).addListener(this);
    }

    private void lightMode() {

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        imageViewHiveLogo.setImageResource(R.drawable.hive_text_logo_dark);

        editTextStatus.setTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));
        editTextStatus.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryDark));

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(imageViewHiveLogo);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(editTextStatus);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(buttonConfirmStatus);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }
    }

    private void darkMode() {

        if (container != null) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        imageViewHiveLogo.setImageResource(R.drawable.hive_text_logo_light);

        editTextStatus.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        editTextStatus.setHintTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(imageViewHiveLogo);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(editTextStatus);

        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(buttonConfirmStatus);

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }
    }

    private void chooseStatus() {

        String status = editTextStatus.getText().toString().trim();

        if (TextUtils.isEmpty(status)) {
            Toasty.info(context, "Please enter a status", Toast.LENGTH_LONG, true).show();
            return;
        }

        DocumentReference userDetailsPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        userDetailsPath.update("status", status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                hideKeyboard(StatusActivity.this);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(context, "Error, please ensure that there is an active network connection", Toast.LENGTH_LONG, true).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        listenerBinding.unbind();
    }

    @Override
    public void onBecameForeground() {
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        chatPath.update("onlineOffline", "online");
    }

    @Override
    public void onBecameBackground() {
        DocumentReference chatPath = mFireBaseFireStore.collection("User").document(mCurrentUserId);
        chatPath.update("onlineOffline", "offline");
    }
}
