package com.nullparams.hive.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nullparams.hive.R;
import com.nullparams.hive.models.GroupMessage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> implements Filterable {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_IMAGE_LEFT = 2;
    private static final int MSG_TYPE_IMAGE_RIGHT = 3;
    private static final int MSG_TYPE_ATTACHMENT_LEFT = 4;
    private static final int MSG_TYPE_ATTACHMENT_RIGHT = 5;
    private Context mContext;
    private List<GroupMessage> mChat;
    private List<GroupMessage> mChatFull;
    private boolean isSender;
    private boolean darkModeOn;

    public GroupMessageAdapter(Context mContext, List<GroupMessage> mChat, SharedPreferences sharedPreferences) {

        this.mContext = mContext;
        this.mChat = mChat;
        mChatFull = new ArrayList<>(mChat);
        darkModeOn = sharedPreferences.getBoolean("darkModeOn", true);
    }

    @NonNull
    @Override
    public GroupMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_LEFT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_LEFT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.image_item_left, parent, false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else if (viewType == MSG_TYPE_IMAGE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.image_item_right, parent, false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else if (viewType == MSG_TYPE_ATTACHMENT_LEFT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.attachment_item_left, parent, false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.attachment_item_right, parent, false);
            return new GroupMessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMessageAdapter.ViewHolder holder, int position) {

        GroupMessage message = mChat.get(position);

        if (darkModeOn) {
            holder.textViewTimeStamp.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
            holder.textViewUsername.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryLight));
        }

        if (isSender) {

            holder.imageViewGreyTicks.setVisibility(View.VISIBLE);
            holder.imageViewBlueTicks.setVisibility(View.INVISIBLE);

        } else {

            holder.imageViewProfilePic.setVisibility(View.GONE);
            holder.textViewUsername.setVisibility(View.VISIBLE);
            holder.textViewUsername.setText(message.getSenderUsername());
        }

        holder.textViewTimeStamp.setText(getDate(message.getTimeStamp(), "HH:mm"));

        switch (message.getMessageType()) {

            case "image":

                if (fileExists(message.getImageFileName())) {
                    loadLocally(message.getImageFileName(), holder.imageViewChatImage);

                    holder.imageViewChatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String folder = Environment.getExternalStorageDirectory() + File.separator + "TAC/Images/";
                            String file = folder + message.getImageFileName() + ".jpg";
                            Uri uri = Uri.parse(file);

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "*/*");
                            mContext.startActivity(intent);
                        }
                    });

                } else {

                    Picasso.get().load(message.getImageUrl()).into(holder.imageViewChatImage);
                    getImage(message.getImageUrl(), message.getImageFileName());

                    holder.imageViewChatImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Uri uri = Uri.parse(message.getImageUrl());

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "*/*");
                            mContext.startActivity(intent);
                        }
                    });
                }

                break;

            case "text":
                holder.textViewShowMessage.setText(message.getMessage());

                break;

            case "attachment":
                holder.imageViewChatImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getImageUrl()));
                        mContext.startActivity(browserIntent);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewShowMessage;
        private TextView textViewTimeStamp;
        private ImageView imageViewProfilePic;
        private ImageView imageViewChatImage;
        private TextView textViewUsername;
        private ImageView imageViewBlueTicks;
        private ImageView imageViewGreyTicks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewShowMessage = itemView.findViewById(R.id.show_message);
            textViewTimeStamp = itemView.findViewById(R.id.text_view_time_stamp);
            imageViewProfilePic = itemView.findViewById(R.id.profile_pic);
            imageViewChatImage = itemView.findViewById(R.id.show_image);
            textViewUsername = itemView.findViewById(R.id.show_username);
            imageViewBlueTicks = itemView.findViewById(R.id.image_view_blue_ticks);
            imageViewGreyTicks = itemView.findViewById(R.id.image_view_grey_ticks);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (mChat.get(position).getIsSender() && mChat.get(position).getMessageType().equals("text")) {
            isSender = true;
            return MSG_TYPE_RIGHT;

        } else if (!mChat.get(position).getIsSender() && mChat.get(position).getMessageType().equals("text")) {
            isSender = false;
            return MSG_TYPE_LEFT;

        } else if (mChat.get(position).getIsSender() && mChat.get(position).getMessageType().equals("image")) {
            isSender = true;
            return MSG_TYPE_IMAGE_RIGHT;

        } else if (!mChat.get(position).getIsSender() && mChat.get(position).getMessageType().equals("image")) {
            isSender = false;
            return MSG_TYPE_IMAGE_LEFT;
        } else if (mChat.get(position).getIsSender() && mChat.get(position).getMessageType().equals("attachment")) {
            isSender = true;
            return MSG_TYPE_ATTACHMENT_RIGHT;
        } else {
            isSender = false;
            return MSG_TYPE_ATTACHMENT_LEFT;
        }
    }

    public static String getDate(long milliSeconds, String dateFormat) {

        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void getImage(String imageUrl, String fileName) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = getBitmapFromURL(imageUrl);

                //External directory path to save file
                String folder = Environment.getExternalStorageDirectory() + File.separator + "TAC/Images/";
                //Create folder if it does not exist
                File directory = new File(folder);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                try {
                    FileOutputStream out = new FileOutputStream(folder + fileName + ".jpg");
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    }
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public static Bitmap getBitmapFromURL(String src) {

        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean fileExists(String fileName) {

        String filePath = Environment.getExternalStorageDirectory() + File.separator + "TAC/Images/" + fileName + ".jpg";

        File file = new File(filePath);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private void loadLocally(String fileName, ImageView imageView) {

        String filePath = Environment.getExternalStorageDirectory() + File.separator + "TAC/Images/" + fileName + ".jpg";

        File file = new File(filePath);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<GroupMessage> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mChatFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (GroupMessage message : mChatFull) {
                    if (message.getMessage().toLowerCase().contains(filterPattern)) {
                        filteredList.add(message);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mChat.clear();
            mChat.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
