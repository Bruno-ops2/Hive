package com.nullparams.hive.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nullparams.hive.ChatActivity;
import com.nullparams.hive.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private Context context = this;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().get("title") == null) {

            String messageTitle = remoteMessage.getNotification().getTitle();
            String messageBody = remoteMessage.getNotification().getBody();

            sendCloudMessagingNotification(messageTitle, messageBody);

        } else {

            SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
            String chatUserId = sharedPreferences.getString("chatUserId", "none");

            String messageTitle = remoteMessage.getData().get("title");
            String messageBody = remoteMessage.getData().get("body");
            String clickAction = remoteMessage.getData().get("click_action");
            String sender = remoteMessage.getData().get("sender");

            if (!chatUserId.equals(remoteMessage.getData().get("sender"))) {
                sendNotification(messageTitle, messageBody, clickAction, sender);
            }
        }
    }

    private void sendCloudMessagingNotification(String title, String body) {

        String url = "https://play.google.com/store/apps/details?id=com.nullparams.hive";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(Uri.parse(url));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "General Notifications";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setColor(getResources().getColor(R.color.Accent))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }

    private void sendNotification(String title, String body, String clickAction, String sender) {

        if (clickAction.equals("com.nullparams.hive.FirebasePushNotifications.TARGETNOTIFICATIONCHAT")) {

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", sender);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            String channelId = "General Notifications";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId);

            notificationBuilder.setSmallIcon(R.drawable.ic_notification);
            notificationBuilder.setContentTitle(title);

            if (body.equals("")) {
                notificationBuilder.setContentText("Media");
            } else {
                notificationBuilder.setContentText(body);
            }

            notificationBuilder.setColor(getResources().getColor(R.color.Accent));
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setSound(defaultSoundUri);
            notificationBuilder.setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
