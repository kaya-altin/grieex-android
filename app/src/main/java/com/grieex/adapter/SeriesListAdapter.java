package com.grieex.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

public class SeriesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static String TAG = SeriesListAdapter.class.getName();
    private final Context mContext;
    private final ArrayList<Series> mData;

    private int mViewType = 0;
    private boolean mShowCheckBox = false;

    public void setViewType(int viewType) {
        mViewType = viewType;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        mShowCheckBox = showCheckBox;

        if (!mShowCheckBox) {
            for (Series series : getSelecteds()) {
                series.setIsSelected(false);
            }
        }
        notifyDataSetChanged();
    }

    public SeriesListAdapter(Context ctx, ArrayList<Series> data) {
        mData = data;
        mContext = ctx;
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.transparent_back).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).build();
    }


    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    private OnItemLongClickListener itemLongClickListener;

    public interface OnItemLongClickListener {
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


    private SeriesListAdapter.OnItemSelectedListener itemSelectedListener;

    public interface OnItemSelectedListener {
        void onSelectedChanged(View itemView, int position);
    }

    public void setOnItemSelectedListener(SeriesListAdapter.OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }

    private final DisplayImageOptions options;

    class ViewHolder1 extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView tvSeriesName;
        final TextView tvDateInfo;
        final TextView tvStatus;
        final ProgressBar progress;
        final TextView tvInfo;
        final ImageView ivCollected;
        final CheckBox chkSelected;

        ViewHolder1(View v) {
            super(v);
            imageView = v.findViewById(R.id.poster);
            tvSeriesName = v.findViewById(R.id.tvSeriesName);
            tvDateInfo = v.findViewById(R.id.tvDateInfo);
            tvStatus = v.findViewById(R.id.tvStatus);
            progress = v.findViewById(R.id.progress);
            tvInfo = v.findViewById(R.id.tvInfo);
            ivCollected= v.findViewById(R.id.ivCollected);
            chkSelected = v.findViewById(R.id.chkSelected);

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

            chkSelected.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemSelectedListener != null)
                        itemSelectedListener.onSelectedChanged(itemView, getLayoutPosition());
                }
            });
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        final com.grieex.widget.AspectRatioImageView imageView;
        final TextView tvSeriesName;

        ViewHolder2(View v) {
            super(v);
            imageView = v.findViewById(R.id.poster);
            tvSeriesName = v.findViewById(R.id.tvSeriesName);

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

    public void add(Series item) {
        mData.add(item);
        sort();
        int index = mData.indexOf(item);
        notifyItemInserted(index);
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
            View v1 = inflater.inflate(R.layout.serieslist_item, parent, false);
            return new ViewHolder1(v1);
        } else {
            View v2 = inflater.inflate(R.layout.serieslist_gallery_item, parent, false);
            return new ViewHolder2(v2);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Series m = mData.get(position);

        if (mViewType == 0) {
            ViewHolder1 h = (ViewHolder1) holder;
            h.tvSeriesName.setText(m.getSeriesName());
            h.tvDateInfo.setText(m.getDateInfo());

            if (m.getStatus().equals("0")) {
                h.tvStatus.setText(mContext.getString(R.string.ended));
            } else {
                h.tvStatus.setText(mContext.getString(R.string.continuing));
            }

            h.progress.setMax(m.getEpisodeCount());
            h.progress.setProgress(m.getWatchedCount());

            h.tvInfo.setText(String.format("%s / %s", m.getWatchedCount(), m.getEpisodeCount()));

            if (m.getCollectedCount()>0){
                h.ivCollected.setVisibility(View.VISIBLE);
            }else{
                h.ivCollected.setVisibility(View.GONE);
            }

            if (mShowCheckBox) {
                h.chkSelected.setChecked(m.getIsSelected());
                h.chkSelected.setVisibility(View.VISIBLE);
            } else {
                h.chkSelected.setVisibility(View.GONE);
            }

            ImageLoader.getInstance().displayImage(m.getPoster(), h.imageView, options);
        } else {
            ViewHolder2 h = (ViewHolder2) holder;
            h.tvSeriesName.setText(m.getSeriesName());

            ImageLoader.getInstance().displayImage(m.getPoster(), h.imageView, options);
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

    public ArrayList<Series> getSelecteds() {
        ArrayList<Series> returnList = new ArrayList<>();
        for (Series c : mData) {
            if (c.getIsSelected())
                returnList.add(c);
        }

        return returnList;
    }
}


