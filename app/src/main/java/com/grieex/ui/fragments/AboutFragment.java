package com.grieex.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.grieex.GrieeX;
import com.grieex.R;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.Utils;

public class AboutFragment extends Fragment {
	protected static final String TAG = AboutFragment.class.getName();

	public static AboutFragment newInstance() {
		return new AboutFragment();
	}

	public AboutFragment() {
		// Required empty public constructor
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
        if (GrieeXSettings.RELEASE_MODE) {
			GrieeX.getInstance().trackScreenView(this.getClass().getName());
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_about, container, false);
		TextView tvVersion = v.findViewById(R.id.tvVersion);
		tvVersion.setText("v" + Utils.getVersion(getActivity()));

		ImageButton btnFacebook = v.findViewById(R.id.btnFacebook);
		btnFacebook.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://www.facebook.com/grieex";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		ImageButton btnTwitter = v.findViewById(R.id.btnTwitter);
		btnTwitter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://www.twitter.com/grieex";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

//		ImageView ivImdb = (ImageView) v.findViewById(R.id.ivImdb);
//		ivImdb.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				String url = "http://www.imdb.com";
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.setData(Uri.parse(url));
//				startActivity(i);
//			}
//		});
//
		ImageView ivTmdb = v.findViewById(R.id.ivTmdb);
		ivTmdb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "https://www.themoviedb.org";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		ImageView ivTrakt = v.findViewById(R.id.ivTrakt);
		ivTrakt.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "https://www.trakt.tv";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		ImageView ivTvDb = v.findViewById(R.id.ivTvDb);
		ivTvDb.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String url = "http://thetvdb.com";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
		return v;
	}

}
