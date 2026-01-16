package com.example.project;
import android.content.Context;
import android.graphics.Color;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomAdapterContacts extends ArrayAdapter<String> {

    public static Set<Integer> selectedItems = new HashSet<>();
    private List<Integer> unselectableIndexes;

    public CustomAdapterContacts(Context context, int resource,  List<String> items) {
        super(context, resource, items);
        unselectableIndexes = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItems.contains(position)) {
                    selectedItems.remove(position);
                    view.setBackgroundColor(Color.WHITE);
                } else if (selectedItems.size() > 0) {
                    selectedItems.add(position);
                    view.setBackgroundColor(Color.LTGRAY);
                }

                if (selectedItems.size() == 0) {
                    AddUserActivity.finishSelect();
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AddUserActivity.p = true;

                MenuItem item = AddUserActivity.menu1.findItem(R.id.action_call);
                if (item != null) {
                    item.setVisible(true);
                }

                item = AddUserActivity.menu1.findItem(R.id.action_select_all);
                if (item != null) {
                    item.setVisible(true);
                }

                item = AddUserActivity.menu1.findItem(R.id.action_settings);
                if (item != null) {
                    item.setVisible(false);
                }

                selectedItems.add(position);
                view.setBackgroundColor(Color.LTGRAY);

                return true;
            }
        });

        if (selectedItems.contains(position)) {
            view.setBackgroundColor(Color.LTGRAY);
        }

        return view;
    }

    public void selectAllItems() {
        ListContactsCall.p = true;
        if (MainActivity.contacts != null) {
            if (selectedItems.size() > 0) {
                selectedItems.clear();
            }

            for (int i = 0; i < MainActivity.contacts.size(); i++) {
                selectedItems.add(i);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public boolean isEnabled(int position) {
        return !unselectableIndexes.contains(position);
    }
}
