package com.grieex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.helper.Constants;
import com.grieex.helper.DateUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.uwetrottmann.trakt5.entities.Comment;

import java.util.ArrayList;

public class TraktCommentsAdapter extends RecyclerView.Adapter<TraktCommentsAdapter.ViewHolder> {
    private final ArrayList<Comment> mData;
    private final Context mContext;
    private final DisplayImageOptions options;

    private boolean firstItemDisable = false;

    public void setFirstItemDisable(boolean firstItemDisable) {
        this.firstItemDisable = firstItemDisable;
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

    interface OnImageViewClickListener {
        void onImageViewClick(View itemView, int position);
    }

    public void setOnImageViewClickListener(OnImageViewClickListener listener) {
        this.itemImageViewClickListener = listener;
    }

    private OnItemLikeClickListener itemLikeClickListener;

    public interface OnItemLikeClickListener {
        void onItemLikeClick(View itemView, int position);
    }

    public void setOnItemLikeClickListener(OnItemLikeClickListener listener) {
        this.itemLikeClickListener = listener;
    }

    public TraktCommentsAdapter(Context context, ArrayList<Comment> myDataset) {
        mData = myDataset;
        mContext = context;
        options = new DisplayImageOptions.Builder().displayer(new CircleBitmapDisplayer()).showImageOnLoading(R.drawable.guest).showImageForEmptyUri(R.drawable.guest).showImageOnFail(R.drawable.guest).cacheInMemory(true).cacheOnDisk(false).considerExifParams(true).build();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView tvUserName;
        final TextView tvDateInfo;
        final TextView tvComment;
        final TextView tvCommentCount;
        final TextView tvLikeCount;
        final LinearLayout llReplies;
        final LinearLayout container;
        final LinearLayout leftLayout;

        ViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.container);
            leftLayout= v.findViewById(R.id.leftLayout);
            imageView = v.findViewById(R.id.imageView);
            tvUserName = v.findViewById(R.id.tvUserName);
            tvDateInfo = v.findViewById(R.id.tvDateInfo);
            tvComment = v.findViewById(R.id.tvComment);
            tvCommentCount = v.findViewById(R.id.tvCommentCount);
            tvLikeCount = v.findViewById(R.id.tvLikeCount);
            llReplies = v.findViewById(R.id.llReplies);

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

            leftLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemLikeClickListener != null)
                        itemLikeClickListener.onItemLikeClick(itemView, getLayoutPosition());
                }
            });
        }


    }

    public void addAll(ArrayList<Comment> items, int positionFrom, int positionTo) {
        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void addEnd(Comment item) {
        int positionFrom = mData.size();
        int positionTo = mData.size() + 1;

        mData.add(item);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void addAllEnd(ArrayList<Comment> items) {
        int positionFrom = mData.size();
        int positionTo = mData.size() + items.size();

        mData.addAll(items);

        for (int i = positionFrom; i <= positionTo; i++) {
            notifyItemInserted(i);
        }
    }

    public void add(int position, Comment item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Comment item) {
        int position = mData.indexOf(item);
        mData.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public TraktCommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Comment c = mData.get(position);
        holder.tvUserName.setText(c.user.username);
        holder.tvDateInfo.setText(DateUtils.getDateFormat(c.created_at.getNano(), Constants.DATE_FORMAT14));
        holder.tvComment.setText(c.comment);
        //mihmih
//        holder.tvLikeCount.setText(Utils.parseString(c.likes));

        if (position == 0 && firstItemDisable) {
            holder.container.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_grey));
        }

        if (c.replies > 0) {
            holder.llReplies.setVisibility(View.VISIBLE);
            holder.tvCommentCount.setText(String.format("(%s)", c.replies));
        } else {
            holder.llReplies.setVisibility(View.GONE);
        }

        if (c.user.images != null && c.user.images.avatar != null && c.user.images.avatar.full != null)
            ImageLoader.getInstance().displayImage(c.user.images.avatar.full, holder.imageView, options);
        else
            ImageLoader.getInstance().displayImage("drawable://" + R.drawable.guest, holder.imageView, options);

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public Comment getItem(int position) {
        return mData.get(position);
    }

    public long getItemId(int position) {
        return mData.get(position).id;
    }
}
