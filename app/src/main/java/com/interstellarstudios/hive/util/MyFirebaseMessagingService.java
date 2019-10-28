package com.interstellarstudios.hive.util;

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
import com.interstellarstudios.hive.ChatActivity;
import com.interstellarstudios.hive.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private Context context = this;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

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

    private void sendNotification(String title, String body, String clickAction, String sender) {

        if (clickAction.equals("com.interstellarstudios.hive.FirebasePushNotifications.TARGETNOTIFICATIONCHAT")) {

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("userId", sender);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
    }
}
