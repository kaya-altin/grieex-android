package com.grieex.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.SeriesCastAdapter;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Series;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;

public class SeriesDetailCastFragment extends Fragment {
    private static final String TAG = SeriesDetailCastFragment.class.getName();

    private static final String ARG_Series = "Series";
    private Series mSeries;
    private SeriesCastAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Activity activity;


    public SeriesDetailCastFragment() {
        // Required empty public constructor
    }

    public static SeriesDetailCastFragment newInstance(Series series) {
        SeriesDetailCastFragment fragment = new SeriesDetailCastFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_Series, series);
        fragment.setArguments(args);
        return fragment;
    }

    public void setSeries(Series series) {
        mSeries = series;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getArguments() != null) {
                mSeries = (Series) getArguments().getSerializable(ARG_Series);
            }

            activity = getActivity();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_series_detail_cast, container, false);
        try {
            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(activity, R.drawable.divider2));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.setAdapter(new SampleRecycler());

            Load();
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        return v;
    }

    private void Load() {
        try {
            if (!isAdded() || mSeries == null)
                return;

            if (mSeries.getCast() != null && mSeries.getCast().size() > 0) {
                setCastAdapter();
            } else {
                getCasts();
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void getCasts() {
        try {
            if (!Connectivity.isConnected(activity)) {
                return;
            }

            TraktTv traktTv = new TraktTv();
            traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                @Override
                public void onCompleted(Object m) {
                    if (m != null) {
                        ArrayList<Cast> casts = (ArrayList<Cast>) m;
                        mSeries.setCast(casts);
                        setCastAdapter();
                    }
                }
            });
            traktTv.getCast(mSeries.getTvdbId());
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void setCastAdapter() {
        mAdapter = new SeriesCastAdapter(mSeries.getCast(), activity);
        mAdapter.setOnItemClickListener(new SeriesCastAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Cast cast = mAdapter.getItem(position);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.themoviedb.org/search?query=" + cast.getName().replace(" ", "+")));
                startActivity(browserIntent);
            }
        });
        mAdapter.setOnImageViewClickListener(new SeriesCastAdapter.OnImageViewClickListener() {
            @Override
            public void onImageViewClick(View itemView, int position) {
                Cast cast = mSeries.getCast().get(position);
                Intent i = new Intent(activity, FullScreenImageActivity.class);
                i.putExtra(Constants.ImageLink, cast.getImageUrl());
                startActivity(i);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }


}
