package com.interstellarstudios.hive;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.interstellarstudios.hive.database.ChatUserEntity;
import com.interstellarstudios.hive.repository.Repository;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProfileViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

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

        ImageView imageViewProfilePic = findViewById(R.id.image_view_profile_pic);
        Picasso.get().load(profilePicUrl).into(imageViewProfilePic);

        TextView textViewUsername = findViewById(R.id.text_view_username);
        textViewUsername.setText(username);

        TextView textViewStatus = findViewById(R.id.text_view_status);
        textViewStatus.setText(status);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
