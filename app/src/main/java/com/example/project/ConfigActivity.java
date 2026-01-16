package com.example.project;
import android.content.Context;
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

public class ConfigActivity extends AppCompatActivity {

    EditText name;
    TextView codeNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        name = findViewById(R.id.nameUserConfig);
        name.setText(MainActivity.phoneUser.substring(2));
    }

    public void updateUser(View view) throws SQLException {
        String nameUser = name.getText().toString();
        DBConnector db = new DBConnector();
        if (isPhoneNumber(nameUser)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
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
                        String finalPhoneNumber = codeNumber.getText().toString() + nameUser;
                        try {
                            db.updateUser(MainActivity.phoneUser, finalPhoneNumber, finalPhoneNumber, finalPhoneNumber);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                        SharedPreferences sharedPreferences = getSharedPreferences("data_user", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("phoneNumber", finalPhoneNumber);
                        editor.apply();

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Данные обновлены", Toast.LENGTH_SHORT).show();
                                MainActivity.phoneUser = finalPhoneNumber;
                            }
                        });
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
}
