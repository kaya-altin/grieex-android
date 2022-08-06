package com.grieex.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.grieex.R;
import com.grieex.core.Imdb;
import com.grieex.core.Tmdb;
import com.grieex.core.listener.OnImdbEventListener;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Constants;
import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.helper.Utils;
import com.grieex.helper.WakefulIntentService;
import com.grieex.model.tables.Backdrop;
import com.grieex.model.tables.Movie;
import com.grieex.ui.MainActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbFind.ExternalSource;
import info.movito.themoviedbapi.model.Artwork;
import info.movito.themoviedbapi.model.FindResults;
import info.movito.themoviedbapi.model.MovieImages;

public class BatchProcessingService extends WakefulIntentService {
    private static final String TAG = BatchProcessingService.class.getName();
    private BroadcastNotifier mBroadcaster;
    private DatabaseHelper dbHelper;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    private String locale = "en";

    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    private int progress = 0;

    private WifiManager.WifiLock wifiLock = null;

    public BatchProcessingService() {
        super("BatchProcessingService");
    }

    @SuppressLint("WifiManagerLeak")
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        mBroadcaster = new BroadcastNotifier(this);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(false).cacheOnDisk(true).considerExifParams(true).build();

        locale = GrieeXSettings.getLocale(this);

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.CHANNEL_ID, "GrieeX", NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the notification channel.
            notificationChannel.setDescription("GrieeX");
            notificationChannel.enableLights(false);
            notificationChannel.setVibrationPattern(null);
            notificationChannel.enableVibration(false);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }


        mBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID);
        mBuilder.setContentTitle("GrieeX");
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS)
                .setVibrate(null);


        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "MyWifiLock");
        if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
    }

    @Override
    public void onDestroy() {
        Prefs.with(this).save(Constants.BATCH_PROCESSING_MOVIE_INFOS, false);
        Prefs.with(this).save(Constants.BATCH_PROCESSING_BACKDROPS, false);
        Prefs.with(this).save(Constants.BATCH_PROCESSING_POSTERS, false);
        super.onDestroy();
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        int id = 123654987;
        try {
            ArrayList<Movie> movies = dbHelper.getMovies();
            int progressMax = movies.size();

            mBuilder.setProgress(progressMax, 0, false);
            mNotifyManager.notify(id, mBuilder.build());

            boolean movie_info = Prefs.with(this).getBoolean(Constants.BATCH_PROCESSING_MOVIE_INFOS, false);
            boolean movie_backdrop = Prefs.with(this).getBoolean(Constants.BATCH_PROCESSING_BACKDROPS, false);
            boolean movie_poster = Prefs.with(this).getBoolean(Constants.BATCH_PROCESSING_POSTERS, false);

            for (final Movie movie : movies) {
                try {
                    if (isCancelled) {
                        break;
                    }

                    progress++;

                    mBuilder.setProgress(progressMax, progress, false);
                    mBuilder.setContentText(movie.getOriginalName());
                    mNotifyManager.notify(id, mBuilder.build());
                    if (movie_info) {
                        Tmdb tmdb = new Tmdb();
                        tmdb.setTmdbEventListener(new OnTmdbEventListener() {

                            @Override
                            public void onNotCompleted(Throwable error, String content) {
                                //mBroadcaster.broadcastIntentWithState(Constants.STATE_BATCH_PROCESSING_NOT_COMPLETED);
                                //stopSelf();
                            }

                            @Override
                            public void onCompleted(Object m) {
                                try {
                                    Movie p = (Movie) m;
                                    movie.setTmdbNumber(p.getTmdbNumber());
                                    movie.setOriginalName(p.getOriginalName());
                                    movie.setDirector(p.getDirector());
                                    movie.setWriter(p.getWriter());
                                    movie.setGenre(p.getGenre());
                                    movie.setYear(p.getYear());
                                    movie.setUserRating(p.getUserRating());
                                    movie.setVotes(p.getVotes());
                                    movie.setImdbUserRating(p.getImdbUserRating());
                                    movie.setImdbVotes(p.getImdbVotes());
                                    movie.setTmdbUserRating(p.getTmdbUserRating());
                                    movie.setTmdbVotes(p.getTmdbVotes());
                                    movie.setRunningTime(p.getRunningTime());
                                    movie.setCountry(p.getCountry());
                                    movie.setLanguage(p.getLanguage());
                                    movie.setEnglishPlot(p.getEnglishPlot());
                                    movie.setBudget(p.getBudget());
                                    movie.setProductionCompany(p.getProductionCompany());
                                    movie.setPoster(p.getPoster());
                                    movie.setReleaseDate(p.getReleaseDate());
                                    movie.setUpdateDate(DateUtils.DateTimeNowString());
                                    movie.setContentProvider(ContentProviders.TMDb.value);

                                    Imdb imdb = new Imdb();
                                    imdb.setImdbEventListener(new OnImdbEventListener() {
                                        @Override
                                        public void onNotCompleted(Throwable error, String content) {

                                        }

                                        @Override
                                        public void onCompleted(Object m) {
                                            try {
                                                Movie p = (Movie) m;
                                                movie.setUserRating(p.getUserRating());
                                                movie.setVotes(p.getVotes());
                                                movie.setImdbUserRating(p.getUserRating());
                                                movie.setImdbVotes(p.getVotes());
                                            } catch (Exception e) {
                                                //NLog.e(TAG, e);
                                            }
                                        }
                                    });
                                    imdb.ParseRating(movie.getImdbNumber());

                                    dbHelper.updateMovie(movie);
                                    dbHelper.fillCast(p.getCast(), movie.getID(), Constants.CollectionType.Movie);
                                    dbHelper.fillBackdrops(p.getBackdrops(), movie.getID(), Constants.CollectionType.Movie);
                                    dbHelper.fillTrailers(p.getTrailers(), movie.getID(), Constants.CollectionType.Movie);
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        });
                        if (!TextUtils.isEmpty(movie.getTmdbNumber()))
                            tmdb.Parse(Utils.parseInt(movie.getTmdbNumber()), locale);
                        else
                            tmdb.ParseImdbNumber(movie.getImdbNumber(), locale);
                    }

                    //-------------------------------------------------------------------

                    if (movie_backdrop) {
                        TmdbApi tmdbApi = new TmdbApi(GrieeXSettings.TmdbApiKey);
                        if (!TextUtils.isEmpty(movie.getTmdbNumber())) {
                            int tmdbNumber = Utils.parseInt(movie.getTmdbNumber());

                            String imdbnumber = movie.getImdbNumber();
                            if (!imdbnumber.contains("tt")) {
                                imdbnumber = "tt" + imdbnumber;
                            }

                            MovieImages images = tmdbApi.getMovies().getImages(tmdbNumber, null);
                            ArrayList<Backdrop> mBackdrops = new ArrayList<>();

                            for (Artwork artwork : images.getBackdrops()) {
                                Backdrop backdrop = new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), imdbnumber, movie.getID(), Constants.CollectionType.Movie);
                                mBackdrops.add(backdrop);
                                imageLoader.loadImageSync(backdrop.getUrl(), options);
                            }

                            dbHelper.fillBackdrops(mBackdrops, movie.getID(), Constants.CollectionType.Movie);
                        } else {
                            String imdbnumber = movie.getImdbNumber();
                            if (!imdbnumber.contains("tt")) {
                                imdbnumber = "tt" + imdbnumber;
                            }

                            FindResults result = tmdbApi.getFind().find(imdbnumber, ExternalSource.imdb_id, null);

                            ArrayList<Backdrop> mBackdrops = new ArrayList<>();
                            if (result.getMovieResults().size() > 0) {
                                MovieImages images = tmdbApi.getMovies().getImages(result.getMovieResults().get(0).getId(), null);

                                for (Artwork artwork : images.getBackdrops()) {
                                    Backdrop backdrop = new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), imdbnumber, movie.getID(), Constants.CollectionType.Movie);
                                    mBackdrops.add(backdrop);
                                    // imageLoader.loadImageSync(backdrop.getUrl());
                                    imageLoader.loadImageSync(backdrop.getUrl(), options);
                                }

                                dbHelper.fillBackdrops(mBackdrops, movie.getID(), Constants.CollectionType.Movie);
                            }
                        }
                    }

                    //-------------------------------------------------------------------

                    if (movie_poster) {
                        if (!TextUtils.isEmpty(movie.getPoster())) {
                            imageLoader.loadImageSync(movie.getPoster(), options);
                        }
                    }

                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_BATCH_PROCESSING_UPDATE_MOVIE, movie, progressMax, progress);
                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_MOVIE, movie);
                } catch (Exception e) {
                    // NLog.e(TAG, e);
                }
            }

            mBroadcaster.broadcastIntentWithState(Constants.STATE_BATCH_PROCESSING_COMPLETED);

        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            Prefs.with(this).save(Constants.BATCH_PROCESSING_MOVIE_INFOS, false);
            Prefs.with(this).save(Constants.BATCH_PROCESSING_BACKDROPS, false);
            Prefs.with(this).save(Constants.BATCH_PROCESSING_POSTERS, false);

            mBuilder.setContentText(getString(R.string.batch_processing_completed));
            // Removes the progress bar
            mBuilder.setProgress(0, 0, false);
            mNotifyManager.notify(id, mBuilder.build());

            // release the WifiLock
            if (wifiLock != null) {
                if (wifiLock.isHeld()) {
                    wifiLock.release();
                    //Log.i("ServiceAlarmBroadcastReceiver", "WiFi Lock released!");
                }
            }
            stopSelf();
        }
    }
}
