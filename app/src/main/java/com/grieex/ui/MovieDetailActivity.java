package com.grieex.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.grieex.R;
import com.grieex.adapter.CustomMenuAdapter;
import com.grieex.adapter.ImageSlideAdapter;
import com.grieex.core.ImportQueues;
import com.grieex.core.SearchResult;
import com.grieex.core.Tmdb;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.DbUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.CustomMenuItem;
import com.grieex.model.tables.Backdrop;
import com.grieex.model.tables.Movie;
import com.grieex.service.ServiceManager;
import com.grieex.ui.dialogs.AddToListDialog;
import com.grieex.ui.dialogs.CustomProgressDialog;
import com.grieex.ui.fragments.MovieDetailCastFragment;
import com.grieex.ui.fragments.MovieDetailFilesFragment;
import com.grieex.ui.fragments.MovieDetailGeneralFragment;
import com.grieex.ui.fragments.MovieDetailOtherFragment;
import com.grieex.widget.WrapContentHeightViewPager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.util.ArrayList;

public class MovieDetailActivity extends BaseActivity {
    private static final String TAG = MovieDetailActivity.class.getName();

    private Movie mMovie;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private static String[] CONTENT;
    private CustomProgressDialog progressDialog;

    private static final long ANIM_VIEWPAGER_DELAY = 5000;
    private static final long ANIM_VIEWPAGER_DELAY_USER_VIEW = 10000;
    private ViewPager viewPagerBackdrops;
    private boolean stopSliding = false;
    private Runnable animateViewPager;
    private Handler handler;
    private ArrayList<Backdrop> mBackdrops;

    private AppBarLayout appBarLayout;
    private FragmentStatePagerAdapter mAdapter;
    private CollapsingToolbarLayout collapsingToolbar;

    private CustomBroadcastReceiver mBroadcastReceiver;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        try {
            Toolbar toolbar = findViewById(R.id.myToolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);

            appBarLayout = findViewById(R.id.appBarLayout);
            collapsingToolbar = findViewById(R.id.collapsing_toolbar);

            CONTENT = getResources().getStringArray(R.array.MovieTabs);
            mMovie = (Movie) this.getIntent().getSerializableExtra(Constants.Movie);

            viewPager = findViewById(R.id.viewPager);
            viewPager.setOffscreenPageLimit(3);

            tabLayout = findViewById(R.id.tabLayout);
            tabLayout.setupWithViewPager(viewPager);

            viewPagerBackdrops = (WrapContentHeightViewPager) findViewById(R.id.viewPagerBackdrops);
            viewPagerBackdrops.setOnTouchListener(new View.OnTouchListener() {
                @Override
                @SuppressLint("ClickableViewAccessibility")
                public boolean onTouch(View v, MotionEvent event) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_CANCEL:
                            break;
                        case MotionEvent.ACTION_UP:
                            // calls when touch release on ViewPager
                            if (mBackdrops != null && mBackdrops.size() != 0) {
                                stopSliding = false;
                                startBackdropAnimation(mBackdrops.size());
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // calls when ViewPager touch
                            if (handler != null && !stopSliding) {
                                stopSliding = true;
                                handler.removeCallbacks(animateViewPager);
                            }
                            break;
                    }
                    return false;
                }
            });

            Load();

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
        if (mMovie.getID() > 0) {
            DatabaseHelper db = DatabaseHelper.getInstance(this);
            mMovie = db.getMovie(mMovie.getID());
            mBackdrops = db.getBackdrops(mMovie.getID());

            collapsingToolbar.setTitle(mMovie.getOriginalName());

            if (mMovie.getFiles().size() == 0)
                CONTENT = getResources().getStringArray(R.array.MovieTabs);
            else
                CONTENT = getResources().getStringArray(R.array.MovieTabs3);

            initTabs();

            if (mBackdrops == null | (mBackdrops != null && mBackdrops.size() == 0)) {
                getBackDrops();
            } else {
                File file = ImageLoader.getInstance().getDiskCache().get(mBackdrops.get(0).getUrl());
                if (file != null && file.exists())
                    appBarLayout.setExpanded(true, true);

                viewPagerBackdrops.setAdapter(new ImageSlideAdapter(this, mBackdrops, new ImageSlideAdapter.OnImageLoadingListener() {
                    @Override
                    public void onOnImageLoaded() {
                        appBarLayout.setExpanded(true, true);
                    }
                }));
            }
        } else {
            LoadFromTmdbOrImdb(mMovie);
        }
    }

    private void LoadFromTmdbOrImdb(Movie movie) {
        try {
            showProgress();
            Tmdb tmdb = new Tmdb();
            tmdb.setTmdbEventListener(new OnTmdbEventListener() {

                @Override
                public void onNotCompleted(Throwable error, String content) {
                    hideProgress();
                }

                @Override
                public void onCompleted(Object m) {
                    try {
                        if (m == null) {
                            hideProgress();
                            return;
                        }

                        Movie p = (Movie) m;
                        mMovie.setImdbNumber(p.getImdbNumber());
                        mMovie.setOriginalName(p.getOriginalName());
                        mMovie.setOtherName(p.getOtherName());
                        mMovie.setDirector(p.getDirector());
                        mMovie.setWriter(p.getWriter());
                        mMovie.setGenre(p.getGenre());
                        mMovie.setYear(p.getYear());
                        mMovie.setUserRating(p.getUserRating());
                        mMovie.setVotes(p.getVotes());
                        mMovie.setTmdbUserRating(p.getTmdbUserRating());
                        mMovie.setTmdbVotes(p.getTmdbVotes());
                        mMovie.setRunningTime(p.getRunningTime());
                        mMovie.setCountry(p.getCountry());
                        mMovie.setLanguage(p.getLanguage());
                        mMovie.setEnglishPlot(p.getEnglishPlot());
                        mMovie.setOtherPlot(p.getOtherPlot());
                        mMovie.setBudget(p.getBudget());
                        mMovie.setProductionCompany(p.getProductionCompany());
                        mMovie.setPoster(p.getPoster().replace("original", "w342"));
                        mMovie.setCast(p.getCast());
                        mMovie.setBackdrops(p.getBackdrops());
                        mMovie.setTrailers(p.getTrailers());
                        mMovie.setTmdbNumber(p.getTmdbNumber());
                        mMovie.setReleaseDate(p.getReleaseDate());


                        hideProgress();
                        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
                        collapsingToolbar.setTitle(mMovie.getOriginalName());


                        CONTENT = getResources().getStringArray(R.array.MovieTabs2);
                        initTabs();

                        mBackdrops = mMovie.getBackdrops();

                        viewPagerBackdrops.setAdapter(new ImageSlideAdapter(MovieDetailActivity.this, mBackdrops, new ImageSlideAdapter.OnImageLoadingListener() {
                            @Override
                            public void onOnImageLoaded() {
                                appBarLayout.setExpanded(true);
                            }
                        }));
                        startBackdropAnimation(mBackdrops.size());
                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }

                }
            });

            if (!TextUtils.isEmpty(movie.getTmdbNumber()))
                tmdb.ParseTmdbNumberAsync(Utils.parseInt(movie.getTmdbNumber()), GrieeXSettings.getLocale(this));
            else if (!TextUtils.isEmpty(movie.getImdbNumber()))
                tmdb.ParseImdbNumberAsync(movie.getImdbNumber(), GrieeXSettings.getLocale(this));
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    private void initTabs() {
        if (mAdapter == null) {
            mAdapter = new MovieDetailAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            if (viewPager != null)
                viewPager.setAdapter(mAdapter);

            tabLayout.setVisibility(View.VISIBLE);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void startBackdropAnimation(final int size) {
        try {
            if (handler != null)
                handler.removeCallbacks(animateViewPager);

            handler = new Handler();
            animateViewPager = new Runnable() {
                public void run() {
                    if (!stopSliding) {
                        if (viewPagerBackdrops.getCurrentItem() == size - 1) {
                            viewPagerBackdrops.setCurrentItem(0);
                        } else {
                            viewPagerBackdrops.setCurrentItem(viewPagerBackdrops.getCurrentItem() + 1, true);
                        }
                        handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY);
                    }
                }
            };

            handler.postDelayed(animateViewPager, ANIM_VIEWPAGER_DELAY_USER_VIEW);
        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    @Override
    public void onResume() {
        if (mBackdrops == null) {
            getBackDrops();
        } else {
            startBackdropAnimation(mBackdrops.size());
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        // if (task != null)
        // task.cancel(true);
        if (handler != null) {
            // Remove callback
            handler.removeCallbacks(animateViewPager);
        }
        super.onPause();
    }

    private void getBackDrops() {
        if (!Connectivity.isConnected(this) || mMovie.getID() <= 0)
            return;

        Tmdb tmdb = new Tmdb();
        tmdb.setTmdbEventListener(new OnTmdbEventListener() {
            @Override
            public void onCompleted(Object m) {
                try {
                    mBackdrops = (ArrayList<Backdrop>) m;
                    if (mBackdrops != null && mBackdrops.size() > 0) {
                        mMovie.setBackdrops(mBackdrops);
                        DatabaseHelper db = DatabaseHelper.getInstance(MovieDetailActivity.this);
                        db.fillBackdrops(mBackdrops, mMovie.getID(), Constants.CollectionType.Movie);

                        viewPagerBackdrops.setAdapter(new ImageSlideAdapter(MovieDetailActivity.this, mBackdrops, new ImageSlideAdapter.OnImageLoadingListener() {
                            @Override
                            public void onOnImageLoaded() {
                                appBarLayout.setExpanded(true, true);
                            }
                        }));
                        startBackdropAnimation(mBackdrops.size());
                    }

                    hideProgress();
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
            }

            @Override
            public void onNotCompleted(Throwable error, String content) {

            }
        });
        tmdb.getBackDrops(mMovie.getImdbNumber());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_add:
                if (!Utils.isProInstalled(MovieDetailActivity.this)) {
                    if (DbUtils.getMoviesCount(MovieDetailActivity.this) >= GrieeXSettings.FreeRecordLimitMovie) {
                        Toast.makeText(MovieDetailActivity.this, getResources().getString(R.string.alert7), Toast.LENGTH_LONG).show();
                        return false;
                    }
                }

                if (DbUtils.isMovieExistWithImdbNumber(MovieDetailActivity.this, mMovie.getImdbNumber())) {
                    new AlertDialog.Builder(MovieDetailActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage(getResources().getString(R.string.exist_movie_message))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    AddMovie();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();

                    return false;
                }

                AddMovie();
                return false;
            case R.id.action_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.delete_movie);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                            dbHelper.deleteMovieById(mMovie.getID());

                            BroadcastNotifier mBroadcaster = new BroadcastNotifier(MovieDetailActivity.this);

                            mBroadcaster.broadcastIntentWithObject(Constants.STATE_DELETE_MOVIE, mMovie);
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
            case R.id.action_get_info:
                if (!Connectivity.isConnected(this)) {
                    Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                    return true;
                }

                getTmdbInfo();

                return true;
            case R.id.action_get_backdrops:
                try {
                    if (!Connectivity.isConnected(this)) {
                        Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    showProgress();
                    getBackDrops();
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }
                return true;
            case R.id.action_add_to_list:
                AddToListDialog d = new AddToListDialog(this);
                d.setListType(1);
                d.setMovie(mMovie);
                d.showDialog();
                return true;
            case R.id.action_share:
                share(mMovie);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void AddMovie() {
        final ArrayList<CustomMenuItem> items = new ArrayList<>();
        items.add(new CustomMenuItem(1, -1, getString(R.string.add_to_collection)));
        items.add(new CustomMenuItem(2, -1, getString(R.string.add_to_list)));

        CustomMenuAdapter a = new CustomMenuAdapter(MovieDetailActivity.this, items);

        AlertDialog.Builder builder = new AlertDialog.Builder(MovieDetailActivity.this);
        builder.setCancelable(true);
        builder.setAdapter(a, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    CustomMenuItem menuItem = items.get(item);

                    if (menuItem.getId() == 1) {
                        Movie movie = new Movie();
                        movie.setContentProvider(Constants.ContentProviders.TMDb.value);
                        movie.setImdbNumber(mMovie.getImdbNumber());
                        movie.setTmdbNumber(mMovie.getTmdbNumber());
                        movie.setOriginalName(mMovie.getOriginalName());
                        movie.setSeen("0");
                        movie.setInsertDate(DateUtils.DateTimeNowString());
                        movie.setUpdateDate(DateUtils.DateTimeNowString());
                        movie.setArchivesNumber(DbUtils.getArchiveNumber(MovieDetailActivity.this));

                        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                        long _id = db.addMovie(movie);
                        movie.setID((int) _id);
                        mMovie.setID(movie.getID());

                        SearchResult sr = new SearchResult(String.valueOf(mMovie.getTmdbNumber()), mMovie.getOriginalName(), mMovie.getPoster(), mMovie.getYear());
                        ImportQueues.AddQueue(MovieDetailActivity.this, _id, sr, Constants.ContentProviders.TMDb);

                        BroadcastNotifier mBroadcaster = new BroadcastNotifier(MovieDetailActivity.this);
                        mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_MOVIE, movie);
                        Toast.makeText(MovieDetailActivity.this, getString(R.string.movie_added), Toast.LENGTH_SHORT).show();

                        ServiceManager.startImportDataService(getApplicationContext());

                        menuAddItem.setVisible(false);

                    } else if (menuItem.getId() == 2) {
                        Movie movie = new Movie();
                        movie.setContentProvider(Constants.ContentProviders.TMDb.value);
                        movie.setImdbNumber(mMovie.getImdbNumber());
                        movie.setTmdbNumber(mMovie.getTmdbNumber());
                        movie.setOriginalName(mMovie.getOriginalName());
                        movie.setSeen("0");
                        movie.setInsertDate(DateUtils.DateTimeNowString());
                        movie.setUpdateDate(DateUtils.DateTimeNowString());
                        DatabaseHelper db = DatabaseHelper.getInstance(getApplicationContext());
                        long _id = db.addMovie(movie);
                        movie.setID((int) _id);
                        mMovie.setID(movie.getID());

                        SearchResult sr = new SearchResult(String.valueOf(mMovie.getTmdbNumber()), mMovie.getOriginalName(), mMovie.getPoster(), mMovie.getYear());
                        ImportQueues.AddQueue(MovieDetailActivity.this, _id, sr, Constants.ContentProviders.TMDb);

                        BroadcastNotifier mBroadcaster = new BroadcastNotifier(MovieDetailActivity.this);
                        mBroadcaster.broadcastIntentWithObject(Constants.STATE_INSERT_MOVIE, movie);

                        ServiceManager.startImportDataService(getApplicationContext());

                        menuAddItem.setVisible(false);

                        //*******************************

                        AddToListDialog d = new AddToListDialog(MovieDetailActivity.this);
                        d.setListType(1);
                        d.setMovie(movie);
                        d.showDialog();
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

    private void getTmdbInfo() {
        showProgress();
        Tmdb tmdb = new Tmdb();
        tmdb.setTmdbEventListener(new OnTmdbEventListener() {

            @Override
            public void onNotCompleted(Throwable error, String content) {
                hideProgress();
            }

            @Override
            public void onCompleted(Object m) {
                try {
                    Movie p = (Movie) m;
                    mMovie.setContentProvider(p.getContentProvider());
                    mMovie.setTmdbNumber(p.getTmdbNumber());
                    mMovie.setOriginalName(p.getOriginalName());
                    mMovie.setDirector(p.getDirector());
                    mMovie.setWriter(p.getWriter());
                    mMovie.setGenre(p.getGenre());
                    mMovie.setYear(p.getYear());
                    mMovie.setUserRating(p.getUserRating());
                    mMovie.setVotes(p.getVotes());
                    mMovie.setTmdbUserRating(p.getTmdbUserRating());
                    mMovie.setTmdbVotes(p.getTmdbVotes());
                    mMovie.setRunningTime(p.getRunningTime());
                    mMovie.setCountry(p.getCountry());
                    mMovie.setLanguage(p.getLanguage());
                    mMovie.setEnglishPlot(p.getEnglishPlot());
                    mMovie.setBudget(p.getBudget());
                    mMovie.setProductionCompany(p.getProductionCompany());
                    mMovie.setReleaseDate(p.getReleaseDate());
                    mMovie.setPoster(p.getPoster());
                    mMovie.setUpdateDate(DateUtils.DateTimeNowString());

                    mMovie.setOtherName(p.getOtherName());
                    mMovie.setOtherPlot(p.getOtherPlot());
                    mMovie.setBackdrops(p.getBackdrops());
                    mMovie.setCast(p.getCast());

                    DatabaseHelper dbHelper = DatabaseHelper.getInstance(getApplicationContext());
                    dbHelper.updateMovie(mMovie);
                    dbHelper.fillCast(p.getCast(), mMovie.getID(), Constants.CollectionType.Movie);
                    dbHelper.fillBackdrops(p.getBackdrops(), mMovie.getID(), Constants.CollectionType.Movie);
                    dbHelper.fillTrailers(p.getTrailers(), mMovie.getID(), Constants.CollectionType.Movie);

                    BroadcastNotifier mBroadcaster = new BroadcastNotifier(MovieDetailActivity.this);
                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_MOVIE, mMovie);

                    hideProgress();
                } catch (Exception e) {
                    NLog.e(TAG, e);
                }

            }
        });
        if (!TextUtils.isEmpty(mMovie.getTmdbNumber()))
            tmdb.ParseTmdbNumberAsync(Utils.parseInt(mMovie.getTmdbNumber()), GrieeXSettings.getLocale(MovieDetailActivity.this));
        else
            tmdb.ParseImdbNumberAsync(mMovie.getImdbNumber(), GrieeXSettings.getLocale(MovieDetailActivity.this));
    }

    private void showProgress() {
        progressDialog = new CustomProgressDialog(MovieDetailActivity.this);
        progressDialog.setText(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.showDialog();
    }

    private void hideProgress() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    private MenuItem menuAddItem;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (mMovie.getID() > 0)
            inflater.inflate(R.menu.moviedetail_actionbar_menu, menu);
        else {
            inflater.inflate(R.menu.public_list_actionbar_menu, menu);
            menuAddItem = menu.findItem(R.id.action_add);
        }
        return super.onCreateOptionsMenu(menu);
    }

    class MovieDetailAdapter extends FragmentStatePagerAdapter {
        public MovieDetailAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MovieDetailGeneralFragment.newInstance(mMovie);
                case 1:
                    return MovieDetailCastFragment.newInstance(mMovie);
                case 2:
                    return MovieDetailOtherFragment.newInstance(mMovie);
                case 3:
                    return MovieDetailFilesFragment.newInstance(mMovie);
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

    //private static final int FACEBOOK_SHARE_REQUEST_CODE = 9752;

    private void share(final Movie m) {
        final ArrayList<CustomMenuItem> items = new ArrayList<>();
        items.add(new CustomMenuItem(1, R.drawable.facebook_icon, getString(R.string.via_imdb)));
        items.add(new CustomMenuItem(2, R.drawable.facebook_icon, getString(R.string.via_tmdb)));
        items.add(new CustomMenuItem(3, R.drawable.twitter_icon, getString(R.string.via_imdb)));
        items.add(new CustomMenuItem(4, R.drawable.twitter_icon, getString(R.string.via_tmdb)));

        CustomMenuAdapter a = new CustomMenuAdapter(MovieDetailActivity.this, items);
        a.setShowIcon(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MovieDetailActivity.this);
        builder.setTitle(R.string.share);
        builder.setCancelable(true);
        builder.setAdapter(a, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        try {
                            Movie movie = DatabaseHelper.getInstance(MovieDetailActivity.this).getMovie(m.getID());

                            CustomMenuItem menuItem = items.get(item);

                            switch (menuItem.getId()) {
                                case 1: {
                                    ShareDialog shareDialog = new ShareDialog(MovieDetailActivity.this);

                                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                .setQuote(m.getOriginalName() + " / " + m.getEnglishPlot())
                                                .setContentUrl(Uri.parse("http://www.imdb.com/title/" + movie.getImdbNumber()))
                                                .build();

                                        shareDialog.show(linkContent);
                                    }
                                    break;
                                }
                                case 2: {
                                    ShareDialog shareDialog = new ShareDialog(MovieDetailActivity.this);

                                    if (ShareDialog.canShow(ShareLinkContent.class)) {
                                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                                .setQuote(m.getOriginalName() + " / " + m.getEnglishPlot())
                                                .setContentUrl(Uri.parse("https://www.themoviedb.org/movie/" + movie.getTmdbNumber()))
                                                .build();

                                        shareDialog.show(linkContent);
                                    }
                                    break;
                                }
                                case 3: {
                                    TweetComposer.Builder builder = new TweetComposer.Builder(MovieDetailActivity.this);
                                    builder.text("http://www.imdb.com/title/" + movie.getImdbNumber());

                                    if (!TextUtils.isEmpty(m.getPoster())) {
                                        File myImageFile = ImageLoader.getInstance().getDiskCache().get(m.getPoster());
                                        if (myImageFile != null) {
                                            Uri myImageUri = Uri.fromFile(myImageFile);
                                            builder.image(myImageUri);
                                        }
                                    }
                                    builder.show();
                                    break;
                                }
                                case 4: {
                                    TweetComposer.Builder builder = new TweetComposer.Builder(MovieDetailActivity.this);
                                    builder.text("https://www.themoviedb.org/movie/" + movie.getTmdbNumber());

                                    if (!TextUtils.isEmpty(m.getPoster())) {
                                        File myImageFile = ImageLoader.getInstance().getDiskCache().get(m.getPoster());
                                        if (myImageFile != null) {
                                            Uri myImageUri = Uri.fromFile(myImageFile);
                                            builder.image(myImageUri);
                                        }
                                    }
                                    builder.show();
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            NLog.e(TAG, e);
                        }
                    }
                }

        );
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }


    // **************
    private class CustomBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                int iState = intent.getIntExtra(Constants.EXTENDED_DATA_STATUS, Constants.STATE_NOT_COMPLETED);
                if (iState == Constants.STATE_UPDATE_MOVIE) {
                    Object o = intent.getExtras().getSerializable(Constants.EXTENDED_DATA_OBJECT);
                    if (o != null && o instanceof Movie) {
                        Movie m = (Movie) o;

                        if (mMovie.getID() == m.getID()) {
                            Load();
                        }
                    }
                }
            } catch (Exception e) {
                NLog.e(TAG, e);
            }
        }
    }
}
