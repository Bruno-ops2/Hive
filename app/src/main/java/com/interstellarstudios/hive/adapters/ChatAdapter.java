package com.interstellarstudios.hive.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.interstellarstudios.hive.R;
import com.interstellarstudios.hive.database.ChatUserEntity;
import com.interstellarstudios.hive.models.Message;
import com.interstellarstudios.hive.repository.Repository;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Message> mChat;
    private boolean isSender;
    private Repository repository;

    public ChatAdapter(Context mContext, List<Message> mChat, Repository repository) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.repository = repository;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new ChatAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new ChatAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        Message message = mChat.get(position);

        holder.textViewShowMessage.setText(message.getMessage());
        holder.textViewTimeStamp.setText(getDate(message.getTimeStamp(), "HH:mm"));

        profilePic(message.getSenderId(), holder.imageViewProfilePic);

        if (isSender) {

            if (message.getSeen()) {
                holder.imageViewBlueTicks.setVisibility(View.VISIBLE);
                holder.imageViewGreyTicks.setVisibility(View.INVISIBLE);
            } else {
                holder.imageViewGreyTicks.setVisibility(View.VISIBLE);
                holder.imageViewBlueTicks.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewShowMessage;
        private TextView textViewTimeStamp;
        private ImageView imageViewBlueTicks;
        private ImageView imageViewGreyTicks;
        private ImageView imageViewProfilePic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewShowMessage = itemView.findViewById(R.id.show_message);
            textViewTimeStamp = itemView.findViewById(R.id.text_view_time_stamp);
            imageViewBlueTicks = itemView.findViewById(R.id.image_view_blue_ticks);
            imageViewGreyTicks = itemView.findViewById(R.id.image_view_grey_ticks);
            imageViewProfilePic = itemView.findViewById(R.id.profile_pic);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mChat.get(position).getIsSender()) {
            isSender = true;
            return MSG_TYPE_RIGHT;
        } else {
            isSender = false;
            return MSG_TYPE_LEFT;
        }
    }

    public static String getDate(long milliSeconds, String dateFormat) {

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void profilePic(String altUserId, ImageView imageProfilePic) {

        List<ChatUserEntity> chatUserEntityList = repository.getChatUser();

        String profilePicUrl = null;
        for (ChatUserEntity chatUserEntity : chatUserEntityList) {
            profilePicUrl = chatUserEntity.getProfilePicUrl();
        }

        Picasso.get().load(profilePicUrl).into(imageProfilePic);

        /*FirebaseFirestore mFireBaseFireStore = FirebaseFirestore.getInstance();

        DocumentReference currentUserRef = mFireBaseFireStore.collection("User").document(altUserId);
        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    User user = snapshot.toObject(User.class);
                    Picasso.get().load(user.getProfilePicUrl()).into(imageProfilePic);
                }
            }
        });*/
    }
}
