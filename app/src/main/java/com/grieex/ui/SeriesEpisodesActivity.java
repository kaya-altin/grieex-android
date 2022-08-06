package com.grieex.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.grieex.R;
import com.grieex.adapter.SeriesEpisodesAdapter;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DbUtils;
import com.grieex.helper.NLog;
import com.grieex.helper.SampleRecycler;
import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Series;
import com.grieex.ui.fragments.EpisodeDetailFragment;
import com.grieex.widget.SimpleDividerItemDecoration;

import java.util.ArrayList;

public class SeriesEpisodesActivity extends BaseActivity {
    private static final String TAG = SeriesEpisodesActivity.class.getName();

    private SeriesEpisodesAdapter mAdapter;
    private int SeriesID;
    private int SeasonNumber;
    private Series mSeries;
    private boolean isExistDatabase = false;

    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_episodes);
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        RecyclerView mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this, R.drawable.divider2));
        mRecyclerView.setAdapter(new SampleRecycler());

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        ProgressBar progress = findViewById(R.id.progress);

        Bundle bundle = getIntent().getExtras();
        mSeries = (Series) bundle.getSerializable(Constants.Series);
        SeriesID = bundle.getInt(Constants.SeriesID);
        SeasonNumber = bundle.getInt(Constants.SeasonNumber);

        if (SeriesID > 0)
            isExistDatabase = true;

        setTitle(mSeries.getSeriesName());


        if (mSeries.getSeasons().size() > 0) {
            ArrayList<Episode> episodes = new ArrayList<>();
            for (Episode e : mSeries.getEpisodes()) {
                if (e.getSeasonNumber() == SeasonNumber) {
                    episodes.add(e);
                }
            }

            mAdapter = new SeriesEpisodesAdapter(episodes, isExistDatabase);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new SeriesEpisodesAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, final int position) {
                    try {
                        final Episode episode = mAdapter.getItem(position);
                        EpisodeDetailFragment newFragment = EpisodeDetailFragment.newInstance(episode);
                        newFragment.setCustomEventListener(new EpisodeDetailFragment.OnCustomEventListener() {
                            @Override
                            public void onDismiss(Episode e) {
                                episode.setWatched(e.getWatched());
                                episode.setFavorite(e.getFavorite());
                                episode.setCollected(e.getCollected());
                                mAdapter.notifyItemChanged(position);
                            }
                        });
                        newFragment.show(getSupportFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                }
            });

            mAdapter.setOnWatchedClickListener(new SeriesEpisodesAdapter.OnWatchedClickListener() {
                @Override
                public void onWatchedClick(View itemView, int position, boolean isChecked) {
                    try {
                        Episode episode = mAdapter.getItem(position);
                        DbUtils.SetEpisodeWatched(SeriesEpisodesActivity.this, episode.getID(), isChecked);
                        isChanged = true;
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                }
            });


            progress.setVisibility(View.GONE);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isExistDatabase) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.series_episodes_actionbar_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_season_watched:
                try {
                    if (mAdapter != null) {
                        DbUtils.SetSeasonEpisodeAllWatched(SeriesEpisodesActivity.this, SeriesID, SeasonNumber, true);
                        mAdapter.setAllChecked(true);
                        isChanged = true;
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_season_not_watched:
                try {
                    if (mAdapter != null) {
                        DbUtils.SetSeasonEpisodeAllWatched(SeriesEpisodesActivity.this, SeriesID, SeasonNumber, false);
                        mAdapter.setAllChecked(false);
                        isChanged = true;
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_season_collected:
                try {
                    if (mAdapter != null) {
                        DbUtils.SetSeasonEpisodeAllCollected(SeriesEpisodesActivity.this, SeriesID, SeasonNumber, true);
                        mAdapter.setAllCollected(true);
                        isChanged = true;
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_season_not_collected:
                try {
                    if (mAdapter != null) {
                        DbUtils.SetSeasonEpisodeAllCollected(SeriesEpisodesActivity.this, SeriesID, SeasonNumber, false);
                        mAdapter.setAllCollected(false);
                        isChanged = true;
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_season_favorite:
                try {
                    if (mAdapter != null) {
                        DbUtils.SetSeasonEpisodeAllFavorite(SeriesEpisodesActivity.this, SeriesID, SeasonNumber, true);
                        mAdapter.setAllFavorite(true);
                        isChanged = true;
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_season_not_favorite:
                try {
                    if (mAdapter != null) {
                        DbUtils.SetSeasonEpisodeAllFavorite(SeriesEpisodesActivity.this, SeriesID, SeasonNumber, false);
                        mAdapter.setAllFavorite(false);
                        isChanged = true;
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_delete_season:
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.delete_season_are_you_sure);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                if (mAdapter != null) {
                                    DbUtils.DeleteSeason(SeriesEpisodesActivity.this, SeriesID, SeasonNumber);
                                    isChanged = true;
                                    finish();
                                }
                            } catch (Exception e) {
                                NLog.e(TAG, e);
                            }
                        }
                    });

                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();


                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isChanged) {
            BroadcastNotifier broadcastNotifier = new BroadcastNotifier(this);
            broadcastNotifier.broadcastIntentWithObject(Constants.STATE_UPDATE_SERIES, mSeries);
        }
    }


}
