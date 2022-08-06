package com.grieex.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.grieex.R;
import com.grieex.helper.NLog;
import com.grieex.model.Page;
import com.grieex.model.Page.PageTypes;

public class SlideMenuAdapter extends ArrayAdapter<Page> {
	private static final String TAG = SlideMenuAdapter.class.getName();

	private final Context mContext;
	private final int tintColor;

	public SlideMenuAdapter(Context context) {
		super(context, 0);
		mContext = context;
		tintColor = ContextCompat.getColor(mContext, R.color.white);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.slidemenu_row, null);
			}

			LinearLayout llRow = convertView.findViewById(R.id.llRow);
			ImageView icon = convertView.findViewById(R.id.row_icon);
			TextView title = convertView.findViewById(R.id.row_title);
			TextView row_count = convertView.findViewById(R.id.row_count);

			Page p = getItem(position);

			LinearLayout llContent = convertView.findViewById(R.id.llContent);
			LinearLayout llSeparator = convertView.findViewById(R.id.llSeparator);


			if (p.getPageType() == PageTypes.Separator) {
				icon.setVisibility(View.GONE);
				title.setVisibility(View.GONE);
				llRow.setClickable(false);

				llSeparator.setVisibility(View.VISIBLE);
				llContent.setVisibility(View.GONE);

				TextView separator_title = convertView.findViewById(R.id.separator_title);
				separator_title.setText(p.getPageName());
				ImageView separator_icon = convertView.findViewById(R.id.separator_icon);

				if (p.getIcon() != 0){
					separator_icon.setVisibility(View.VISIBLE);
					Drawable iconDrawable = ContextCompat.getDrawable(mContext, p.getIcon());
					Drawable wrappedDrawable = DrawableCompat.wrap(iconDrawable);
					DrawableCompat.setTint(wrappedDrawable, tintColor);
					separator_icon.setImageDrawable(iconDrawable);
				}else{
					separator_icon.setVisibility(View.GONE);
				}
				llRow.setBackgroundResource(0);
			} else {
				llSeparator.setVisibility(View.GONE);
				llContent.setVisibility(View.VISIBLE);

				icon.setVisibility(View.VISIBLE);
				title.setVisibility(View.VISIBLE);
				if (p.getIcon() == 0) {
					icon.setVisibility(View.GONE);
				} else {
					icon.setVisibility(View.VISIBLE);
					Drawable iconDrawable = ContextCompat.getDrawable(mContext, p.getIcon());
					Drawable wrappedDrawable = DrawableCompat.wrap(iconDrawable);
					DrawableCompat.setTint(wrappedDrawable, tintColor);
					icon.setImageDrawable(iconDrawable);
				}
				title.setText(p.getPageName());
				llRow.setBackgroundResource(R.drawable.selector_slide_menu);

				if (p.getCount() != 0){
					row_count.setVisibility(View.VISIBLE);
					row_count.setText(String.valueOf(p.getCount()));
				}else{
					row_count.setVisibility(View.GONE);
				}
			}
		} catch (Exception e) {
			NLog.e(TAG, e);
		}

		return convertView;
	}

}