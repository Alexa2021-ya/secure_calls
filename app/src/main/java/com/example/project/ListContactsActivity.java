package com.example.project;

import static com.example.project.MainActivity.account;
import static com.example.project.MainActivity.confContactsList;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import java.sql.SQLException;
import java.util.ArrayList;

import android.Manifest;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ListContactsActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_CONTACTS = 1;
    public static boolean READ_CONTACTS_GRANTED = false;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 101;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static boolean p = false;
    static Menu menu1;

    ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contacts);

        setContactAdapter();

        checkPermissions();
    }

    private void setContactAdapter() {
        RecyclerView listContacts = findViewById(R.id.contactListRecycler);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listContacts.getContext(), LinearLayoutManager.VERTICAL);
        listContacts.addItemDecoration(dividerItemDecoration);
        ContactAdapter.OnContactClickListener contactClickListener = new ContactAdapter.OnContactClickListener() {
            @Override
            public void onContactClick(Contact contact, int position) {
                startActivityOneContact(contact);
            }
        };

        ContactAdapter.OnLongContactClickListener contactLongClickListener = new ContactAdapter.OnLongContactClickListener() {
            @Override
            public void onLongContactClick(Contact contact, int position) {
                ActionBar actionBar = getSupportActionBar();

                p = true;

                MenuItem item = menu1.findItem(R.id.action_call);
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

        if (MainActivity.contacts != null) {
            adapter = new ContactAdapter(this, contactClickListener, contactLongClickListener);
            listContacts.setAdapter(adapter);

        }
    }

    public void fillList() {
        if (MainActivity.contacts == null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            databaseHelper.getContacts();
            adapter.notifyDataSetChanged();
        }

        hideProgressBar();
    }

    public void startActivityOneContact(Contact contact){
        Intent intent = new Intent(this, OneContactActivity.class);
        intent.putExtra(Contact.class.getSimpleName(), contact);
        startActivity(intent);
    }

    private void showProgressBar(){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (progressBar != null){
            progressBar.setVisibility(View.VISIBLE);
        }

        Button buttonUpdate = findViewById(R.id.buttonUpdateListContacts);
        buttonUpdate.setEnabled(false);

        RecyclerView listContacts = findViewById(R.id.contactListRecycler);
        listContacts.setVisibility(View.INVISIBLE);
        listContacts.setEnabled(false);
    }

    private void hideProgressBar(){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if(progressBar != null){
            progressBar.setVisibility(View.INVISIBLE);
        }

        Button buttonUpdate = findViewById(R.id.buttonUpdateListContacts);
        RecyclerView listContacts = findViewById(R.id.contactListRecycler);

        listContacts.setVisibility(View.VISIBLE);
        buttonUpdate.setEnabled(true);
        listContacts.setEnabled(true);

        adapter.notifyDataSetChanged();
    }

    public void updateListContact(View view) {
        if (READ_CONTACTS_GRANTED) {
            showProgressBar();
            Contact contact;

            ContentResolver contentResolver = getContentResolver();
            contact = new Contact(contentResolver);


            if (MainActivity.contacts != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DBConnector db = new DBConnector();
                        Looper.prepare();
                        if (db.getConnectionDB() == null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    fillList();
                                    Toast.makeText(getApplicationContext(), "Возникли проблемы при подключении к серверу", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else {
                            try {
                                contact.getContactsList();
                                db.showUsers(getApplicationContext());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        fillList();
                                        if (MainActivity.contacts.size() == 0) {
                                            Toast.makeText(getApplicationContext(), "Не найдено совпадений", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (SQLException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }).start();
            }
        }
        else Toast.makeText(getApplicationContext(), "Требуется установить разрешения", Toast.LENGTH_LONG).show();
    }

    public void checkPermissions() {
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if(hasReadContactPermission == PackageManager.PERMISSION_GRANTED){
            READ_CONTACTS_GRANTED = true;
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }
    }


    public static void finishSelectConf() {
        if (p == true) {
            MenuItem item = menu1.findItem(R.id.action_call);
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

    @Override
    public void onBackPressed() {
        if (p == true) {
            adapter.clearSelectedItems();
            setContactAdapter();
        }
        else {
            super.onBackPressed();
        }
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

        if (id == R.id.action_call) {
            try {
                startConf();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void startConf() throws Exception {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        } else {
            if (adapter.selectedItems.size() > 1) {
                MyCall call = new MyCall(account, -1);

                ArrayList<Contact> contacts = new ArrayList<Contact>();
                for (Integer position : adapter.selectedItems) {
                    Contact contact = MainActivity.contacts.get(position);
                    contacts.add(contact);
                }

                if (confContactsList == null) {
                    confContactsList = new ArrayList<>();
                }

                call.makeCallSip(contacts);
            }
            else {
                MyCall call = new MyCall(account, -1);
                for (Integer position : adapter.selectedItems) {
                    Contact contact = MainActivity.contacts.get(position);
                    call.makeCallSip(contact.getNumber(), contact.getName());
                }
            }
        }

        adapter.clearSelectedItems();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu1 = menu;
        return true;
    }
}
