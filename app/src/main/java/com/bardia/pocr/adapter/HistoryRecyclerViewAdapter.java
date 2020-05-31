package com.bardia.pocr.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bardia.pocr.HistoryActivity;
import com.bardia.pocr.R;
import com.bardia.pocr.model.TextObject;

import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>{

    HistoryRecyclerViewAdapter.OnItemClickListener itemClickListener, showDetail;
    ArrayList<TextObject> textObjects;

    public interface OnItemClickListener {
        void onItemClick(TextObject textObject, int adapterPosition);
    }

    public HistoryRecyclerViewAdapter(ArrayList<TextObject> textObjects,
                                      HistoryRecyclerViewAdapter.OnItemClickListener listener,
                                      HistoryRecyclerViewAdapter.OnItemClickListener showDetail) {
        this.textObjects = textObjects;
        this.itemClickListener = listener;
        this.showDetail = showDetail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.history_item_layout, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.v("RECYCLERVIEW - VH", String.valueOf(position));
        if(textObjects.get(position).getText().length() < 50) {
            holder.tvText.setText(textObjects.get(position).getText());
        } else {
            holder.tvText.setText(textObjects.get(position).getText().substring(0, 45).concat("[...]"));
        }
        holder.tvDate.setText(textObjects.get(position).getDate());
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.onItemClick(textObjects.get(position), position);
            }
        });
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetail.onItemClick(textObjects.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return textObjects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout layout;
        TextView tvText, tvDate, tvEmpty;
        ImageButton button;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.historyItemLayout);
            tvText = itemView.findViewById(R.id.historyText);
            tvDate = itemView.findViewById(R.id.historyDate);
            button = itemView.findViewById(R.id.deleteFromHistory);
        }
    }
}
