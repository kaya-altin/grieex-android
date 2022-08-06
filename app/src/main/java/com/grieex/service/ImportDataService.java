package com.grieex.service;

import android.content.Intent;

import com.grieex.core.Beyazperde;
import com.grieex.core.ImportQueues;
import com.grieex.core.Sinemalar;
import com.grieex.core.Tmdb;
import com.grieex.core.TraktTv;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.helper.WakefulIntentService;
import com.grieex.core.listener.OnBeyazperdeEventListener;
import com.grieex.core.listener.OnSinemalarEventListener;
import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Movie.COLUMNS;
import com.grieex.model.tables.Queue;
import com.grieex.model.tables.Series;


public class ImportDataService extends WakefulIntentService {
    private static final String TAG = ImportDataService.class.getName();
    private DatabaseHelper dbHelper;
    private BroadcastNotifier mBroadcaster;

    private String locale = "en";

    public ImportDataService() {
        super("ImportDataService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        dbHelper = DatabaseHelper.getInstance(getApplicationContext());
        mBroadcaster = new BroadcastNotifier(this);
        locale = GrieeXSettings.getLocale(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Import(intent);
    }

    private void Import(final Intent intent) {
        if (ImportQueues.GetQueuesCount(this) == 0) {
            mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_COMPLETED);
            stopSelf();
            return;
        }

        if (!Connectivity.isConnected(this)) {
            mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_NOT_COMPLETED);
            stopSelf();
            return;
        }

        try {
            mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_CONNECTING);

            final Queue q = ImportQueues.GetQueue(this);
            if (q != null) {
                ImportQueues.RemoveQueue(ImportDataService.this, q);

                switch (q.getType()) {
                    case TMDb:
                        final Tmdb tmdb = new Tmdb();
                        tmdb.setTmdbEventListener(new OnTmdbEventListener() {
                            @Override
                            public void onNotCompleted(Throwable error, String content) {
                                mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_NOT_COMPLETED);
                                Import(intent);
                            }

                            @Override
                            public void onCompleted(Object m) {
                                try {
                                    Movie mm = (Movie) m;

                                    Movie movie = new Movie();
                                    movie.LoadWithWhereColumn(ImportDataService.this, COLUMNS._ID, String.valueOf(q.getObjectID()));
                                    movie.setOriginalName(mm.getOriginalName());
                                    movie.setOtherName(mm.getOtherName());
                                    movie.setDirector(mm.getDirector());
                                    movie.setWriter(mm.getWriter());
                                    movie.setGenre(mm.getGenre());
                                    movie.setYear(mm.getYear());
                                    movie.setUserRating(mm.getUserRating());
                                    movie.setVotes(mm.getVotes());
                                    movie.setTmdbUserRating(mm.getTmdbUserRating());
                                    movie.setTmdbVotes(mm.getTmdbVotes());
                                    movie.setRunningTime(mm.getRunningTime());
                                    movie.setCountry(mm.getCountry());
                                    movie.setLanguage(mm.getLanguage());
                                    movie.setEnglishPlot(mm.getEnglishPlot());
                                    movie.setOtherPlot(mm.getOtherPlot());
                                    movie.setBudget(mm.getBudget());
                                    movie.setProductionCompany(mm.getProductionCompany());
                                    movie.setReleaseDate(mm.getReleaseDate());
                                    movie.setTmdbNumber(mm.getTmdbNumber());
                                    movie.setImdbNumber(mm.getImdbNumber());
                                    movie.setPoster(mm.getPoster());
                                    movie.setIsSyncWaiting("1");
                                    movie.setContentProvider(ContentProviders.TMDb.value);
                                    movie.setUpdateDate(DateUtils.DateTimeNowString());

                                    dbHelper.updateMovie(movie);

                                    dbHelper.fillCast(mm.getCast(), q.getObjectID(), Constants.CollectionType.Movie);
                                    dbHelper.fillBackdrops(mm.getBackdrops(), q.getObjectID(), Constants.CollectionType.Movie);
                                    dbHelper.fillTrailers(mm.getTrailers(), q.getObjectID(), Constants.CollectionType.Movie);

                                    // ImportQueues.RemoveQueue(ImportDataService.this,
                                    // q);

                                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_MOVIE, movie);
                                    Import(intent);
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        });
                        tmdb.Parse(Utils.parseInt(q.getUrl()), locale);
                        break;
                    case Beyazperde:
                        Beyazperde beyazperde = new Beyazperde();
                        beyazperde.setBeyazperdeEventListener(new OnBeyazperdeEventListener() {
                            @Override
                            public void onNotCompleted(Throwable error, String content) {
                                mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_NOT_COMPLETED);
                                Import(intent);
                            }

                            @Override
                            public void onCompleted(Object m) {
                                try {
                                    Movie moviee = (Movie) m;

                                    Movie movie = new Movie();
                                    movie.LoadWithWhereColumn(ImportDataService.this, COLUMNS._ID, String.valueOf(q.getObjectID()));
                                    movie.setOtherName(moviee.getOtherName());
                                    movie.setOtherPlot(moviee.getOtherPlot());
                                    movie.setUpdateDate(DateUtils.DateTimeNowString());
                                    dbHelper.updateMovie(movie);

                                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_MOVIE, movie);
                                    Import(intent);
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        });
                        beyazperde.Parse(q.getUrl());
                        break;
                    case Sinemalar:
                        Sinemalar sinemalar = new Sinemalar();
                        sinemalar.setCustomEventListener(new OnSinemalarEventListener() {
                            @Override
                            public void onNotCompleted(Throwable error, String content) {
                                mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_NOT_COMPLETED);
                                Import(intent);
                            }

                            @Override
                            public void onCompleted(Object m) {
                                try {
                                    Movie moviee = (Movie) m;

                                    Movie movie = new Movie();
                                    movie.LoadWithWhereColumn(ImportDataService.this, COLUMNS._ID, String.valueOf(q.getObjectID()));
                                    movie.setOtherName(moviee.getOtherName());
                                    movie.setOtherPlot(moviee.getOtherPlot());
                                    movie.setUpdateDate(DateUtils.DateTimeNowString());
                                    dbHelper.updateMovie(movie);

                                    mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_MOVIE, movie);
                                    Import(intent);
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        });
                        sinemalar.Parse(q.getUrl());
                        break;
                    case TMDbTv:
//					final TmdbTv tmdb = new TmdbTv();
//					tmdb.setCustomEventListener(new TmdbTv.OnTmdbTvEventListener() {
//						@Override
//						public void onNotCompleted(Throwable error, String content) {
//							mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_NOT_COMPLETED);
//							Import(intent);
//						}
//
//						@Override
//						public void onCompleted(Object m) {
//							try {
//								Series ss = (Series) m;
//
//								Series series = new Series();
//								series.LoadWithWhereColumn(ImportDataService.this, COLUMNS._ID, String.valueOf(q.getObjectID()));
//								series.setSeriesName(ss.getSeriesName());
//								series.setFirstAired(ss.getFirstAired());
//								series.setTmdbId(ss.getTmdbId());
//								series.setImdbId(ss.getImdbId());
//								series.setTvdbId(ss.getTvdbId());
//								series.setOverview(ss.getOverview());
//								series.setPoster(ss.getPoster());
//								series.setContentProvider(ContentProviders.TMDbTv.value);
//								series.setUpdateDate(DateTimeUtils.DateTimeNowString());
//
//
//								dbHelper.updateSeries(series);
////
////								dbHelper.fillCast(mm.getCast(), String.valueOf(q.getObjectID()));
////								dbHelper.fillBackdrops(mm.getBackdrops(), String.valueOf(q.getObjectID()));
////								dbHelper.fillTrailers(mm.getTrailers(),String.valueOf(q.getObjectID()));
//
//
//								mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_SERIES, series);
//								Import(intent);
//							} catch (Exception e) {
//								NLog.e(TAG, e);
//							}
//						}
//					});
//					tmdb.Parse(Utilities.parseInt(q.getUrl()),locale);
                        break;
                    case TraktTv:
                        TraktTv traktTv = new TraktTv();
                        traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                            @Override
                            public void onCompleted(Object m) {
                                try {
                                    if (m != null) {
                                        Series ss = (Series) m;

                                        Series series = new Series();
                                        series.LoadWithWhereColumn(ImportDataService.this, Series.COLUMNS._ID, String.valueOf(q.getObjectID()));
                                        series.setSeriesName(ss.getSeriesName());
                                        series.setOverview(ss.getOverview());
                                        series.setFirstAired(ss.getFirstAired());
                                        series.setNetwork(ss.getNetwork());
                                        series.setImdbId(ss.getImdbId());
                                        series.setTmdbId(ss.getTmdbId());
                                        series.setTraktId(ss.getTraktId());
                                        series.setTvdbId(ss.getTvdbId());
                                        series.setLanguage(ss.getLanguage());
                                        series.setCountry(ss.getCountry());
                                        series.setGenres(ss.getGenres());
                                        series.setRuntime(ss.getRuntime());
                                        series.setCertification(ss.getCertification());
                                        series.setAirDay(ss.getAirDay());
                                        series.setAirTime(ss.getAirTime());
                                        series.setTimezone(ss.getTimezone());
                                        series.setAirYear(ss.getAirYear());
                                        series.setStatus(ss.getStatus());
                                        series.setRating(ss.getRating());
                                        series.setVotes(ss.getVotes());
                                        series.setSeriesLastUpdate(ss.getSeriesLastUpdate());
                                        series.setPoster(ss.getPoster());
                                        series.setFanart(ss.getFanart());
                                        series.setHomepage(ss.getHomepage());


                                        series.setContentProvider(ContentProviders.TraktTv.value);
                                        series.setUpdateDate(DateUtils.DateTimeNowString());

                                        dbHelper.updateSeries(series);

                                        dbHelper.fillSeasons(ss.getSeasons(), q.getObjectID());
                                        dbHelper.fillEpisodes(ss.getEpisodes(), q.getObjectID());
                                        dbHelper.fillCast(ss.getCast(), q.getObjectID(), Constants.CollectionType.Series);

                                        mBroadcaster.broadcastIntentWithObject(Constants.STATE_UPDATE_SERIES, series);
                                        Import(intent);
                                    } else {
                                        mBroadcaster.broadcastIntentWithState(Constants.STATE_IMPORT_NOT_COMPLETED);
                                        Import(intent);
                                    }
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        });
                        traktTv.Parse(q.getUrl(), locale);
                        break;
                }

            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            ServiceManager.setImportDataServiceState(this, -1);
            stopSelf();
        }
    }

}
