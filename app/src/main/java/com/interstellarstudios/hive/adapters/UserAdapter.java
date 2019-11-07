package com.interstellarstudios.hive.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.hive.ChatActivity;
import com.interstellarstudios.hive.R;
import com.interstellarstudios.hive.models.Message;
import com.interstellarstudios.hive.models.User;
import com.interstellarstudios.hive.util.ShortenMessage;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
    private String lastMessage;
    private long timeStamp;
    private boolean isChat;
    private boolean isRead;
    private String messageType;
    private boolean darkModeOn;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat, SharedPreferences sharedPreferences) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
        darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = mUsers.get(position);

        if (darkModeOn) {
            holder.username.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewLastMessage.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewTimeStamp.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.imageViewStatusOnline.setImageResource(R.drawable.circle_online_dark);
        }

        holder.username.setText(user.getUsername());

        if (user.getProfilePicUrl() != null) {
            Picasso.get().load(user.getProfilePicUrl()).into(holder.profilePic);
        }

        if (user.getOnlineOffline().equals("online")) {
            holder.imageViewStatusOnline.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewStatusOnline.setVisibility(View.GONE);
        }

        if (isChat) {
            lastMessage(user.getId(), holder.textViewLastMessage, holder.textViewTimeStamp, holder.username);

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    deleteChat(user.getId());
                    return true;
                }
            });
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, ChatActivity.class);
                i.putExtra("userId", user.getId());
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profilePic;
        public ImageView imageViewStatusOnline;
        public TextView textViewLastMessage;
        public TextView textViewTimeStamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.textview_username);
            profilePic = itemView.findViewById(R.id.profile_pic);
            imageViewStatusOnline = itemView.findViewById(R.id.image_view_status_online);
            textViewLastMessage = itemView.findViewById(R.id.text_view_last_message);
            textViewTimeStamp = itemView.findViewById(R.id.text_view_time_stamp);
        }
    }

    private void lastMessage(String altUserId, TextView textViewLastMessage, TextView textViewTimeStamp, TextView textViewUsername) {

        lastMessage = "default";
        timeStamp = 0;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        FirebaseFirestore mFireBaseFireStore = FirebaseFirestore.getInstance();
        CollectionReference usersPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages");
        usersPath.orderBy("timeStamp", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {

                    Message message = doc.toObject(Message.class);

                    messageType = message.getMessageType();
                    lastMessage = message.getMessage();
                    timeStamp = message.getTimeStamp();
                    isRead = message.getIsRead();
                }

                if (lastMessage.equals("default")) {
                    textViewLastMessage.setText("");
                } else if (messageType.equals("image")) {
                    if (isRead) {
                        textViewLastMessage.setText("Image");
                    } else {
                        textViewLastMessage.setText("Image");
                        textViewLastMessage.setTypeface(null, Typeface.BOLD);
                        textViewUsername.setTypeface(null, Typeface.BOLD);
                    }
                } else if (messageType.equals("attachment")) {
                    if (isRead) {
                        textViewLastMessage.setText("Attachment");
                    } else {
                        textViewLastMessage.setText("Attachment");
                        textViewLastMessage.setTypeface(null, Typeface.BOLD);
                        textViewUsername.setTypeface(null, Typeface.BOLD);
                    }
                } else {
                    if (isRead) {
                        textViewLastMessage.setText(ShortenMessage.getShortDesc(lastMessage));
                    } else {
                        textViewLastMessage.setText(ShortenMessage.getShortDesc(lastMessage));
                        textViewLastMessage.setTypeface(null, Typeface.BOLD);
                        textViewUsername.setTypeface(null, Typeface.BOLD);
                    }
                }

                if (timeStamp == 0) {
                    textViewTimeStamp.setText("");
                } else {
                    textViewTimeStamp.setText(getDate(timeStamp, "HH:mm"));
                }
            }
        });
    }

    public static String getDate(long milliSeconds, String dateFormat) {

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void deleteChat(String altUserId) {

        new AlertDialog.Builder(mContext)
                .setTitle("Delete Chat")
                .setMessage("Are you sure you want to delete this chat?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = firebaseUser.getUid();

                        FirebaseFirestore mFireBaseFireStore = FirebaseFirestore.getInstance();

                        CollectionReference seenPath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages");
                        seenPath.get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                DocumentReference messagePath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId).collection("Messages").document(document.getId());
                                                messagePath.delete();

                                                DocumentReference chatMessagePath = mFireBaseFireStore.collection("Chats").document(currentUserId).collection("Single").document(altUserId);
                                                chatMessagePath.delete();
                                            }
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
