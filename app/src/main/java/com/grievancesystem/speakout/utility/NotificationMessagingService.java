package com.grievancesystem.speakout.utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.grievancesystem.speakout.Database.Prefs;
import com.grievancesystem.speakout.R;
import com.grievancesystem.speakout.activities.SplashActivity;
import com.grievancesystem.speakout.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NotificationMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "notification_liveTask";
    private static final String CHANNEL_NAME = "Current Complaints Status";
    private static final String CHANNEL_DESCRIPTION = "Notifications for Complaints";
    private static final String TAG = "FCM_Notification";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String title = null;
        String message = null;
        String profile_uri = null;
        boolean isBlocked = false;
        boolean setLoggedOut = false;

        Map<String, String> data = remoteMessage.getData();
        Log.d(TAG, "remoteMessage.getData() : " + remoteMessage.getData());

        JSONObject jsnObject = new JSONObject(data);
        try {
            title = jsnObject.getString("title");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            message = jsnObject.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            profile_uri = jsnObject.getString("profile_uri");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            isBlocked = jsnObject.getBoolean("isBlocked");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            setLoggedOut = jsnObject.getBoolean("setLoggedOut");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (setLoggedOut || isBlocked) {
            Helper.signOutUser(this, true);
        }

        if (title == null || title.isEmpty()) title = "You have New Notifications";
        if (message == null || message.isEmpty()) message = "Tap to open the app";
        sendNotification(title, message, profile_uri);
    }

    public void sendNotification(String messageTitle, String messageBody, String profile_uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.app_logo)
                        .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                R.drawable.app_logo))
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentIntent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(messageTitle);
        bigTextStyle.bigText(messageBody);
        notificationBuilder.setStyle(bigTextStyle);

        NotificationManagerCompat notificationManagerCompat =
                NotificationManagerCompat.from(this);

        // Since android Oreo notification channel is needed.

        int unique_id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManagerCompat.notify(unique_id, notificationBuilder.build());
        Log.d(TAG, "sendNotification = run");
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
        saveTokenToServer(s);
    }

    private void saveTokenToServer(String token) {
        User user = Prefs.getUser(this);
        if (user == null || user.getUsername() == null)
            return;

        DatabaseReference databaseReference;
        if (user.getUserType() == Helper.USER_STUDENT)
            databaseReference = FirebaseDatabase.getInstance().getReference("StudentUsers");
        else databaseReference = FirebaseDatabase.getInstance().getReference("AdminUsers");

        HashMap<String, Object> data = new HashMap<>();
        data.put("fcm_token", token);
        data.put("isLoggedIn", true);
        databaseReference.child(user.getUid()).updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // successfully saved
                    Prefs.getUser(getApplicationContext()).setFcm_token(token);
                } else {
                    // error
                }
            }
        });
    }
}
