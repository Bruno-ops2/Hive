package com.nullparams.hive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nullparams.hive.database.ChatUserEntity;
import com.nullparams.hive.repository.Repository;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileViewActivity extends AppCompatActivity {

    private Context context = this;
    private Window window;
    private View container;
    private TextView textViewUsername;
    private TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        window = this.getWindow();
        container = findViewById(R.id.container);

        Repository repository = new Repository(getApplication());
        List<ChatUserEntity> chatUserEntityList = repository.getChatUser();

        String profilePicUrl = null;
        String username = null;
        String status = null;

        for (ChatUserEntity chatUserEntity : chatUserEntityList) {
            profilePicUrl = chatUserEntity.getProfilePicUrl();
            username = chatUserEntity.getUsername();
            status = chatUserEntity.getStatus();
        }

        textViewUsername = findViewById(R.id.text_view_username);
        textViewStatus = findViewById(R.id.text_view_status);
        textViewUsername.setText(username);
        textViewStatus.setText(status);

        ImageView imageViewProfilePic = findViewById(R.id.image_view_profile_pic);
        if (profilePicUrl != null) {
            Picasso.get().load(profilePicUrl).into(imageViewProfilePic);
        }

        boolean darkModeOn = sharedPreferences.getBoolean("darkModeOn", false);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }
    }

    private void lightMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        }

        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.SecondaryDark));
    }

    private void darkMode() {

        window.setStatusBarColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        if (container != null) {
            container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.SecondaryDark));
        }

        textViewUsername.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
        textViewStatus.setTextColor(ContextCompat.getColor(context, R.color.PrimaryLight));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
