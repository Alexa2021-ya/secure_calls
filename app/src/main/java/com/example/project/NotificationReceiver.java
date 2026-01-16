package com.example.project;
import static com.example.project.MainActivity.currentCall;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("ACTION_ANSWER_CALL".equals(action)) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                ServiceCallbackCall.deleteNotification(ServiceCallbackCall.NOTIFICATION_INCOMING_CALL_ID, notificationManager);

                currentCall.answerCall();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if ("ACTION_HANGUP_CALL".equals(action)) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                StatusBarNotification[] activeNotifications = new StatusBarNotification[0];
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    activeNotifications = notificationManager.getActiveNotifications();
                }

                if (activeNotifications.length > 0) {
                    int notificationId = activeNotifications[0].getId();
                    notificationManager.cancel(notificationId);
                }


                if (currentCall != null) {
                    currentCall.hangupCall();
                }
                else {
                    for (int i = 0; i < MainActivity.confCalls.size(); i++) {
                       MainActivity.confCalls.get(i).hangupCall();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}