package com.grieex.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.grieex.R;
import com.grieex.model.tables.Cast;

import java.util.ArrayList;

/**
 * Created by durmus.altin on 20.10.2015.
 */
public class SeriesCastAdapter extends RecyclerView.Adapter<SeriesCastAdapter.ViewHolder> {
    private final Activity activity;
    private final ArrayList<Cast> mData;

    private OnItemClickListener itemClickListener;
    private OnImageViewClickListener itemImageViewClickListener;

    public SeriesCastAdapter(ArrayList<com.grieex.model.tables.Cast> myDataset, Activity _activity) {
        activity = _activity;
        mData = myDataset;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnImageViewClickListener(OnImageViewClickListener listener) {
        this.itemImageViewClickListener = listener;
    }

    public void addAll(ArrayList<Cast> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Cast item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Cast item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public SeriesCastAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.castlist_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Cast cast = mData.get(position);
        holder.tvActor.setText(cast.getName());
        holder.tvCharacter.setText(cast.getCharacter());

        Glide.with(activity)
                .load(cast.getImageUrl())
                .placeholder(R.drawable.guest)
                .into(holder.ivPoster);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Cast getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return mData.get(position).getID();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }


    public interface OnImageViewClickListener {
        void onImageViewClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivPoster;
        final TextView tvActor;
        final TextView tvCharacter;

        ViewHolder(View v) {
            super(v);
            ivPoster = v.findViewById(R.id.ivPoster);
            tvActor = v.findViewById(R.id.tvActor);
            tvCharacter = v.findViewById(R.id.tvCharacter);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(itemView, getLayoutPosition());
                }
            });

            ivPoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemImageViewClickListener != null)
                        itemImageViewClickListener.onImageViewClick(itemView, getLayoutPosition());
                }
            });
        }


    }
}
