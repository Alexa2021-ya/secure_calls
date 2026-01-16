package com.example.project;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.Set;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    interface OnContactClickListener {
        void onContactClick(Contact contact, int position);
    }

    interface OnLongContactClickListener {
        void onLongContactClick(Contact contact, int position);
    }

    public Set<Integer> selectedItems = new HashSet<>();
    private final OnContactClickListener onClickListener;
    private final OnLongContactClickListener onLongContactClickListener;
    private final LayoutInflater inflater;
    Context context;
    ContactAdapter(Context context, OnContactClickListener onClickListener, OnLongContactClickListener onLongContactClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.onLongContactClickListener = onLongContactClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_contacts_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Contact contact;
        contact = MainActivity.contacts.get(position);

        holder.nameView.setText(contact.getName());
        holder.numberView.setText(contact.getNumber());
        holder.itemView.setBackgroundColor(Color.WHITE);


        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = ((ColorDrawable) holder.itemView.getBackground()).getColor();
                if (color != Color.GREEN) {
                    if (selectedItems.contains(position)) {
                        selectedItems.remove(position);
                        holder.itemView.setBackgroundColor(Color.WHITE);

                        if (selectedItems.size() == 0) {
                            clearSelectedItems();
                        }
                    }
                    else if (selectedItems.size() != 0) {
                        selectedItems.add(position);
                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                    }
                    else {
                        onClickListener.onContactClick(contact, position);
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongContactClickListener.onLongContactClick(contact, position);

                if (selectedItems.contains(position)) {
                    holder.itemView.setBackgroundColor(Color.WHITE);
                    selectedItems.remove(position);
                }
                else {
                    selectedItems.add(position);
                    holder.itemView.setBackgroundColor(Color.LTGRAY);
                }

                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return MainActivity.contacts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView numberView;

        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.nameContactTextItem);
            numberView = view.findViewById(R.id.numberContactTextItem);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelectedItems() {
        selectedItems.clear();
        notifyDataSetChanged();
        ListContactsActivity.finishSelectConf();
    }

    public void selectAllItems() {
        for (int i = 0; i < MainActivity.contacts.size(); i++) {
            if (!selectedItems.contains(i)) {
                selectedItems.add(i);
            }
        }
        notifyDataSetChanged();
    }
}
