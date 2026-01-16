package com.example.project;
import static com.example.project.MainActivity.account;
import static com.example.project.MainActivity.confCalls;
import static com.example.project.MainActivity.confContacts;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.pjsip.pjsua2.CallOpParam;
import java.util.ArrayList;
import java.util.List;

public class AddUserActivity extends AppCompatActivity {
    CustomAdapterContacts adapter;
    public static Menu menu1;
    public static boolean p = false;
    public static Handler handler;
    List<String> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        setContactAdapter();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_TYPE.CALL_DISCONNECTED) {
                    finish();
                }
            }
        };
    }

    private void setContactAdapter() {
        ListView listContacts = findViewById(R.id.contactsCallList);
        contacts = new ArrayList<>();

        for (int i = 0; i < MainActivity.contacts.size(); i++) {
            boolean isContact = true;
            for (int j = 0; j < confContacts.size(); j++) {
                if (MainActivity.contacts.get(i).equals(confContacts.get(j))) {
                    isContact = false;
                    break;
                }
            }
            if (isContact) {
                contacts.add(MainActivity.contacts.get(i).getName());
            }
        }
        adapter = new CustomAdapterContacts(this, android.R.layout.simple_list_item_1, contacts);
        listContacts.setAdapter(adapter);
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

        if (id == R.id.action_call) {
            try {
                call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void call() throws Exception {
        for (Integer item : adapter.selectedItems) {
            Contact contact = null;
            for (int i = 0; i < MainActivity.contacts.size(); i++) {
                if (MainActivity.contacts.get(i).getName() == contacts.get(item)) {
                    contact = MainActivity.contacts.get(i);
                }
            }

            if (contact != null) {
                MyCall call = new MyCall(account, -1);
                CallOpParam callOpParam = new CallOpParam(true);
                String uri = "sips:" + contact.getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
                MyCall.isAddContactConf = true;
                call.makeCall(uri, callOpParam);

                for (int i = 0; i < confContacts.size(); i++) {
                    String message = "ADD:" + contact.getNumber();
                    String uriContact = "sips:" + confContacts.get(i).getNumber() + "@" + MainActivity.URL_SERVER + ";transport=tls";
                    account.sendMessageAddContact(uriContact, message);
                }

                confContacts.add(contact);
                confCalls.add(call);

                String message = "Conf:";
                for (Contact contact1 : confContacts) {
                    message = message + contact1.getNumber() + "\n";
                }
                account.sendMessageConf(uri, message);
            }
        }

        setContactAdapter();
        CustomAdapterContacts.selectedItems.clear();
        adapter.notifyDataSetChanged();
        finishSelect();
    }

    public static void finishSelect() {
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
            adapter.selectedItems.clear();
            adapter.notifyDataSetChanged();
            finishSelect();
        }
        else {
            super.onBackPressed();
        }
    }
}
