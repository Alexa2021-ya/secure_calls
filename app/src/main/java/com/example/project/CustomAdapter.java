package com.example.project;
import static com.example.project.MainActivity.confContacts;
import static com.example.project.MainActivity.isConfStart;
import static com.example.project.MainActivity.statusConfs;

import android.content.Context;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomAdapter extends ArrayAdapter<String> {

    public static Set<Integer> selectedItems = new HashSet<>();
    public CustomAdapter(Context context, int resource,  List<String> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        TextView textView = view.findViewById(android.R.id.text1);

        if (isConfStart) {
            if (MainActivity.confCalls.get(position).isActive()) {
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            } else {
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
            }
        }
        else {
            if (MainActivity.statusCalls.get(position)) {
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
            }
            else {
                textView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
            }
        }


        if (isConfStart == true) {
            if (selectedItems.contains(position)) {
                view.setBackgroundColor(Color.LTGRAY);
            }
            else {
                view.setBackgroundColor(Color.WHITE);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedItems.contains(position)) {
                        selectedItems.remove(position);
                        view.setBackgroundColor(Color.WHITE);
                    }
                    else if (selectedItems.size() > 0) {
                        selectedItems.add(position);
                        view.setBackgroundColor(Color.LTGRAY);
                    }

                    if (selectedItems.size() == 0) {
                        ListContactsCall.finishSelect();
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ListContactsCall.p = true;

                    MenuItem item = ListContactsCall.menu1.findItem(R.id.action_delete_call);
                    if(item != null){
                        item.setVisible(true);
                    }

                    item = ListContactsCall.menu1.findItem(R.id.action_select_all);
                    if(item != null){
                        item.setVisible(true);
                    }

                    item = ListContactsCall.menu1.findItem(R.id.action_settings);
                    if (item != null){
                        item.setVisible(false);
                    }

                    selectedItems.add(position);
                    view.setBackgroundColor(Color.LTGRAY);

                    return true;
                }
            });
        }


        return view;
    }

    public void selectAllItems() {
        ListContactsCall.p = true;
        if (confContacts != null) {
            if (selectedItems.size() > 0) {
                selectedItems.clear();
            }

            for (int i = 0; i < confContacts.size(); i++) {
                selectedItems.add(i);
            }
        }
        notifyDataSetChanged();
    }
}

