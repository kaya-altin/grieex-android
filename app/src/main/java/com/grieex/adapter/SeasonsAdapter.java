package com.grieex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Season;

import java.util.ArrayList;


/**
 * Created by Griee on 24.9.2015.
 */
public class SeasonsAdapter extends RecyclerView.Adapter<SeasonsAdapter.ViewHolder> {
    private final ArrayList<Season> mData;
    private final Context mContext;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        final TextView tvSeriesTitle;
        final ProgressBar progress;
        final TextView tvInfo;

        ViewHolder(View v) {
            super(v);
            tvSeriesTitle = v.findViewById(R.id.tvSeriesTitle);
            progress = v.findViewById(R.id.progress);
            tvInfo = v.findViewById(R.id.tvInfo);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(itemView, getLayoutPosition());
                }
            });
        }


    }

    public void addAll(ArrayList<Season> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Season item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Season item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }

    public SeasonsAdapter(Context context, ArrayList<Season> myDataset) {
        mData = myDataset;
        mContext = context;
    }

    @Override
    public SeasonsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.season_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Season s = mData.get(position);

        if (s.getNumber() != 0) {
            holder.tvSeriesTitle.setText(mContext.getString(R.string.season) + " " + Utils.parseString(s.getNumber()));
        } else {
            holder.tvSeriesTitle.setText(mContext.getString(R.string.specials));
        }

        if (s.getEpisodeCountCustom() > 0)
            holder.tvInfo.setText(Utils.parseString(s.getWatchedCount() + "/" + s.getEpisodeCountCustom()));
        else
            holder.tvInfo.setText(Utils.parseString(s.getEpisodeCount()));

        holder.progress.setMax(s.getEpisodeCountCustom());
        holder.progress.setProgress(s.getWatchedCount());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public Season getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return mData.get(position).getID();
    }
}
