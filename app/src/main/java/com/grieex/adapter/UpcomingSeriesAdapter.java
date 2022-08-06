package com.grieex.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.model.tables.Series;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class UpcomingSeriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Series> mData;

    private int mViewType = 0;

    public void setViewType(int viewType) {
        mViewType = viewType;
    }


    private final DisplayImageOptions options;

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    private OnItemLongClickListener itemLongClickListener;

    interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    private OnImageViewClickListener itemImageViewClickListener;

    public interface OnImageViewClickListener {
        void onImageViewClick(View itemView, int position);
    }

    public void setOnImageViewClickListener(OnImageViewClickListener listener) {
        this.itemImageViewClickListener = listener;
    }

    public UpcomingSeriesAdapter(ArrayList<Series> myDataset) {
        mData = myDataset;
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.transparent_back).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    class ViewHolder1 extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView tvSeriesName;
        final TextView tvEpisodeName;
        final TextView tvDateInfo;

        ViewHolder1(View v) {
            super(v);
            imageView = v.findViewById(R.id.poster);
            tvSeriesName = v.findViewById(R.id.tvSeriesName);
            tvEpisodeName = v.findViewById(R.id.tvEpisodeName);
            tvDateInfo = v.findViewById(R.id.tvDateInfo);

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

    public void addAll(ArrayList<Series> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void addAllEnd(ArrayList<Series> items) {
        int positionFrom = mData.size();
        int positionTo = mData.size()+items.size();

        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Series item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Series item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void add(Series item) {
        mData.add(item);
        sort();
        int index = mData.indexOf(item);
        notifyItemInserted(index);
    }

    private void sort() {
        Collections.sort(mData, new SeriesComparator());
    }

    private class SeriesComparator implements Comparator<Series> {
        public int compare(Series o1, Series o2) {

            Collator trCollator = Collator.getInstance(new Locale("tr", "TR"));

            return trCollator.compare(o1.getSeriesName(), o2.getSeriesName());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (mViewType == 0) {
            View v1 = inflater.inflate(R.layout.series_upcoming_item, parent, false);
            return new ViewHolder1(v1);
        } else {
            View v2 = inflater.inflate(R.layout.movielist_gallery_item, parent, false);
            return new ViewHolder2(v2);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Series s = mData.get(position);

        if (mViewType == 0) {
            ViewHolder1 h = (ViewHolder1) holder;
            h.tvSeriesName.setText(s.getSeriesName());
            h.tvEpisodeName.setText(s.getEpisodeName());
            h.tvDateInfo.setText(s.getDateInfo());

            ImageLoader.getInstance().displayImage(s.getPoster(), h.imageView, options);
        } else {
            ViewHolder2 h = (ViewHolder2) holder;
            h.tvOriginalName.setText(s.getSeriesName());

            ImageLoader.getInstance().displayImage(s.getPoster(), h.imageView, options);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public Series getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return mData.get(position).getID();
    }
}
