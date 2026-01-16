package com.example.project;
import static com.example.project.MainActivity.account;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class OneContactActivity extends AppCompatActivity {
    private  Contact contact;
    TextView nameTextView;
    TextView numberTextView;
    String contactNumber;
    String contactName;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101;
    private static final int PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_contact);

        Bundle arguments = getIntent().getExtras();

        if (arguments != null) {
            contact = (Contact) arguments.getSerializable(Contact.class.getSimpleName());

            contactName = contact.getName();
            nameTextView = findViewById(R.id.nameOneContact);
            nameTextView.setText(contactName);

            contactNumber = contact.getNumber();
            numberTextView = findViewById(R.id.numberOneContact);
            numberTextView.setText(contactNumber);
        }
    }

    public void makeCall(View view) {
        call();
    }

    public void call() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
            } else {
                MyCall call = new MyCall(account, -1);
                try {
                    call.makeCallSip(contact.getNumber(), contact.getName());
                }
                catch (Exception e) {
                    call.delete();
                }
            }
        } else {
            MyCall call = new MyCall(account, -1);
            try {
                call.makeCallSip(contact.getNumber(), contact.getName());
            }
            catch (Exception e) {
                call.delete();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Для записи аудио необходимо разрешение на использование микрофона", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

