package com.example.project;
import static com.example.project.MainActivity.account;
import static com.example.project.MainActivity.confCalls;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.isConfStart;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ListContactsCall extends AppCompatActivity {
    public static boolean p = false;
    public static Handler handler;
    CustomAdapter adapter;
    static Menu menu1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contacts_call);
        setConfContactAdapter();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE.CALL_DISCONNECTED) {
                    finish();
                }
            }
        };
    }

    private void setConfContactAdapter() {
        ListView list = findViewById(R.id.listContactsConf2);
        List<String> contacts = new ArrayList<>();
        for (int i = 0; i < confContacts.size(); i++) {
            contacts.add(confContacts.get(i).getName());
        }
        adapter = new CustomAdapter(this, android.R.layout.simple_list_item_1, contacts);
        list.setAdapter(adapter);
    }

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

        if (id == R.id.action_delete_call) {
            try {
                deleteContactsCall();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public static void finishSelect() {
        if (p == true) {
            MenuItem item = menu1.findItem(R.id.action_delete_call);
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

    private void deleteContactsCall() throws Exception {
        for (Integer position : adapter.selectedItems) {
            confContacts.remove(confContacts.get(position));
            confCalls.get(position).hangupCall();
            confCalls.remove(confCalls.get(position));

            for (int i = 0; i < confContacts.size(); i++) {
                String message = "DELETE:" + position;
                String uri = "sips:" + confContacts.get(i).getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
                account.sendMessageDeleteContact(uri, message);
            }
        }

        if (isConfStart) {
            CustomAdapter.selectedItems.clear();
            setConfContactAdapter();
            finishSelect();
        }
    }

    @Override
    public void onBackPressed() {
        if (p == true) {
            adapter.selectedItems.clear();
            adapter.notifyDataSetChanged();
            //setConfContactAdapter();
            finishSelect();
        }
        else {
            super.onBackPressed();
        }
    }
}
