package com.grieex.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.grieex.R;
import com.grieex.helper.NLog;
import com.grieex.helper.UiUtils;
import com.grieex.model.CustomMenuItem;

import java.util.ArrayList;

public class CustomMenuAdapter extends BaseAdapter {
	private static final String TAG = CustomMenuAdapter.class.getName();

	private Context mContext;
	private ArrayList<CustomMenuItem> data;
	private LayoutInflater inflater = null;
	private int layoutResourceId = 0;

	private boolean mShowIcon = false;

	public void setShowIcon(boolean b) {
		mShowIcon = true;
	}

	public CustomMenuAdapter(Context ctx, ArrayList<CustomMenuItem> d) {
		if (ctx == null)
			return;

		try {
			mContext = ctx;
			inflater = LayoutInflater.from(mContext);
			data = d;
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	private CustomMenuAdapter(Context ctx, int _layoutResourceId, ArrayList<CustomMenuItem> d) {
		if (ctx == null)
			return;

		try {
			mContext = ctx;
			inflater = LayoutInflater.from(mContext);
			data = d;
			layoutResourceId = _layoutResourceId;
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	class ViewHolder {
		ImageView icon;
		TextView title;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		try {
			ViewHolder holder;
			if (convertView == null) {
				if (layoutResourceId == 0)
					convertView = inflater.inflate(R.layout.custom_menu_item, parent, false);
				else
					convertView = inflater.inflate(layoutResourceId, parent, false);

				holder = new ViewHolder();
				holder.icon = convertView.findViewById(R.id.ivIcon);
				holder.title = convertView.findViewById(R.id.tvText);
				convertView.setTag(holder);
			} else {
				// view already defined, retrieve view holder
				holder = (ViewHolder) convertView.getTag();
			}

			CustomMenuItem c = data.get(position);
			if (mShowIcon & c.getIcon() != -1) {
				holder.icon.setVisibility(View.VISIBLE);
				holder.icon.setImageResource(c.getIcon());
			} else {
				holder.icon.setVisibility(View.INVISIBLE);
			}

			CharSequence text = UiUtils.setSpanBetweenTokens(c.getText(), "#b#", new StyleSpan(Typeface.BOLD));
			holder.title.setText(text);
			// holder.title.setText(Html.fromHtml(c.getText()));

			if (c.getSelected()) {
				holder.title.setTextColor(ContextCompat.getColor(mContext, R.color.red));
			} else {
				holder.title.setTextColor(ContextCompat.getColor(mContext, R.color.dark_grey));
			}

			return convertView;
		} catch (Exception e) {
			NLog.e(TAG, e);
		}
		return convertView;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

}
