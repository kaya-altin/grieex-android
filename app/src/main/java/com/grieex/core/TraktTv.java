package com.grieex.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.grieex.core.listener.OnTraktTvEventListener;
import com.grieex.enums.TraktResult;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Episode;
import com.grieex.model.tables.Season;
import com.grieex.model.tables.Series;
import com.sburba.tvdbapi.TvdbApi;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.Comment;
import com.uwetrottmann.trakt5.entities.SearchResult;
import com.uwetrottmann.trakt5.entities.Show;
import com.uwetrottmann.trakt5.entities.ShowIds;
import com.uwetrottmann.trakt5.entities.TrendingShow;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.Status;
import com.uwetrottmann.trakt5.services.Shows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbTV;
import info.movito.themoviedbapi.model.tv.TvSeries;
import retrofit2.Response;

public class TraktTv {
    private static final String TAG = TraktTv.class.getName();
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_LANG = "en";
    private static final String BASE_IMAGE_URL = "https://thetvdb.com/banners/";

    private OnTraktTvEventListener mListener;

    public TraktTv() {

    }


    public void Search(final String SeriesName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Series> returnList = new ArrayList<>();
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);

                Response<List<SearchResult>> response = trakt.search().textQueryShow(SeriesName, null, null, null, null, null, null, null, null, null, Extended.FULL, 1, DEFAULT_PAGE_SIZE).execute();
                if (response.isSuccessful()) {
                    List<SearchResult> shows = response.body();
                    for (SearchResult result : shows) {
                        returnList.add(parseSeries(result.show));
                    }
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(returnList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }


    public void Parse(String TraktId, String locale) {
        try {
            TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);

            Response<Show> showResponse = trakt.shows().summary(TraktId, Extended.FULL).execute();
            Response<List<com.uwetrottmann.trakt5.entities.Season>> seasons = trakt.seasons().summary(TraktId, Extended.FULLEPISODES).execute();

            TvdbApi tvdbApi = new TvdbApi(GrieeXSettings.TvDbApiKey, locale);

            Show show = showResponse.body();
            Series series = parseSeries(showResponse.body());

            if (show.ids != null) {
                if (!TextUtils.isEmpty(show.ids.imdb)) {
                    Collection<com.sburba.tvdbapi.model.Series> seriesTvDbList = tvdbApi.getSeriesFromImdbId(show.ids.imdb);
                    if (seriesTvDbList != null && seriesTvDbList.size() > 0) {
                        com.sburba.tvdbapi.model.Series seriesTvDb = seriesTvDbList.iterator().next();
                        if (!TextUtils.isEmpty(seriesTvDb.overview))
                            series.setOverview(seriesTvDb.overview);
                    }
                }

                if (show.ids.tvdb != null) {
                    Collection<com.sburba.tvdbapi.model.Episode> episodes = tvdbApi.getEpisodes(show.ids.tvdb);
                    series.setEpisodes(parseEpisodes(episodes, show.airs.timezone, show.airs.time));

                    Collection<com.sburba.tvdbapi.model.Actor> actors = tvdbApi.getActors(show.ids.tvdb);
                    series.setCast(parseActors(actors));
                }
            }

            series.setSeasons(parseSeasons(seasons.body()));

            if (mListener != null)
                mListener.onCompleted(series);
        } catch (Exception e) {
            NLog.e(TAG, e);
            if (mListener != null)
                mListener.onCompleted(null);
        }
    }

    public void ParseAsync(final String TraktId, final String locale) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
                Response<Show> showResponse = trakt.shows().summary(TraktId, Extended.FULL).execute();
                Response<List<com.uwetrottmann.trakt5.entities.Season>> seasons = trakt.seasons().summary(TraktId, Extended.FULLEPISODES).execute();

                TvdbApi tvdbApi = new TvdbApi(GrieeXSettings.TvDbApiKey, locale);

                Show show = showResponse.body();
                Series series = parseSeries(show);

                if (show.ids != null) {
                    if (!TextUtils.isEmpty(show.ids.imdb)) {
                        Collection<com.sburba.tvdbapi.model.Series> seriesTvDbList = tvdbApi.getSeriesFromImdbId(show.ids.imdb);
                        if (seriesTvDbList != null && seriesTvDbList.size() > 0) {
                            com.sburba.tvdbapi.model.Series seriesTvDb = seriesTvDbList.iterator().next();
                            if (!TextUtils.isEmpty(seriesTvDb.overview))
                                series.setOverview(seriesTvDb.overview);
                        }
                    }

                    if (show.ids.tvdb != null) {
                        Collection<com.sburba.tvdbapi.model.Episode> episodes = tvdbApi.getEpisodes(show.ids.tvdb);
                        series.setEpisodes(parseEpisodes(episodes, show.airs.timezone, show.airs.time));

                        Collection<com.sburba.tvdbapi.model.Actor> actors = tvdbApi.getActors(show.ids.tvdb);
                        series.setCast(parseActors(actors));
                    }
                }

                series.setSeasons(parseSeasons(seasons.body()));

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(series);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void ParseAsync2(final String TraktId, final String locale) {
        try {
            TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
            Response<Show> showResponse = trakt.shows().summary(TraktId, Extended.FULL).execute();
            //List<com.uwetrottmann.trakt5.entities.Season> seasons = trakt.seasons().summary(TraktId, Extended.FULLIMAGES);
            Show show = showResponse.body();

            TvdbApi tvdbApi = new TvdbApi(GrieeXSettings.TvDbApiKey, locale);
            //Collection<com.sburba.tvdbapi.model.Episode> episodes = tvdbApi.getEpisodes(show.ids.tvdb);
            Collection<com.sburba.tvdbapi.model.Actor> actors = tvdbApi.getActors(show.ids.tvdb);

            Series series = parseSeries(show);

            if (show.ids != null && !TextUtils.isEmpty(show.ids.imdb)) {
                Collection<com.sburba.tvdbapi.model.Series> seriesTvDbList = tvdbApi.getSeriesFromImdbId(show.ids.imdb);
                if (seriesTvDbList != null && seriesTvDbList.size() > 0) {
                    com.sburba.tvdbapi.model.Series seriesTvDb = seriesTvDbList.iterator().next();
                    if (!TextUtils.isEmpty(seriesTvDb.overview))
                        series.setOverview(seriesTvDb.overview);
                }
            }

            //series.setSeasons(parseSeasons(seasons));
            // series.setEpisodes(parseEpisodes(episodes, show.airs.timezone, show.airs.time));
            series.setCast(parseActors(actors));

            if (mListener != null)
                mListener.onCompleted(series);
        } catch (Exception e) {
            NLog.e(TAG, e);
            if (mListener != null)
                mListener.onCompleted(null);
        }
    }

    public void ParseRatingAsync(final String TraktId, final String locale) {
        try {
            TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
            Response<Show> response = trakt.shows().summary(TraktId, Extended.FULL).execute();

            if (response.isSuccessful()) {
                if (mListener != null)
                    mListener.onCompleted(parseSeries(response.body()));
            }
        } catch (Exception e) {
            NLog.e(TAG, e);
            if (mListener != null)
                mListener.onCompleted(null);
        }
    }

    public void getSeasons(final String TraktId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
                Response<List<com.uwetrottmann.trakt5.entities.Season>> response = trakt.seasons().summary(TraktId, Extended.FULL).execute();

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(parseSeasons(response.body()));
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void getTrending(final int page) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Series> returnList = new ArrayList<>();

                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
                Shows traktShows = trakt.shows();

                Response<List<TrendingShow>> response = traktShows.trending(page, DEFAULT_PAGE_SIZE, Extended.FULL).execute();
                if (response.isSuccessful()) {
                    List<TrendingShow> shows = response.body();
                    for (TrendingShow t : shows) {
                        returnList.add(parseSeries(t.show));
                    }
                } else {
                    if (response.code() == 401) {
                        // authorization required, supply a valid OAuth access token
                    } else {
                        // the request failed for some other reason
                    }
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(returnList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void getPopular(final int page) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Series> returnList = new ArrayList<>();

                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
                Shows traktShows = trakt.shows();

                Response<List<Show>> response = traktShows.popular(page, DEFAULT_PAGE_SIZE, Extended.FULL).execute();
                if (response.isSuccessful()) {
                    List<Show> shows = response.body();
                    for (Show t : shows) {
                        returnList.add(parseSeries(t));
                    }
                } else {
                    if (response.code() == 401) {
                        // authorization required, supply a valid OAuth access token
                    } else {
                        // the request failed for some other reason
                    }
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(returnList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void getCast(final int tvdbId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TvdbApi tvdbApi = new TvdbApi(GrieeXSettings.TvDbApiKey, DEFAULT_LANG);
                Collection<com.sburba.tvdbapi.model.Actor> actors = tvdbApi.getActors(tvdbId);

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(parseActors(actors));
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void getComments(final Integer showId, final int page, final int pageSize) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
                Shows traktShows = trakt.shows();

                Response<List<Comment>> response = traktShows.comments(String.valueOf(showId), page, pageSize, Extended.FULL).execute();

                if (response.isSuccessful()) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onCompleted(response.body());
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }


    public void getCommentReplies(final int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                List<Comment> comments = new ArrayList<>();
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey);
                Response<Comment> comment = trakt.comments().get(id).execute();
                comments.add(comment.body());

                Response<List<Comment>> replies = trakt.comments().replies(id).execute();
                comments.addAll(replies.body());

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(comments);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void addComment(final String accessToken, final int traktId, final String comment, final boolean isSpoiler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey).accessToken(accessToken);

                Comment c = new Comment();
                c.comment = comment;
                c.spoiler = isSpoiler;

                c.show = new Show();
                c.show.ids = ShowIds.trakt(traktId);

                Response<Comment> response = trakt.comments().post(c).execute();

                if (response.isSuccessful()) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onCompleted(response);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void updateComment(final String accessToken, final int commentId, final String comment, final boolean isSpoiler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey).accessToken(accessToken);

                Comment c = new Comment(comment, isSpoiler, false);

                Response<Comment> response = trakt.comments().update(commentId, c).execute();

                if (response.isSuccessful()) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onCompleted(response);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void deleteComment(final String accessToken, final int commentId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey).accessToken(accessToken);
                Response response = trakt.comments().delete(commentId).execute();

                if (response.isSuccessful()) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onCompleted(TraktResult.SUCCESS);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void replyComment(final String accessToken, final int commentId, final String comment, final boolean isSpoiler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey).accessToken(accessToken);
                Comment c = new Comment(comment, isSpoiler, false);
                Response<Comment> response = trakt.comments().postReply(commentId, c).execute();

                if (response.isSuccessful()) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onCompleted(response);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(null);
                });
            }
        });
    }

    public void likeComment(final String accessToken, final Comment comment) {
//        new AsyncTask<Void, Void, Integer>() {
//            protected void onPostExecute(Integer result) {
//                if (mListener != null)
//                    mListener.onCompleted(result);
//            }
//
//            @Override
//            protected Integer doInBackground(Void... params) {
//                try {
//                    TraktV2 trakt = new TraktV2(GrieeXSettings.TraktApiKey).accessToken(accessToken);
//
//                    Response<Comment> response;
//
//                    if (!comment.mylike)
//                        response = trakt.comments().postLike(comment.id, comment).execute();
//                    else
//                        response = trakt.comments().deleteLike(comment.id).execute();
//
//                    if (response.isSuccessful()) {
//                        return TraktResult.SUCCESS;
//                    } else {
//                        if (response.code() == 401) {
//                            return TraktResult.AUTH_ERROR;
//                        } else {
//                            return TraktResult.ERROR;
//                        }
//                    }
//
//                } catch (Exception e) {
//                    return TraktResult.ERROR;
//                }
//
//            }
//
//        }.execute();
    }

    private Series parseSeries(Show t) {
        Series s = new Series();
        s.setSeriesName(t.title);
        s.setOverview(t.overview);
        s.setFirstAired(Utils.parseString(t.first_aired));
        s.setNetwork(t.network);
        s.setImdbId(t.ids.imdb);
        s.setTmdbId(t.ids.tmdb);
        s.setTvdbId(t.ids.tvdb);
        s.setTraktId(t.ids.trakt);
        s.setLanguage(t.language);
        s.setCountry(t.country);
        s.setAirYear(t.year);
        s.setGenres(parseGenres(t.genres));
        s.setRuntime(Utils.parseString(t.runtime));
        s.setCertification(t.certification);

        if (t.airs != null) {
            s.setAirDay(t.airs.day);
            s.setAirTime(t.airs.time);
            s.setTimezone(t.airs.timezone);
        }
        s.setRating(t.rating);
        s.setVotes(t.votes);
        s.setStatus(parseStatus(t.status));
        s.setHomepage(t.homepage);
        s.setSeriesLastUpdate(Utils.parseString(t.updated_at));

//        if (t.images != null) {
//            s.setPoster(t.images.poster.medium);
//            s.setFanart(t.images.fanart.medium);
//        } else {

//        }

        if (t.ids != null) {
            if (t.ids.tmdb != null) {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);
                TvSeries ss = tmdb.getTvSeries().getSeries(t.ids.tmdb, DEFAULT_LANG, TmdbTV.TvMethod.images);
                if (ss != null) {
                    s.setPoster("https://image.tmdb.org/t/p/w342" + ss.getPosterPath());
                    s.setFanart("https://image.tmdb.org/t/p/w500" + ss.getBackdropPath());
                }
            } else if (t.ids.tvdb != null) {
//                TvdbApi tvdbApi = new TvdbApi(GrieeXSettings.TvDbApiKey, "en");
//                Collection<Banner> banners = tvdbApi.getBanners(t.ids.tvdb, 1);
//                if (banners != null) {
////                    s.setPoster("https://image.tmdb.org/t/p/w342" + banners.getPosterPath());
////                    s.setFanart("https://image.tmdb.org/t/p/w500" + ss.getBackdropPath());
//                }
            }
        }


        return s;
    }

    private ArrayList<Season> parseSeasons(List<com.uwetrottmann.trakt5.entities.Season> seasons) {
        ArrayList<Season> mSeasons = new ArrayList<>();
        if (seasons != null) {
            for (com.uwetrottmann.trakt5.entities.Season s : seasons) {
                Season ss = getSeason(s);
                if (ss != null)
                    mSeasons.add(ss);
            }
        }
        return mSeasons;
    }

    private Season getSeason(com.uwetrottmann.trakt5.entities.Season season) {
        Season s = new Season();
        // Durmuş SeriesId set edilecek.
        //s.setSeriesId();
        s.setOverview(season.overview);
        s.setNumber(season.number);
        //mihmih
//        if (season.images != null && season.images.poster != null)
//            s.setPoster(season.images.poster.medium);
        s.setAiredEpisodes(season.aired_episodes);
        s.setEpisodeCount(season.episode_count);
        s.setRating(Utils.parseString(season.rating));
        s.setVotes(Utils.parseString(season.votes));

        if (season.ids != null) {
            if (season.ids.tvdb != null)
                s.setTvdbId(season.ids.tvdb);
            if (season.ids.tmdb != null)
                s.setTmdbId(season.ids.tmdb);
            if (season.ids.trakt != null)
                s.setTraktId(season.ids.trakt);
        }

        return s;
    }

    private String parseStatus(Status status) {
        if (status != null) {
            if (status.name().equals("RETURNING")) {
                return "1";
            } else if (status.name().equals("ENDED")) {
                return "0";
            }
        }
        return "0";
    }

    private String parseGenres(List<String> genres) {
        if (genres == null)
            return "";

        StringBuilder sb = new StringBuilder();

        for (String s : genres) {
            if (!TextUtils.isEmpty(sb.toString())) {
                sb.append(", ");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private ArrayList<Episode> parseEpisodes(Collection<com.sburba.tvdbapi.model.Episode> episodes, String timeZone, String time) {
        ArrayList<Episode> returnEpisodes = new ArrayList<>();

        if (episodes != null) {
            for (com.sburba.tvdbapi.model.Episode episode : episodes) {
                Episode e = new Episode();
                e.setSeriesId(episode.seriesId);
                e.setEpisodeName(episode.name);
                e.setEpisodeNumber(episode.number);
                e.setFirstAired(DateUtils.ConvertDateToString(episode.firstAired));
                e.setFirstAiredMs(DateUtils.getMillisecondsLocale(episode.firstAired, timeZone, time));
                //Durmuş
                //e.setGuestStars(episode.guestStars);
                e.setOverview(episode.overview);
                e.setRating(Utils.parseString(episode.rating));
                e.setSeasonNumber(episode.seasonNumber);
                e.setAirsAfterSeason(episode.airsAfterSeason);
                e.setAirsBeforeEpisode(episode.airsBeforeEpisode);
                e.setAirsBeforeSeason(episode.airsBeforeSeason);
                if (!TextUtils.isEmpty(episode.filename) && !episode.filename.equals(BASE_IMAGE_URL))
                    e.setEpisodeImage(replaceHTTP(episode.filename));
                e.setLastUpdated((int) episode.lastUpdated);
                e.setTvdbSeasonId(episode.seasonId);
                e.setTvdbSeriesId(episode.seriesId);
                e.setTvdbEpisodeId(episode.id);

                returnEpisodes.add(e);
            }
        }
        return returnEpisodes;
    }

    private ArrayList<Cast> parseActors(Collection<com.sburba.tvdbapi.model.Actor> actors) {
        ArrayList<Cast> returnActors = new ArrayList<>();

        if (actors != null) {
            for (com.sburba.tvdbapi.model.Actor actor : actors) {
                Cast c = new Cast();
                c.setCastID(String.valueOf(actor.id));
                c.setName(actor.name);
                c.setCharacter(actor.role);
                if (!TextUtils.isEmpty(actor.getImageUrl()) && !actor.getImageUrl().equals(BASE_IMAGE_URL))
                    c.setImageUrl(replaceHTTP(actor.getImageUrl()));
                c.setUrl("");

                returnActors.add(c);
            }
        }
        return returnActors;
    }

    public void setTraktEventListener(OnTraktTvEventListener onTraktEventListener) {
        mListener = onTraktEventListener;

    }

    private String replaceHTTP(String value) {
        return value.replace("http://", "https://");
    }
}
