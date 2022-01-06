package com.example.memorymap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent repeatingIntent = new Intent(context, MainActivity.class);
        repeatingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, repeatingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        DatabaseAccess dbAccess = DatabaseAccess.getInstance(context);
        dbAccess.open();

        List<String> loc = dbAccess.getInfoColumn(0);
        Random random = new Random();
        int i = random.nextInt(loc.size());

        String title = loc.get(i);
        String triggers = dbAccess.getInfoColumn(7).get(i);
        if (triggers == "null"){
            triggers = "No triggers inputed";
        }
        dbAccess.close();

        Notification notification = new NotificationCompat.Builder(context, App.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText("Prasang Triggers: " + triggers)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();
        notificationManager.notify(0, notification);
    }
}

