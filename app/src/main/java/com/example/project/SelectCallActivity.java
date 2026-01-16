package com.example.project;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class SelectCallActivity extends AppCompatActivity {
    ArrayList<String> contacts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_select);

        contacts = new ArrayList<>();

        ListView contactsList = this.<ListView>findViewById(R.id.contactsCallList);
        TextView status = findViewById(R.id.statusSelectCall);
        TextView type = findViewById(R.id.typeSelectCall);
        TextView allTime = findViewById(R.id.allTimeSelectCall);
        TextView time = findViewById(R.id.timeSelectCall);
        TextView date = findViewById(R.id.dateSelectCall);

        Bundle arguments = getIntent().getExtras();

        if (arguments != null) {
            ArrayList<CallForList> call = (ArrayList<CallForList>) arguments.getSerializable(ArrayList.class.getSimpleName());
            if (call != null) {
                ArrayList<String> stringList = new ArrayList<>();
                for (int i = 0; i < call.size(); i++) {
                    stringList.add(call.get(i).getNameCall());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringList);
                contactsList.setAdapter(adapter);

                type.setText(call.get(0).getTypeCall());
                date.setText(call.get(0).getDateCall());
                status.setText(call.get(0).getStatusCall());
                allTime.setText(call.get(0).getAllTimeCall());
                time.setText(call.get(0).getTimeCall());
            }
        }
    }
}
