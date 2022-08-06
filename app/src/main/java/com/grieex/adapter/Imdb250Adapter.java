package com.grieex.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.helper.NLog;
import com.grieex.model.tables.Imdb250;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;
import java.util.ArrayList;


/**
 * Created by Griee on 24.9.2015.
 */
public class Imdb250Adapter extends RecyclerView.Adapter<Imdb250Adapter.ViewHolder> {
    private final ArrayList<Imdb250> mData;
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
        final ImageView imageView;
        final TextView tvTitle;
        final TextView tvRating;
        final ImageView ivOk;



        ViewHolder(View v) {
            super(v);
            imageView = v.findViewById(R.id.ivPoster);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvRating = v.findViewById(R.id.tvRating);
            ivOk = v.findViewById(R.id.ivOk);

            imageView.setOnClickListener(new View.OnClickListener() {
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

    public void addAll(ArrayList<Imdb250> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void addAllEnd(ArrayList<Imdb250> items) {
        int positionFrom = mData.size();
        int positionTo = mData.size()+items.size();

        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Imdb250 item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Imdb250 item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        mData.clear();
    }

    public Imdb250Adapter(ArrayList<Imdb250> myDataset) {
        mData = myDataset;
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.transparent_back).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
    }

    @Override
    public Imdb250Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.imdb250list_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            Imdb250 m = mData.get(position);

            holder.tvTitle.setText(m.getRank() + ". " + m.getTitle());

            if (m.getRating() != null && !m.getRating().isEmpty()) {
                //String strUserRating = m.getRating().replace(",", ".");
                //float fUserRating = Float.parseFloat(strUserRating);

                if (m.getVotes() == 0) {
                    holder.tvRating.setText(m.getRating());
                } else {
                    String strVotes = new DecimalFormat("###,###").format(m.getVotes());
                    holder.tvRating.setText(String.valueOf(m.getRating()) + " / " + strVotes.replace(",", "."));
                }

                // holder.rbVotes.setRating(fUserRating);
            }

            if (!m.getIsExisting()) {
                holder.ivOk.setVisibility(View.GONE);
            } else {
                holder.ivOk.setVisibility(View.VISIBLE);
            }



            ImageLoader.getInstance().displayImage(m.getImageLink(), holder.imageView, options);

        } catch (Exception e) {
            NLog.e(e);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public Imdb250 getItem(int position) {
        return mData.get(position);
    }

    public int getPositionFromImdbId(String imdbId) {
        if (TextUtils.isEmpty(imdbId))
            return -1;

        for (int i = 0; i <= mData.size(); i++) {
            if (mData.get(i).getImdbNumber().equals(imdbId))
                return i;
        }
        return -1;
    }
}
