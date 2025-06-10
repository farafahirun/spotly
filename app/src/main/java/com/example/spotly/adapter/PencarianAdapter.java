package com.example.spotly.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spotly.R;
import com.example.spotly.model.PlaceResult;

import java.util.ArrayList;
import java.util.List;

public class PencarianAdapter extends RecyclerView.Adapter<PencarianAdapter.ViewHolder> {
    private List<PlaceResult> results = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(PlaceResult placeResult);
    }

    public PencarianAdapter(Context context) {
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<PlaceResult> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    public void clearData() {
        int size = results.size();
        if (size > 0) {
            results.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hasil_pencarian, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaceResult place = results.get(position);
        holder.bind(place, listener);
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvDisplayName, tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivPlaceIcon);
            tvDisplayName = itemView.findViewById(R.id.tvPlaceDisplayName);
            tvType = itemView.findViewById(R.id.tvPlaceType);
        }

        public void bind(final PlaceResult place, final OnItemClickListener listener) {
            tvDisplayName.setText(place.getDisplayName());
            tvType.setText(place.getType().replace('_', ' '));

            Glide.with(itemView.getContext())
                    .load(place.getIconUrl())
                    .placeholder(R.drawable.pin)
                    .error(R.drawable.pin)
                    .into(ivIcon);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(place);
                }
            });
        }
    }
}