package com.grieex.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.grieex.R;
import com.grieex.adapter.SeasonsAdapter;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Season;
import com.grieex.model.tables.Series;
import com.grieex.ui.FullScreenImageActivity;
import com.grieex.ui.SeriesEpisodesActivity;
import com.grieex.widget.SimpleDividerItemDecoration;


public class SeriesDetailSeasonsFragment extends Fragment {
    private static final String TAG = SeriesDetailSeasonsFragment.class.getName();

    private static final String ARG_Series = "Series";

    private Series mSeries;
    private Activity activity;

    private RecyclerView mRecyclerView;
    private SeasonsAdapter mAdapter;
    private LinearLayout llSeasons;

    private LinearLayout llNextEpisode;
    private Episode nextEpisode;
    private ImageView ivEpisodeImage;
    private TextView tvEpisodeName;
    private TextView tvDateInfo;


    public SeriesDetailSeasonsFragment() {

    }

    public static SeriesDetailSeasonsFragment newInstance(Series series) {
        SeriesDetailSeasonsFragment fragment = new SeriesDetailSeasonsFragment();
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
        View v = inflater.inflate(R.layout.fragment_series_detail_seasons, container, false);
        try {
            llSeasons = v.findViewById(R.id.llSeasons);

            mRecyclerView = v.findViewById(R.id.list);
            mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(activity, R.drawable.divider));
            mRecyclerView.setNestedScrollingEnabled(false);
            mRecyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManagerVERTICAL = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);
            linearLayoutManagerVERTICAL.setAutoMeasureEnabled(true);
            mRecyclerView.setLayoutManager(linearLayoutManagerVERTICAL);
            mRecyclerView.setAdapter(new SampleRecycler());

            llNextEpisode = v.findViewById(R.id.llNextEpisode);
            llNextEpisode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EpisodeDetailFragment newFragment = EpisodeDetailFragment.newInstance(nextEpisode);
                    newFragment.setCustomEventListener(new EpisodeDetailFragment.OnCustomEventListener() {
                        @Override
                        public void onDismiss(Episode e) {
                            nextEpisode.setWatched(e.getWatched());
                            nextEpisode.setFavorite(e.getFavorite());
                            nextEpisode.setCollected(e.getCollected());
                        }
                    });
                    newFragment.show(getActivity().getSupportFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
                }
            });
            ivEpisodeImage = v.findViewById(R.id.ivEpisodeImage);
            ivEpisodeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(nextEpisode.getEpisodeImage()))
                        return;

                    Intent it = new Intent(getActivity(), FullScreenImageActivity.class);
                    it.putExtra(Constants.ImageLink, nextEpisode.getEpisodeImage());
                    getActivity().startActivity(it);
                }
            });
            tvEpisodeName = v.findViewById(R.id.tvEpisodeName);
            tvDateInfo = v.findViewById(R.id.tvDateInfo);
            Button btSetAsSeen = v.findViewById(R.id.btSetAsSeen);
            btSetAsSeen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        DbUtils.SetEpisodeWatched(activity, nextEpisode.getID(), true);

                        BroadcastNotifier broadcastNotifier = new BroadcastNotifier(activity);
                        broadcastNotifier.broadcastIntentWithObject(Constants.STATE_UPDATE_SERIES, mSeries);
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                }
            });

            LoadNextEpisode();
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

            //llContent.setVisibility(View.VISIBLE);


            if (mSeries.getSeasons() != null) {
                llSeasons.setVisibility(View.VISIBLE);

                mAdapter = new SeasonsAdapter(activity, mSeries.getSeasons());
                mAdapter.setOnItemClickListener(new SeasonsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View itemView, int position) {
                        try {
                            Season s = mAdapter.getItem(position);
                            Intent it = new Intent(activity, SeriesEpisodesActivity.class);
                            //Burada diziyi gönderiyorum. Eskiden gönderilmiyordu.
                            //SeriesEpisodesActivity de sadece mSeries gönderilerek yüklenmesi sağlanacak.
                            //SeriesEpisodesActivity deki DataLoader iptal edilecek.
                            it.putExtra(Constants.Series, mSeries);
                            it.putExtra(Constants.SeriesID, s.getSeriesId());
                            it.putExtra(Constants.SeasonNumber, s.getNumber());
                            startActivity(it);
                        } catch (Exception e) {
                            NLog.e(e);
                        }
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void LoadNextEpisode() {
        try {
            nextEpisode = DbUtils.getNextEpisode(activity, mSeries.getID());
            if (nextEpisode == null) {
                llNextEpisode.setVisibility(View.GONE);
                return;
            }

            Glide.with(activity)
                    .load(nextEpisode.getEpisodeImage())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivEpisodeImage);

            String seasonNumber = String.format("%02d", nextEpisode.getSeasonNumber());
            String episodeNumber = String.format("%02d", nextEpisode.getEpisodeNumber());

            tvEpisodeName.setText(String.format("%sx%s:%s", seasonNumber, episodeNumber, nextEpisode.getEpisodeName()));

            tvDateInfo.setText(DateUtils.millisToString(nextEpisode.getFirstAiredMs()));
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }
}
