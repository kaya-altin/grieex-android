package com.grieex.ui;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.grieex.R;
import com.grieex.adapter.CustomMenuAdapter;
import com.grieex.core.ImportQueues;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.CustomMenuItem;
import com.grieex.model.tables.Series;
import com.grieex.service.ServiceManager;
import com.grieex.ui.dialogs.AddToListDialog;
import com.grieex.ui.dialogs.CustomProgressDialog;
import com.grieex.ui.fragments.SeriesDetailCastFragment;
import com.grieex.ui.fragments.SeriesDetailInfoFragment;
import com.grieex.ui.fragments.SeriesDetailSeasonsFragment;
import com.grieex.widget.AspectRatioImageView;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeriesDetailActivity extends BaseActivity {
    private static final String TAG = SeriesDetailActivity.class.getName();
    private static String[] CONTENT;
    private Series mSeries;
    private CustomProgressDialog progressDialog;
    private CallbackManager callbackManager;
    private DatabaseHelper dbHelper;
    private boolean isExistDatabase = true;
    private CustomBroadcastReceiver mBroadcastReceiver;
    private AppBarLayout appBarLayout;
    private FragmentStatePagerAdapter mAdapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private AspectRatioImageView poster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_series_detail);
        try {
            Toolbar toolbar = findViewById(R.id.myToolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);

            appBarLayout = findViewById(R.id.appBarLayout);


            CONTENT = getResources().getStringArray(R.array.SeriesTabs);
            viewPager = findViewById(R.id.viewPager);
            viewPager.setOffscreenPageLimit(2);

            tabLayout = findViewById(R.id.tabLayout);
            tabLayout.setupWithViewPager(viewPager);

            mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            dbHelper = DatabaseHelper.getInstance(this);
            poster = findViewById(R.id.poster);

            Bundle bundle = getIntent().getExtras();
            String SeriesID = bundle.getString(Constants.SeriesID);
            String ImdbOrTraktID = bundle.getString(Constants.ImdbOrTraktID);
            isExistDatabase = bundle.getBoolean(Constants.IsExistDatabase, false);

            if (!TextUtils.isEmpty(SeriesID)) {
                mSeries = dbHelper.getSeries(Integer.parseInt(SeriesID));
                Load();
            } else if (!TextUtils.isEmpty(ImdbOrTraktID)) {
                if (isExistDatabase) {
                    SeriesID = dbHelper.getSeriesIdFromtTraktTvIdOrImdbId(ImdbOrTraktID);
                    if (!TextUtils.isEmpty(SeriesID)) {
                        mSeries = dbHelper.getSeries(Integer.parseInt(SeriesID));
                        Load();
                    } else {
                        getSeries(ImdbOrTraktID);
                    }
                } else {
                    getSeries(ImdbOrTraktID);
                }
            } else {
                return;
            }

            callbackManager = CallbackManager.Factory.create();

            if (mBroadcastReceiver == null) {
                IntentFilter statusIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                mBroadcastReceiver = new CustomBroadcastReceiver();
                LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, statusIntentFilter);
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void Load() {
        if (mSeries != null) {
            tabLayout.setVisibility(View.VISIBLE);

            CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
            collapsingToolbar.setTitle(mSeries.getSeriesName());

            if (!TextUtils.isEmpty(mSeries.getFanart())) {
                Glide.with(SeriesDetailActivity.this)
                        .load(mSeries.getFanart())
                        .into(poster);

                Glide.with(SeriesDetailActivity.this)
                        .asFile()
                        .load(mSeries.getFanart())
                        .into(new CustomTarget<File>() {
                            @Override
                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                appBarLayout.setExpanded(true, true);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

            }

            if (viewPager != null)
                viewPager.setAdapter(mAdapter);

            int EpisodeWatchedCount = DbUtils.getEpisodeWatchedCount(this, mSeries.getID());
            if (EpisodeWatchedCount > 0)
                viewPager.setCurrentItem(1);
        }

//        for (int i = 0; i <= mAdapter.getCount(); i++) {
//            Fragment f = mAdapter.getItem(i);
//            if (f instanceof SeriesDetailInfoFragment) {
//                ((SeriesDetailInfoFragment) f).setSeries(mSeries);
//                ((SeriesDetailInfoFragment) f).Load();
//            } else if (f instanceof SeriesDetailSeasonsFragment) {
//                ((SeriesDetailSeasonsFragment) f).setSeries(mSeries);
//                ((SeriesDetailSeasonsFragment) f).Load();
//            } else if (f instanceof SeriesDetailCastFragment) {
//                ((SeriesDetailCastFragment) f).setSeries(mSeries);
//                ((SeriesDetailCastFragment) f).Load();
//            } else if (f instanceof SeriesDetailCommentsFragment) {
//                ((SeriesDetailCommentsFragment) f).Load();
//            }
//        }


    }

    private void getSeries(String id) {
        try {
            showProgress();

            TraktTv traktTv = new TraktTv();
            traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                @Override
                public void onCompleted(Object m) {
                    try {
                        if (m != null) {
                            mSeries = (Series) m;
                            Load();
                        }
                        hideProgress();
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                }
            });
            traktTv.ParseAsync(id, GrieeXSettings.getLocale(this));
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mBroadcastReceiver != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
                mBroadcastReceiver = null;
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (isExistDatabase) {
            inflater.inflate(R.menu.seriesdetail_actionbar_menu, menu);
        } else {
            inflater.inflate(R.menu.seriesdetail_actionbar_menu2, menu);

            final MenuItem menuAddItem = menu.findItem(R.id.action_add);
            menuAddItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (!Utils.isProInstalled(SeriesDetailActivity.this)) {
                        if (DbUtils.getSeriesCount(SeriesDetailActivity.this) >= GrieeXSettings.FreeRecordLimitSeries) {
                            Toast.makeText(SeriesDetailActivity.this, getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }

                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());

                    executor.execute(() -> {
                        if (mSeries == null)
                            return;

                        mSeries.setIsExisting(true);
                        mSeries.setContentProvider(Constants.ContentProviders.TraktTv.value);
                        mSeries.setInsertDate(DateUtils.DateTimeNowString());
                        mSeries.setUpdateDate(DateUtils.DateTimeNowString());

                        long _id = DatabaseHelper.getInstance(getApplicationContext()).addSeries(mSeries);
                        mSeries.setID((int) _id);

                        ImportQueues.AddQueue(SeriesDetailActivity.this, _id, String.valueOf(mSeries.getTraktId()), Constants.ContentProviders.TraktTv);

                        BroadcastNotifier mBroadcaster = new BroadcastNotifier(SeriesDetailActivity.this);
                        mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_SERIES, mSeries);


                        handler.post(() -> {
                            menuAddItem.setVisible(false);

                            Toast.makeText(SeriesDetailActivity.this, getResources().getString(R.string.series_added), Toast.LENGTH_SHORT).show();

                            ServiceManager.startImportDataService(getApplicationContext());
                        });

                    });
                    return false;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_get_info:
                showProgress();

                TraktTv traktTv = new TraktTv();
                traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                    @Override
                    public void onCompleted(Object m) {
                        try {
                            if (m != null) {
                                Series ss = (Series) m;
                                updateSeries(ss);
                                Load();
                            } else {
                                hideProgress();
                            }
                        } catch (Exception e) {
                            NLog.e(TAG, e);
                        }
                    }
                });
                traktTv.ParseAsync(String.valueOf(mSeries.getTraktId()), GrieeXSettings.getLocale(this));
                return true;
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.delete_series);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dbHelper.deleteSeriesById(mSeries.getID());

                            BroadcastNotifier mBroadcaster = new BroadcastNotifier(SeriesDetailActivity.this);
                            mBroadcaster.broadcastIntentWithObject(Constants.STATE_DELETE_SERIES, mSeries);
                            finish();
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
                return true;
            case R.id.action_add_to_list:
                AddToListDialog d = new AddToListDialog(this);
                d.setListType(2);
                d.setSeries(mSeries);
                d.showDialog();
                return true;
            case R.id.action_share:
                share(mSeries);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showProgress() {
        progressDialog = new CustomProgressDialog(SeriesDetailActivity.this);
        progressDialog.setText(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.showDialog();
    }

    private void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    private void updateSeries(final Series ss) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                mSeries.setSeriesName(ss.getSeriesName());
                mSeries.setOverview(ss.getOverview());
                mSeries.setFirstAired(ss.getFirstAired());
                mSeries.setNetwork(ss.getNetwork());
                mSeries.setImdbId(ss.getImdbId());
                mSeries.setTmdbId(ss.getTmdbId());
                mSeries.setTraktId(ss.getTraktId());
                mSeries.setTvdbId(ss.getTvdbId());
                mSeries.setLanguage(ss.getLanguage());
                mSeries.setCountry(ss.getCountry());
                mSeries.setGenres(ss.getGenres());
                mSeries.setRuntime(ss.getRuntime());
                mSeries.setCertification(ss.getCertification());
                mSeries.setAirDay(ss.getAirDay());
                mSeries.setAirTime(ss.getAirTime());
                mSeries.setTimezone(ss.getTimezone());
                mSeries.setAirYear(ss.getAirYear());
                mSeries.setStatus(ss.getStatus());
                mSeries.setRating(ss.getRating());
                mSeries.setVotes(ss.getVotes());
                mSeries.setSeriesLastUpdate(ss.getSeriesLastUpdate());
                mSeries.setPoster(ss.getPoster());
                mSeries.setFanart(ss.getFanart());
                mSeries.setHomepage(ss.getHomepage());

                mSeries.setContentProvider(Constants.ContentProviders.TraktTv.value);
                mSeries.setUpdateDate(DateUtils.DateTimeNowString());

                dbHelper.updateSeries(mSeries);

                dbHelper.fillSeasons(ss.getSeasons(), mSeries.getID());
                dbHelper.fillEpisodes(ss.getEpisodes(), mSeries.getID());
                dbHelper.fillCast(ss.getCast(), mSeries.getID(), Constants.CollectionType.Series);

                handler.post(() -> {
                    hideProgress();
                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(SeriesDetailActivity.this);
                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_SERIES, mSeries);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    hideProgress();
                });
            }
        });
    }

    private void share(final Series m) {
        final ArrayList<CustomMenuItem> items = new ArrayList<>();
        items.add(new CustomMenuItem(1, R.drawable.facebook_icon, getString(R.string.via_imdb)));
        items.add(new CustomMenuItem(2, R.drawable.facebook_icon, getString(R.string.via_tmdb)));
        items.add(new CustomMenuItem(3, R.drawable.twitter_icon, getString(R.string.via_imdb)));
        items.add(new CustomMenuItem(4, R.drawable.twitter_icon, getString(R.string.via_tmdb)));

        CustomMenuAdapter a = new CustomMenuAdapter(this, items);
        a.setShowIcon(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.share);
        builder.setCancelable(true);
        builder.setAdapter(a, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    Series series = dbHelper.getSeries(m.getID());

                    CustomMenuItem menuItem = items.get(item);

                    switch (menuItem.getId()) {
                        case 1:
                            if (ShareDialog.canShow(ShareLinkContent.class)) {
                                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                        .setQuote(m.getSeriesName() + " / " + m.getOverview())
                                        .setContentUrl(Uri.parse("http://www.imdb.com/title/" + series.getImdbId()))
                                        .build();

                                ShareDialog shareDialog = new ShareDialog(SeriesDetailActivity.this);
                                shareDialog.show(linkContent);
                            }
                            break;
                        case 2:
                            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                    .setQuote(m.getSeriesName() + " / " + m.getOverview())
                                    .setContentUrl(Uri.parse("https://www.themoviedb.org/movie/" + series.getTmdbId()))
                                    .build();

                            ShareDialog shareDialog = new ShareDialog(SeriesDetailActivity.this);
                            shareDialog.show(linkContent);
                            break;
                        case 3: {
                            TweetComposer.Builder builder = new TweetComposer.Builder(SeriesDetailActivity.this);
                            builder.text("http://www.imdb.com/title/" + series.getTmdbId());

                            if (!TextUtils.isEmpty(m.getPoster())) {
                                Glide.with(SeriesDetailActivity.this)
                                        .asFile()
                                        .load(m.getPoster())
                                        .into(new CustomTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                Uri myImageUri = Uri.fromFile(resource);
                                                builder.image(myImageUri);
                                                builder.show();
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });

                            } else {
                                builder.show();
                            }
                            break;
                        }
                        case 4: {
                            TweetComposer.Builder builder = new TweetComposer.Builder(SeriesDetailActivity.this);
                            builder.text("https://www.themoviedb.org/show/" + series.getImdbId());

                            if (!TextUtils.isEmpty(m.getPoster())) {
                                Glide.with(SeriesDetailActivity.this)
                                        .asFile()
                                        .load(m.getPoster())
                                        .into(new CustomTarget<File>() {
                                            @Override
                                            public void onResourceReady(@NonNull File resource, @Nullable Transition<? super File> transition) {
                                                Uri myImageUri = Uri.fromFile(resource);
                                                builder.image(myImageUri);
                                                builder.show();
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {

                                            }
                                        });

                            } else {
                                builder.show();
                            }

                            break;
                        }
                    }
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return SeriesDetailInfoFragment.newInstance(mSeries);
                case 1:
                    return SeriesDetailSeasonsFragment.newInstance(mSeries);
                case 2:
                    return SeriesDetailCastFragment.newInstance(mSeries);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length];
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }

    // **************
    private class CustomBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NOT_COMPLETED);
                if (iState == Constants.STATE_UPDATE_SERIES) {
                    Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                    if (o != null && o instanceof Series) {
                        Series m = (Series) o;

                        if (mSeries != null && mSeries.getID() == m.getID()) {
                            mSeries = dbHelper.getSeries(m.getID());
                            mAdapter.notifyDataSetChanged();
                            //Load();
                        }
                    }
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }

}
