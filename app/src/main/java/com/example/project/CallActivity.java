package com.example.project;
import android.content.Intent;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.currentCall;
import static com.example.project.MainActivity.isCall;
import static com.example.project.MainActivity.isConfStart;
import static com.example.project.MyCall.isConfirmed;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Timer;
import java.util.TimerTask;

public class CallActivity extends AppCompatActivity {
    public static Handler handler;
    private static Timer timer;
    public static boolean isCallStart = false;
    public static TextView timerView;
    private static String timeText;
    public static int seconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_call);

        TextView name = findViewById(R.id.subscriberInfo);
        if (confContacts != null && confContacts.size() > 0) {
            name.setText(R.string.title_group_call);
            Button buttonListContacts = findViewById(R.id.buttonConfContacts);
            buttonListContacts.setVisibility(View.VISIBLE);

            if (isConfStart) {
                Button buttonAddUser = findViewById(R.id.addUser);
                buttonAddUser.setVisibility(View.VISIBLE);
            }
        }
        else {
            name.setText(ServiceCallbackCall.getCurrentCallName());
        }

        timerView = findViewById(R.id.timeInfo);
        timerView.setText(timeText);


        getColorDinamic();
        getColorMicro();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE.CALL_DISCONNECTED) {
                    if (ServiceCallbackCall.audioManager != null) {
                        finish();
                    }
                }
            }
        };
    }

    private void getColorMicro() {
        Button btnMicro = findViewById(R.id.buttonMuteMicrophone);
        if (ServiceCallbackCall.audioManager.isMicrophoneMute()) {
            btnMicro.setBackgroundColor(getResources().getColor(R.color.red));
        }
        else {
            btnMicro.setBackgroundColor(getResources().getColor(R.color.green));
        }
    }

    private void getColorDinamic() {
        Button btnDinamic = findViewById(R.id.buttonMuteDinamic);
        if (ServiceCallbackCall.audioManager.isSpeakerphoneOn()) {
            btnDinamic.setBackgroundColor(getResources().getColor(R.color.green));
        }
        else {
            btnDinamic.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }



    public void finishCall(View view) throws Exception {
        if (currentCall != null) {
            currentCall.hangupCall();
        }
        else {
            for (int i = 0; i < MainActivity.confCalls.size(); i++) {
                if (MainActivity.confCalls.get(i) != null) {
                    MainActivity.confCalls.get(i).hangupCall();
                }
            }
        }
    }

    public void setMicrophone(View view) {
        if (ServiceCallbackCall.audioManager.isMicrophoneMute()) {
            ServiceCallbackCall.audioManager.setMicrophoneMute(false);
        }
        else {
            ServiceCallbackCall.audioManager.setMicrophoneMute(true);
        }
        getColorMicro();
    }

    public void setDinamic(View view) {
        if (ServiceCallbackCall.audioManager.isSpeakerphoneOn()) {
            ServiceCallbackCall.audioManager.setSpeakerphoneOn(false);
        }
        else {
            ServiceCallbackCall.audioManager.setSpeakerphoneOn(true);
        }
        getColorDinamic();
    }

    public static void startTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    seconds++;
                    int minutes = seconds / 60;
                    int secs = seconds % 60;

                    String time = String.format("%02d:%02d", minutes, secs);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (timerView != null) {
                                timeText = time;
                                timerView.setText(timeText);
                            }
                        }
                    });

                } catch (Exception e) {
                    System.out.println("Exception in run method: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }





        public static void stopTimer() {
            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
                seconds = 0;  // сброс счетчика времени
            }
    }

    public void addUserCall(View view) {
        Intent intent = new Intent(this, AddUserActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    public void getListContactsCall(View view) {
        Intent intent = new Intent(this, ListContactsCall.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
