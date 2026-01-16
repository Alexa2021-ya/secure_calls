package com.example.project;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.ViewHolder> {
    interface OnCallClickListener {
        void onCallClick(ArrayList<CallForList> call, int position);
    }

    interface OnLongCallClickListener {
        void onLongCallClick(ArrayList<CallForList> call, int position);
    }

    interface OnButtonClickListener {
        void onButtonClicked(int position);
    }

    private static Set<Integer> selectedItems = new HashSet<>();
    private final OnCallClickListener onClickListener;
    private final OnButtonClickListener onButtonClickListener;
    private final OnLongCallClickListener onLongClickListener;
    private final LayoutInflater inflater;

    CallAdapter(Context context, OnCallClickListener onClickListener, OnLongCallClickListener onLongClickListener, OnButtonClickListener onButtonClickListener) {
        this.onClickListener = onClickListener;
        this.onLongClickListener = onLongClickListener;
        this.onButtonClickListener = onButtonClickListener;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CallAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_calls_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ArrayList<CallForList> call = MainActivity.groupedCalls.get(position);

        if (call != null) {
            if (call.size() == 1) {
                holder.nameView.setText(call.get(0).getNameCall());
            } else {
                holder.nameView.setText(R.string.title_group_call);
            }
            holder.infoView.setText(call.get(0).getTypeCall() + " " + call.get(0).getDateCall());
        }

        holder.itemView.setBackgroundColor(Color.WHITE);


        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    onClickListener.onCallClick(call, position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClickListener.onLongCallClick(call, position);

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

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onButtonClickListener.onButtonClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return  MainActivity.groupedCalls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameView;
        final TextView infoView;
        final Button button;
        ViewHolder(View view) {
            super(view);
            nameView = view.findViewById(R.id.nameContactCall);
            infoView = view.findViewById(R.id.dateCall);
            button = view.findViewById(R.id.buttonDeleteContactCall);
        }
    }

    public void deleteSelectedItems(Context context) {
        ArrayList<ArrayList<CallForList>> selectedItemsToBeRemoved = new ArrayList<>();
        for (Integer position : selectedItems) {
            selectedItemsToBeRemoved.add(MainActivity.groupedCalls.get(position));
        }

        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.deleteCalls(selectedItemsToBeRemoved);

        MainActivity.groupedCalls = null;
        MainActivity.calls = null;

        databaseHelper = new DatabaseHelper(context);
        databaseHelper.getCalls();

        clearSelectedItems();
    }

    public void selectAllItems() {
        for (int i = 0; i < MainActivity.groupedCalls.size(); i++) {
            if (!selectedItems.contains(i)) {
                selectedItems.add(i);
            }
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelectedItems() {
        selectedItems.clear();
        notifyDataSetChanged();
        MainActivity.finishActivity();
    }
}
