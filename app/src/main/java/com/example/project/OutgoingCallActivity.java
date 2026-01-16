package com.example.project;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.currentCall;
import static com.example.project.MainActivity.isConfStart;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OutgoingCallActivity extends AppCompatActivity {

    public static Handler handler;
    public static boolean isOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_call);
        TextView nameCallee = findViewById(R.id.calleeName);

        if (isConfStart) {
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
                        try {
                            finishCall();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (msg.what == MSG_TYPE.CALL_CONFIRMED) {
                        finish();
                    }
                }
            };
    }

    public void finishOutCall(View view) throws Exception {
        if (currentCall != null) {
            currentCall.hangupCall();
        }
        else {
            for (int i = 0; i < MainActivity.confCalls.size(); i++) {
                if (MainActivity.confCalls.get(i) != null) {
                    MainActivity.confCalls.get(i).hangupCall();
                }
            }
            MainActivity.confCalls.clear();
        }
    }

    public void finishCall() throws Exception {
        Toast.makeText(this, "Звонок завершён", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
