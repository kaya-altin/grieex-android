package com.grieex.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Cast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


/**
 * Created by Griee on 24.9.2015.
 */
public class CastListAdapter extends RecyclerView.Adapter<CastListAdapter.ViewHolder> {
    private final ArrayList<Cast> mData;
    private final DisplayImageOptions options;



    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private OnImageViewClickListener itemImageViewClickListener;

    public interface OnImageViewClickListener {
        void onImageViewClick(View itemView, int position);
    }

    public void setOnImageViewClickListener(OnImageViewClickListener listener) {
        this.itemImageViewClickListener = listener;
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

            ivPoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemImageViewClickListener != null)
                        itemImageViewClickListener.onImageViewClick(itemView, getLayoutPosition());
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
        }
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

    public void clear() {
        mData.clear();
    }

    public CastListAdapter(ArrayList<Cast> myDataset) {
        mData = myDataset;
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.guest).showImageForEmptyUri(R.drawable.guest).showImageOnFail(R.drawable.guest).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
    }

    @Override
    public CastListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.castlist_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            final Cast m = mData.get(position);

            holder.tvActor.setText(m.getName());
            holder.tvCharacter.setText(m.getCharacter());


            ImageLoader.getInstance().displayImage(m.getImageUrl(), holder.ivPoster, options);
        } catch (Exception e) {
            NLog.e(e);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public Cast getItem(int position) {
        return mData.get(position);
    }


}
