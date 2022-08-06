package com.grieex.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.grieex.R;
import com.grieex.model.ThemeItem;

import java.util.ArrayList;

public class ThemeAdapter extends BaseAdapter {
	protected static String TAG = ThemeAdapter.class.getName();
	private final ArrayList<ThemeItem> data;
	private LayoutInflater inflater = null;

	public ThemeAdapter(Context context, ArrayList<ThemeItem> d) {
		data = d;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	private class ViewHolder {
		ImageView imageView;
		TextView tvThemeName;
	}

	public int getCount() {
		return data.size();
	}

	public ThemeItem getItem(int position) {
		return data.get(position);
	}

	public void remove(int index) {
		data.remove(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final View view;
		final ViewHolder holder;
		if (convertView == null) {
			view = inflater.inflate(R.layout.theme_item, parent, false);
			setViewHolder(view);
		} else {
			view = convertView;
		}
		holder = (ViewHolder) view.getTag();

		ThemeItem m = data.get(position);
		holder.tvThemeName.setText(m.getThemeName());
		holder.imageView.setImageResource(m.getColor());

		return view;
	}

	private void setViewHolder(View view) {
		ViewHolder holder = new ViewHolder();
		holder.imageView = view.findViewById(R.id.imageView);
		holder.tvThemeName = view.findViewById(R.id.tvThemeName);
		view.setTag(holder);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}