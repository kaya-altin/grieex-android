package com.grieex.sync;

import android.accounts.Account;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;

import androidx.core.app.TaskStackBuilder;

import com.grieex.R;
import com.grieex.core.TraktTv;
import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.helper.BroadcastNotifier;
import com.grieex.helper.Connectivity;
import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Prefs;
import com.grieex.model.IncomingEpisode;
import com.grieex.model.tables.Series;
import com.grieex.receiver.NotificationPublisher;
import com.grieex.ui.MainActivity;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final String TAG = SyncAdapter.class.getName();
    private final Context mContext;
    private DatabaseHelper dbHelper;
    private final BroadcastNotifier mBroadcaster;
    private String locale = "en";

    SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mBroadcaster = new BroadcastNotifier(mContext);
        locale = GrieeXSettings.getLocale(mContext);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        try {
            long lastSyncDate = Prefs.with(mContext).getLong(Constants.Pref_Last_Sync_Date, 0L);

            if (DateUtils.differenceMinute(lastSyncDate) < 60)
                return;

            Prefs.with(mContext).save(Constants.Pref_Last_Sync_Date, DateUtils.DateTimeNow().getTime());

            mBroadcaster.broadcastIntentWithState(Constants.STATE_SYNC_STARTED);

            dbHelper = DatabaseHelper.getInstance(mContext);

            if (Connectivity.isConnected(mContext)) {
                ArrayList<Series> series = dbHelper.getSeries();

                if (series != null) {
                    for (Series s : series) {
                        final int ObjectID = s.getID();

                        TraktTv traktTv = new TraktTv();
                        traktTv.setTraktEventListener(new OnTraktTvEventListener() {
                            @Override
                            public void onCompleted(Object m) {
                                try {
                                    if (m != null) {
                                        Series ss = (Series) m;

                                        Series series = new Series();
                                        series.LoadWithWhereColumn(mContext, Series.COLUMNS._ID, String.valueOf(ObjectID));
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

                                        series.setContentProvider(Constants.ContentProviders.TraktTv.value);
                                        series.setUpdateDate(DateUtils.DateTimeNowString());

                                        dbHelper.updateSeries(series);

                                        dbHelper.fillSeasons(ss.getSeasons(), ObjectID);
                                        dbHelper.fillEpisodes(ss.getEpisodes(), ObjectID);
                                        dbHelper.fillCast(ss.getCast(), ObjectID, Constants.CollectionType.Series);
                                    }
                                } catch (Exception e) {
                                    NLog.e(TAG, e);
                                }
                            }
                        });
                        traktTv.Parse(String.valueOf(s.getTraktId()), locale);
                    }
                }
            }


            int notificationTime = GrieeXSettings.getNotificationTime(mContext);
            if (notificationTime != -1) {
                ArrayList<IncomingEpisode> incomingEpisodes = dbHelper.getIncomingEpisodes();
                for (IncomingEpisode s : incomingEpisodes) {
                    String date = DateUtils.getDateFormat(s.getFirstAiredMs(), Constants.DATE_FORMAT12);
                    scheduleNotification(s.getId(), getNotification(s.getSeriesName(), date, s.getPoster()), s.getFirstAiredMs());
                }
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
        } finally {
            mBroadcaster.broadcastIntentWithState(Constants.STATE_SYNC_ENDED);
        }
    }

    private void scheduleNotification(int notificationId, Notification notification, long firstAiredMs) {
        int notificationTime = GrieeXSettings.getNotificationTime(mContext);

        Intent notificationIntent = new Intent(mContext, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = firstAiredMs - TimeUnit.MINUTES.toMillis(notificationTime);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String title, String content, String poster) {
        Notification.Builder builder = new Notification.Builder(mContext)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher);

        Intent resultIntent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }
}
