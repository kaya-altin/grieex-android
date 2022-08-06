package com.grieex.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.helper.DateUtils;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Episode;

import java.util.ArrayList;

public class SeriesEpisodesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static String TAG = SeriesEpisodesAdapter.class.getName();
    private final ArrayList<Episode> mData;
    private final boolean isExistDatabase;

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


    private OnWatchedClickListener itemWatchedClickListener;

    public interface OnWatchedClickListener {
        void onWatchedClick(View itemView, int position, boolean isChecked);
    }

    public void setOnWatchedClickListener(OnWatchedClickListener listener) {
        this.itemWatchedClickListener = listener;
    }

    class ViewHolder1 extends RecyclerView.ViewHolder {
        public int position;
        final boolean needInflate;
        final TextView tvEpisodeName;
        final TextView tvFirstAired;
        final TextView tvEpisodeNumber;
        final CheckBox cbWatched;
        final ImageView ivFavorite;
        final ImageView ivCollected;


        ViewHolder1(View v) {
            super(v);
            tvEpisodeName = v.findViewById(R.id.tvEpisodeName);
            tvFirstAired = v.findViewById(R.id.tvFirstAired);
            tvEpisodeNumber = v.findViewById(R.id.tvEpisodeNumber);
            cbWatched = v.findViewById(R.id.cbWatched);
            ivFavorite = v.findViewById(R.id.ivFavorite);
            ivCollected = v.findViewById(R.id.ivCollected);

            needInflate = false;

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

            cbWatched.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemWatchedClickListener != null)
                        itemWatchedClickListener.onWatchedClick(itemView, getLayoutPosition(), cbWatched.isChecked());
                }
            });
        }
    }


    public SeriesEpisodesAdapter(ArrayList<Episode> data, boolean isExistDatabase) {
        mData = data;
        this.isExistDatabase = isExistDatabase;
    }

    public void addAll(ArrayList<Episode> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Episode item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Episode item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public void setAllChecked(boolean b) {
        for (Episode m : mData) {
            m.setWatched(Utils.parseBooleanToInt(b));
        }
        notifyDataSetChanged();
    }

    public void setAllCollected(boolean b) {
        for (Episode m : mData) {
            m.setCollected(Utils.parseBooleanToInt(b));
        }
        notifyDataSetChanged();
    }

    public void setAllFavorite(boolean b) {
        for (Episode m : mData) {
            m.setFavorite(Utils.parseBooleanToInt(b));
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v1 = inflater.inflate(R.layout.series_episode_item, parent, false);
        return new ViewHolder1(v1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Episode m = mData.get(position);

        ViewHolder1 h = (ViewHolder1) holder;
        h.tvEpisodeName.setText(m.getEpisodeName());
        h.tvEpisodeNumber.setText(Utils.parseString(m.getEpisodeNumber()));
        h.cbWatched.setChecked(Utils.parseBoolean(m.getWatched()));

        if (m.getCollected() == 1)
            h.ivCollected.setVisibility(View.VISIBLE);
        else
            h.ivCollected.setVisibility(View.GONE);

        if (m.getFavorite() == 1)
            h.ivFavorite.setVisibility(View.VISIBLE);
        else
            h.ivFavorite.setVisibility(View.GONE);

        if (m.getFirstAiredMs() != 0) {
            h.tvFirstAired.setText(DateUtils.millisToString(m.getFirstAiredMs()));
        } else {
            h.tvFirstAired.setText(R.string.release_date_unknown);
        }

        if (!isExistDatabase){
            h.cbWatched.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Episode getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return mData.get(position).getID();
    }


}


