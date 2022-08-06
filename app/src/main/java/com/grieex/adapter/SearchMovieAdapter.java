package com.grieex.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grieex.R;
import com.grieex.core.SearchResult;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;


/**
 * Created by Griee on 24.9.2015.
 */
public class SearchMovieAdapter extends RecyclerView.Adapter<SearchMovieAdapter.ViewHolder> {
    private final ArrayList<SearchResult> mData;
    private Constants.ContentProviders mResultType;
    private final DisplayImageOptions options;


    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private OnAddClickListener listener2;

    public interface OnAddClickListener {
        void onAddClick(View itemView, int position);
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.listener2 = listener;
    }

    public void setResultType(Constants.ContentProviders resultType) {
        mResultType = resultType;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView poster;
        final TextView tvOriginalName;
        final TextView tvYear;
        final FloatingActionButton btnAdd;

        ViewHolder(View v) {
            super(v);
            poster = v.findViewById(R.id.poster);
            tvOriginalName = v.findViewById(R.id.tvOriginalName);
            tvYear = v.findViewById(R.id.tvYear);
            btnAdd = v.findViewById(R.id.btnAdd);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener2 != null)
                        listener2.onAddClick(btnAdd, getLayoutPosition());
                }
            });
        }


    }

    public void addAll(ArrayList<SearchResult> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void addAllEnd(ArrayList<SearchResult> items) {
        int positionFrom = mData.size();
        int positionTo = mData.size()+items.size();

        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, SearchResult item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(SearchResult item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void clear(){
        mData.clear();
    }

    public SearchMovieAdapter(ArrayList<SearchResult> myDataset) {
        mData = myDataset;
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.transparent_back).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).displayer(new FadeInBitmapDisplayer(500)).build();
    }

    @Override
    public SearchMovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_movie, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            SearchResult sr = mData.get(position);
            holder.tvOriginalName.setText(sr.getTitle());
            holder.tvYear.setText(sr.getYear());


            if (mResultType == Constants.ContentProviders.TMDb || mResultType == Constants.ContentProviders.Imdb || mResultType == Constants.ContentProviders.TMDbTv) {
                if (sr.getIsExisting()) {
                    holder.btnAdd.setVisibility(View.GONE);
                } else {
                    holder.btnAdd.setVisibility(View.VISIBLE);
                }
            } else {
                holder.btnAdd.setVisibility(View.GONE);
            }


            ImageLoader.getInstance().displayImage(sr.getPoster(), holder.poster, options);
        } catch (Exception e) {
            NLog.e(e);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public SearchResult getItem(int position) {
        return mData.get(position);
    }


}
