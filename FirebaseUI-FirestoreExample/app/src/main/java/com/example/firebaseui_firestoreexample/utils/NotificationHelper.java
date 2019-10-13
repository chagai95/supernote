package com.example.firebaseui_firestoreexample.utils;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;

import com.example.firebaseui_firestoreexample.R;
import com.example.firebaseui_firestoreexample.receivers.MyBroadcastReceiver;
import com.example.firebaseui_firestoreexample.receivers.NotificationReceiver;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class NotificationHelper {

    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;

    public NotificationHelper(Context mContext) {
        this.mContext = mContext.getApplicationContext();
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    /**
     * Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in
     * the support library
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void initNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        for (NotificationChannel channel : NotificationChannels.getChannels()) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void createNotification(String title,
                                   String content,
                                   PendingIntent notifyIntent,
                                   DocumentReference reminderDocumentReference) {


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(mContext, NotificationReceiver.class);
        intent.setAction("swiped");
        intent.putExtra("reminderID",reminderDocumentReference.getId());
        CollectionReference coll = reminderDocumentReference.getParent();
        DocumentReference documentReference = coll.getParent();
        assert documentReference != null;
        intent.putExtra("noteID",documentReference.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, intent, 0);



        mBuilder = new NotificationCompat.Builder(mContext, "CHANNEL_ID");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notifyIntent)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setDeleteIntent(pendingIntent); // this gets triggered when we dismiss (swipe away) the notification or click clear all in the notification panel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId("CHANNEL_ID");
        }
    }

    public void createNotificationForWhatsapp(String title,
                                              String content,
                                              String whatsappNumber,
                                              PendingIntent notifyIntent) {


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intentSwiped = new Intent(mContext, NotificationReceiver.class);
        intentSwiped.setAction("swiped");//we use the action to check if the notification has been dismissed in MyBroadcastReceiver
        intentSwiped.putExtra("whatsappMessage",content);
        intentSwiped.putExtra("whatsappNumber",whatsappNumber);
        PendingIntent pendingIntentSwiped = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, intentSwiped, 0);


        Intent intentButton = new Intent(mContext, NotificationReceiver.class);
        String whatsappSendButton;
        if (isNetworkAvailable()) {
            whatsappSendButton = mContext.getString(R.string.sendWhatsapp);
            intentButton.setAction("sendWhatsapp");
            intentButton.putExtra("whatsappNumber",whatsappNumber);
            intentButton.putExtra("whatsappMessage",content);
        } else {
            whatsappSendButton = mContext.getString(R.string.snoozeWhatsapp);
            intentButton.setAction("snoozeWhatsapp");
            intentButton.putExtra("whatsappNumber",whatsappNumber);
            intentButton.putExtra("whatsappMessage",content);
        }
        PendingIntent pendingIntentButton =
                PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, intentButton, 0);

        mBuilder = new NotificationCompat.Builder(mContext, "CHANNEL_ID");
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notifyIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_save, whatsappSendButton, pendingIntentButton)
                .setSound(alarmSound)
                .setDeleteIntent(pendingIntentSwiped); // this gets triggered when we dismiss (swipe away) the notification or click clear all in the notification panel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setLargeIcon(null);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId("CHANNEL_ID");
        }
    }

    @SuppressWarnings("unused")
    public Builder getBuilder() {
        return mBuilder;
    }

    @SuppressWarnings("unused")
    public NotificationHelper setVibration(long[] pattern) {
        if (pattern == null || pattern.length == 0) {
            pattern = new long[]{500, 500};
        }
        mBuilder.setVibrate(pattern);
        return this;
    }

    public void show(int id) {
        mNotificationManager.notify(id, mBuilder.build());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null; //added to avoid warning
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}