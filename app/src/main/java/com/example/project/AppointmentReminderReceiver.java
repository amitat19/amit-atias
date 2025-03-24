package com.example.project;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AppointmentReminderReceiver extends BroadcastReceiver {

    Context context;

    public void onReceive(Context context, Intent intent) {

        this.context = context;
        // קבלת הנתונים מה-Intent
        String title = intent.getStringExtra("title");
        String time = intent.getStringExtra("time");

        createNotification(context, title, time);
    }

    private void createNotification(Context context, String title, String time) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = new NotificationCompat.Builder(context, "appointment_channel")
                .setSmallIcon(R.drawable.map_ic) // TODO ודא שיש לך אייקון תקף
                .setContentTitle("תזכורת לתור!")
                .setContentText("יש לך תור ל-" + title + " מחר בשעה " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}