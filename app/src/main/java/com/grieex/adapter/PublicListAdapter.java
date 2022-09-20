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
import com.grieex.model.tables.Movie;

import java.util.ArrayList;

public class PublicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static String TAG = PublicListAdapter.class.getName();
    private final Activity activity;
    private final ArrayList<Movie> mData;
    private int mViewType = 0;
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;
    private OnImageViewClickListener itemImageViewClickListener;

    public PublicListAdapter(ArrayList<Movie> data, Activity _activity) {
        activity = _activity;
        mData = data;
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    public void setOnImageViewClickListener(OnImageViewClickListener listener) {
        this.itemImageViewClickListener = listener;
    }

    public void addAll(ArrayList<Movie> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void addAllEnd(ArrayList<Movie> items) {
        int positionFrom = mData.size();
        int positionTo = mData.size() + items.size();

        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Movie item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Movie item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (mViewType == 0) {
            View v1 = inflater.inflate(R.layout.publiclist_row, parent, false);
            return new ViewHolder1(v1);
        } else {
            View v2 = inflater.inflate(R.layout.movielist_gallery_item, parent, false);
            return new ViewHolder2(v2);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Movie m = mData.get(position);

        if (mViewType == 0) {
            ViewHolder1 h = (ViewHolder1) holder;
            if (m.getYear() != null && !m.getYear().isEmpty())
                h.tvOriginalName.setText(m.getOriginalName() + " (" + m.getYear() + ")");
            else
                h.tvOriginalName.setText(m.getOriginalName());

            h.tvPlot.setText(m.getEnglishPlot());

            Glide.with(activity)
                    .load(m.getPoster())
                    .placeholder(R.drawable.transparent_back)
                    .into(h.imageView);

        } else {
            ViewHolder2 h = (ViewHolder2) holder;
            if (m.getYear() != null && !m.getYear().isEmpty())
                h.tvOriginalName.setText(m.getOriginalName() + " (" + m.getYear() + ")");
            else
                h.tvOriginalName.setText(m.getOriginalName());

            Glide.with(activity)
                    .load(m.getPoster())
                    .placeholder(R.drawable.transparent_back)
                    .into(h.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Movie getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return mData.get(position).getID();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public interface OnImageViewClickListener {
        void onImageViewClick(View itemView, int position);
    }

    class ViewHolder1 extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView tvOriginalName;
        final TextView tvPlot;

        ViewHolder1(View v) {
            super(v);
            imageView = v.findViewById(R.id.ivPoster);
            tvOriginalName = v.findViewById(R.id.tvOriginalName);
            tvPlot = v.findViewById(R.id.tvPlot);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(itemView, getLayoutPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemLongClickListener != null)
                        itemLongClickListener.onItemLongClick(itemView, getLayoutPosition());
                    return true;
                }
            });

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemImageViewClickListener != null)
                        itemImageViewClickListener.onImageViewClick(itemView, getLayoutPosition());
                }
            });
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        final com.grieex.widget.AspectRatioImageView imageView;
        final TextView tvOriginalName;


        ViewHolder2(View v) {
            super(v);
            imageView = v.findViewById(R.id.ivPoster);
            tvOriginalName = v.findViewById(R.id.tvOriginalName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClick(itemView, getLayoutPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (itemLongClickListener != null)
                        itemLongClickListener.onItemLongClick(itemView, getLayoutPosition());
                    return true;
                }
            });
        }
    }


}


