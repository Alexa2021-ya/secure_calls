package com.example.project;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.currentCall;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class IncomingCallActivity extends AppCompatActivity {
    public static Handler handler;
    private NotificationManager notificationManager;
    public static boolean isIncoming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        TextView nameCallee = findViewById(R.id.callerInfo);
        boolean isConf = false;
        if (confContacts != null) {
            if (confContacts.size() > 0) {
                isConf = true;
            }
        }

        if (isConf) {
            String callee = "";
            for (int i = 0; i < confContacts.size(); i++) {
                callee += confContacts.get(i).getName() + "\n";
            }
            nameCallee.setText(callee);
        }
        else {
            nameCallee.setText(ServiceCallbackCall.getCurrentCallName());
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE.CALL_DISCONNECTED) {
                    ServiceCallbackCall.deleteNotification(ServiceCallbackCall.NOTIFICATION_INCOMING_CALL_ID, notificationManager);

                    finish();
                }
            }
        };
    }

    public void onAnswer(View view) throws Exception {
        ServiceCallbackCall.deleteNotification(ServiceCallbackCall.NOTIFICATION_INCOMING_CALL_ID, notificationManager);

        try {
            currentCall.answerCall();
        } catch (Exception e) {
            currentCall.hangupCall();
            throw new RuntimeException(e);
        }
        finish();
    }

    public void onHangup(View view) throws Exception {
        ServiceCallbackCall.deleteNotification(ServiceCallbackCall.NOTIFICATION_INCOMING_CALL_ID, notificationManager);
        currentCall.hangupCall();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
