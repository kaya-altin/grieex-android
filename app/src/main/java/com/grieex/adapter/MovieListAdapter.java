package com.grieex.adapter;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.model.tables.Movie;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MovieListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static String TAG = MovieListAdapter.class.getName();
    private final ArrayList<Movie> mData;

    private int mViewType = 0;
    private boolean mShowCheckBox = false;
    private String mOrderTextColumn = "";

    public void setViewType(int viewType) {
        mViewType = viewType;
    }

    public void setShowCheckBox(boolean showCheckBox) {
        mShowCheckBox = showCheckBox;

        if (!mShowCheckBox) {
            for (Movie movie : getSelecteds()) {
                movie.setIsSelected(false);
            }
        }
        notifyDataSetChanged();
    }

    public void setShowOrderText(String column) {
        mOrderTextColumn = column;
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


    private OnItemSelectedListener itemSelectedListener;

    public interface OnItemSelectedListener {
        void onSelectedChanged(View itemView, int position);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.itemSelectedListener = listener;
    }

    private final DisplayImageOptions options;

    class ViewHolder1 extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView tvOriginalName;
        final TextView tvOtherName;
        final TextView tvDirector;
        final CheckBox chkSelected;
        final TextView tvFilterText;

        ViewHolder1(View v) {
            super(v);
            imageView = v.findViewById(R.id.ivPoster);
            tvOriginalName = v.findViewById(R.id.tvOriginalName);
            tvOtherName = v.findViewById(R.id.tvOtherName);
            tvDirector = v.findViewById(R.id.tvDirector);
            chkSelected = v.findViewById(R.id.chkSelected);
            tvFilterText = v.findViewById(R.id.tvFilterText);

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

    public MovieListAdapter(ArrayList<Movie> data) {
        mData = data;
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.transparent_back).cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public void add(Movie item) {
        mData.add(item);
        sort();
        int index = mData.indexOf(item);
        notifyItemInserted(index);
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

    private void sort() {
        Collections.sort(mData, new MovieComparator());
    }

    private class MovieComparator implements Comparator<Movie> {
        public int compare(Movie o1, Movie o2) {

            Collator trCollator = Collator.getInstance(new Locale("tr", "TR"));

            return trCollator.compare(o1.getOriginalName(), o2.getOriginalName());
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (mViewType == 0) {
            View v1 = inflater.inflate(R.layout.movielist_item, parent, false);
            return new ViewHolder1(v1);
        } else {
            View v2 = inflater.inflate(R.layout.movielist_gallery_item, parent, false);
            return new ViewHolder2(v2);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Movie m = mData.get(position);

        if (mViewType == 0) {
            ViewHolder1 h = (ViewHolder1) holder;
            if (m.getYear() != null && !m.getYear().isEmpty())
                h.tvOriginalName.setText(m.getOriginalName() + " (" + m.getYear() + ")");
            else
                h.tvOriginalName.setText(m.getOriginalName());

            if (!TextUtils.isEmpty(m.getOtherName())) {
                h.tvOtherName.setText(m.getOtherName());
                h.tvOtherName.setVisibility(View.VISIBLE);
            } else
                h.tvOtherName.setVisibility(View.GONE);

            h.tvDirector.setText(m.getDirector());

            if (TextUtils.isEmpty(mOrderTextColumn)) {
                h.tvFilterText.setVisibility(View.GONE);
            } else {
                h.tvFilterText.setVisibility(View.VISIBLE);

                switch (mOrderTextColumn) {
                    case Movie.COLUMNS.ArchivesNumber:
                        h.tvFilterText.setText(m.getArchivesNumber());
                        break;
                    case Movie.COLUMNS.InsertDate:
                        h.tvFilterText.setText(m.getInsertDate());
                        break;
                    case Movie.COLUMNS.UpdateDate:
                        h.tvFilterText.setText(m.getUpdateDate());
                        break;
                    case Movie.COLUMNS.ImdbUserRating:
                        h.tvFilterText.setText(m.getImdbUserRating());
                        break;
                    case Movie.COLUMNS.TmdbUserRating:
                        h.tvFilterText.setText(m.getTmdbUserRating());
                        break;
                }
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
            if (m.getYear() != null && !m.getYear().isEmpty())
                h.tvOriginalName.setText(m.getOriginalName() + " (" + m.getYear() + ")");
            else
                h.tvOriginalName.setText(m.getOriginalName());


            ImageLoader.getInstance().displayImage(m.getPoster(), h.imageView, options);
        }

    }

//	@Override
//	public void onBindViewHolder(ViewHolder holder, int position) {
//		Movie m = mData.get(position);
//
//		if (m.getYear() != null && !m.getYear().isEmpty())
//			holder.tvOriginalName.setText(m.getOriginalName() + " (" + m.getYear() + ")");
//		else
//			holder.tvOriginalName.setText(m.getOriginalName());
//
//		holder.tvOtherName.setText(m.getOtherName());
//		holder.tvDirector.setText(m.getDirector());
//
//
//		imageLoader.displayImage(m.getPoster(), holder.imageView, options);
//	}

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


    public ArrayList<Movie> getSelecteds() {
        ArrayList<Movie> returnList = new ArrayList<>();
        for (Movie c : mData) {
            if (c.getIsSelected())
                returnList.add(c);
        }

        return returnList;
    }
}


