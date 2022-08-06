package com.grieex.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.model.FileItem;

import java.util.ArrayList;

public class MovieFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static String TAG = MovieFilesAdapter.class.getName();
    private final ArrayList<FileItem> mData;

    class ViewHolder extends RecyclerView.ViewHolder {
        public int position;
        final TextView lblListItem;


        ViewHolder(View v) {
            super(v);
            lblListItem = v.findViewById(R.id.lblListItem);
        }
    }


    public MovieFilesAdapter(ArrayList<FileItem> data) {
        mData = data;
    }

    public void addAll(ArrayList<FileItem> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, FileItem item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(FileItem item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v1 = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FileItem m = mData.get(position);

        ViewHolder h = (ViewHolder) holder;
        h.lblListItem.setText(m.getText());

        if (m.geIsTitle())
            h.lblListItem.setTypeface(null, Typeface.BOLD);
        else
            h.lblListItem.setTypeface(null, Typeface.NORMAL);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public FileItem getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return -1;
    }


}


