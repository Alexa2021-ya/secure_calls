package com.example.project;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
    }

    public void save(View view) {
        TextView codeNumber = findViewById(R.id.numberCode);
        EditText number = findViewById(R.id.phoneNumberReg);
        String phoneNumber = number.getText().toString();
        if (isPhoneNumber(phoneNumber)) {
            String finalPhoneNumber = codeNumber.getText().toString() + phoneNumber;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DBConnector db = new DBConnector();
                    Looper.prepare();
                    if (db.getConnectionDB() == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Возникли проблемы при подключении к серверу", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        try {
                            if (db.isUser(finalPhoneNumber)) {

                            }
                            else {
                                db.addUser(finalPhoneNumber, finalPhoneNumber, finalPhoneNumber);
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        SharedPreferences sharedPreferences = getSharedPreferences("data_user", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("phoneNumber", finalPhoneNumber);
                        editor.apply();

                        sharedPreferences = getSharedPreferences("my_pref", Context.MODE_PRIVATE);
                        editor = sharedPreferences.edit();
                        editor.putBoolean("is_first_time", false);
                        editor.apply();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                            }
                        });

                        restartApp();
                        finish();
                    }
                }
            }).start();
        }
        else {
            Toast.makeText(this, "Некорректный номер телефона", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isPhoneNumber(String phoneNumber) {
        String regex = "\\d{10}";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phoneNumber);

        if (matcher.matches()) {
            return true;
        }

        return false;
    }

    public void restartApp() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
