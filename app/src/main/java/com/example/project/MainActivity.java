package com.example.project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

class MSG_TYPE
{
    public final static int INCOMING_CALL = 1;
    public final static int CALL = 0;
    public final static int CALL_CONFIRMED = 2;
    public final static int CALL_DISCONNECTED = 3;
}

public class MainActivity extends AppCompatActivity {
    public static boolean isConfStart = false;
    public static ArrayList<Contact> contacts = null;
    public static ArrayList<CallForList> calls = null;
    public static ArrayList<ArrayList<Contact>> confContactsList;
    public static ArrayList<MyCall> confCallsList;
    public static ArrayList<Contact> confContacts = null;
    public static ArrayList<MyCall> confCalls = null;
    public static ArrayList<Boolean> statusCalls = null;
    public static ArrayList<Boolean> statusConfs = null;
    public static boolean isCall = false;
    public static Menu menu1;
    public static CallAdapter adapter;
    private static boolean p = false;

    public static String phoneUser;
    public static final String URL_SERVER = "192.168.1.14";
    public static MyCall currentCall = null;
    public static MyAccount account = null;
    public static MyEndpoint ep = null;
    public static ArrayList<ArrayList<CallForList>> groupedCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.buttonCall);

        if (checkFirstTime()) {
            // Если пользователь зашел впервые, предлагаем ему зарегистрироваться
            // Выводим страницу с предложением регистрации
            startRegActivity();
            finish();
        }
        else {
            if (MainActivity.contacts == null) {
                DatabaseHelper databaseHelper = new DatabaseHelper(this);
                databaseHelper.getContacts();
            }

            setPhoneNumber();
            startService();

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение для создания уведомлений не предоставлено", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_SIP) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_SIP}, 7);
            }

            if (calls == null) {
                DatabaseHelper databaseHelper = new DatabaseHelper(this);
                databaseHelper.getCalls();
            }
        }

        setCallAdapter();
    }

    private void setCallAdapter() {
        // получаем элемент ListView
        RecyclerView listCalls = findViewById(R.id.callListRecycler);
        // Создание экземпляра DividerItemDecoration с переданным контекстом и ориентацией
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listCalls.getContext(), LinearLayoutManager.VERTICAL);
        // Присваивание разделителя к RecyclerView
        listCalls.addItemDecoration(dividerItemDecoration);

        listCalls.setLayoutManager(new LinearLayoutManager(this));

        CallAdapter.OnButtonClickListener buttonClickListener = new CallAdapter.OnButtonClickListener() {
            @Override
            public void onButtonClicked(int position) {
                //////ИСПРАВИТЬ
                if (MainActivity.groupedCalls.get(position).size() == 1) {
                    String number = MainActivity.groupedCalls.get(position).get(0).getNumberCall();
                    String name = MainActivity.groupedCalls.get(position).get(0).getNameCall();

                    if (name == null) {
                        call(number, null);
                    }
                    else {
                        call(number, name);
                    }
                }
                else {
                    try {
                        ArrayList<Contact> contacts1 = new ArrayList<>();
                        for (int i = 0; i < MainActivity.groupedCalls.get(position).size(); i++) {
                            Contact contact = new Contact(MainActivity.groupedCalls.get(position).get(i).getNameCall(),
                                    MainActivity.groupedCalls.get(position).get(i).getNumberCall());
                            contacts1.add(contact);
                            //confContacts.add(contact);
                        }

                        int idCall = 0;

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                        if (statusConfs != null) {
                            if (statusConfs.get(position)) {
                                int finalIdCall = idCall;
                                alertDialogBuilder
                                        .setMessage("Конференция ещё идёт. Хотите ли присоединиться?")
                                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                try {
                                                    confContacts = contacts1;
                                                    String uri = "sips:" + MyAccount.confUri.get(finalIdCall).getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
                                                    String message = "adduser";
                                                    account.sendAddUser(uri, message);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        })
                                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            } else {
                                alertDialogBuilder
                                        .setMessage("Конференция была завершена. Хотите ли создать новую конференцию с данными пользователями?")
                                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                MyCall call = new MyCall(account, -1);
                                                try {
                                                    call.makeCallSip(contacts1);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        })
                                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // Обработка нажатия на кнопку "Нет"
                                                //Если нет, то ничего не делать
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                                System.out.println("Конференция была завершена. Хотите ли создать новую конференцию с данными пользователями?");
                            }
                        }
                            else {
                                alertDialogBuilder
                                        .setMessage("Конференция была завершена. Хотите ли создать новую конференцию с данными пользователями?")
                                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                MyCall call = new MyCall(account, -1);
                                                try {
                                                    call.makeCallSip(contacts1);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        })
                                        .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                            }
                                        });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                                System.out.println("Конференция была завершена. Хотите ли создать новую конференцию с данными пользователями?");
                            }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        };

        //определяем слушателя нажатия элемента в списке
        CallAdapter.OnCallClickListener callClickListener = new CallAdapter.OnCallClickListener() {
            @Override
            public void onCallClick(ArrayList<CallForList> call, int position) {
                startActivityOneCall(call);
            }
        };

        CallAdapter.OnLongCallClickListener callLongClickListener = new CallAdapter.OnLongCallClickListener() {
            @Override
            public void onLongCallClick(ArrayList<CallForList> call, int position) {
                // Получаем текущий ActionBar
                ActionBar actionBar = getSupportActionBar();

                p = true;

                MenuItem item = menu1.findItem(R.id.action_remove);
                if(item != null){
                    item.setVisible(true);
                }

                item = menu1.findItem(R.id.action_select_all);
                if(item != null){
                    item.setVisible(true);
                }

                item = menu1.findItem(R.id.action_settings);
                if (item != null){
                    item.setVisible(false);
                }
            }
        };

        if (calls != null) {
            // создаем адаптер
            adapter = new CallAdapter(this, callClickListener, callLongClickListener, buttonClickListener);
            // устанавливаем для списка адаптер
            listCalls.setAdapter(adapter);
        }
    }


    private void startActivityOneCall(ArrayList<CallForList> call) {
        Intent intent = new Intent(this, SelectCallActivity.class);
        intent.putExtra(ArrayList.class.getSimpleName(), call);
        startActivity(intent);
    }
    public static MyEndpoint getMyEndpoint() {
        return ep;
    }
    public void startService() {
        Intent i = new Intent(this, ServiceCallbackCall.class);
        ContextCompat.startForegroundService(this, i);
    }

    public boolean checkFirstTime(){
        SharedPreferences sharedPreferences = getSharedPreferences("my_pref", Context.MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("is_first_time", true);

        return isFirstTime;
    }

    public void startActivityListContacts(View view) throws Exception {
        Intent intent = new Intent(this, ListContactsActivity.class);
        startActivity(intent);
    }

    public void startRegActivity() {
        Intent intent = new Intent(this, RegActivity.class);
        startActivity(intent);
    }

    public void setPhoneNumber() {
        SharedPreferences sharedPreferences = getSharedPreferences("data_user", Context.MODE_PRIVATE);
        phoneUser = sharedPreferences.getString("phoneNumber", null);
    }

    //Как правильно осуществлять сетевые операции в приложении с потоками

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu1 = menu;
        return true;
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_select_all) {
            adapter.selectAllItems();
        }

        if (id == R.id.action_remove) {
            adapter.deleteSelectedItems(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (p == true) {
            adapter.clearSelectedItems();
            setCallAdapter();
        }
        else {
            super.onBackPressed();
        }
    }

    public static void finishActivity() {
        if (p == true) {
            MenuItem item = menu1.findItem(R.id.action_remove);
            if (item != null) {
                item.setVisible(false);
            }

            item = menu1.findItem(R.id.action_select_all);
            if (item != null) {
                item.setVisible(false);
            }

            item = menu1.findItem(R.id.action_settings);
            if (item != null) {
                item.setVisible(true);
            }
            p = false;
        }
    }

    public void call(String uri, String name) {
        MyCall call = new MyCall(account, -1);
        if (name != null) {
            call.makeCallSip(uri, name);
        }
        else {
            call.makeCallSip(uri, null);
        }
        call.delete();
    }
}