package com.grieex.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.grieex.core.listener.OnTmdbTvEventListener;
import com.grieex.helper.Constants;
import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Backdrop;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Series;
import com.grieex.model.tables.Trailer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbFind.ExternalSource;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.TmdbTV;
import info.movito.themoviedbapi.TvResultsPage;
import info.movito.themoviedbapi.model.Artwork;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.FindResults;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.Language;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.MovieImages;
import info.movito.themoviedbapi.model.ProductionCompany;
import info.movito.themoviedbapi.model.ProductionCountry;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvSeries;

public class TmdbTv {
    private static final String TAG = TmdbTv.class.getName();
    private static final String DEFAULT_LANG = "en";

    private OnTmdbTvEventListener mListener;

    public TmdbTv() {

    }


    public void Parse(int tmdbNumber, final String locale) {

        try {
            TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);
            TvSeries result = tmdb.getTvSeries().getSeries(tmdbNumber, DEFAULT_LANG, TmdbTV.TvMethod.credits, TmdbTV.TvMethod.external_ids, TmdbTV.TvMethod.images, TmdbTV.TvMethod.videos);

            Series s = new Series();
            s.setSeriesName(result.getOriginalName());
            s.setFirstAired(result.getFirstAirDate());
            s.setTmdbId(result.getId());
            s.setImdbId(result.getExternalIds().getImdbId());
            s.setTvdbId(Utils.parseInt(result.getExternalIds().getTvdbId()));
            s.setOverview(result.getOverview());
            s.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + result.getPosterPath());


            StringBuilder sb = new StringBuilder();
            for (Network obj : result.getNetworks()) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(", ");
                }
                sb.append(obj.getName());

            }
            s.setNetwork(sb.toString());

            ArrayList<Backdrop> mBackdrops = new ArrayList<>();
            for (Artwork artwork : result.getImages().getBackdrops()) {
                mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbCastPosterSize + artwork.getFilePath(), s.getImdbId(), s.getID(), Constants.CollectionType.Series));
            }
            s.setBackdrops(mBackdrops);

//
//            for (TvSeason season : result.getSeasons()) {
//                TvSeason ss = tmdb.getTvSeasons().getSeason(result.getId(), season.getSeasonNumber(), DEFAULT_LANG);
//                Log.i("aa", "ee");
//            }

            if (mListener != null)
                mListener.onCompleted(s);
        } catch (Exception e) {
            NLog.e(TAG, e);

            if (mListener != null)
                mListener.onNotCompleted(null, "");
        }

    }

    public void ParseImdbNumber(String imdbNumber, final String locale) {
        try {
            TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

            Movie m = new Movie();
            m.setContentProvider(ContentProviders.TMDb.value);
            // MovieDb result =
            // tmdb.getMovies().getMovie(Integer.parseInt(url), "en");
            FindResults result = tmdb.getFind().find(imdbNumber, ExternalSource.imdb_id, null);
            if (result.getMovieResults().size() == 0) {
                if (mListener != null)
                    mListener.onNotCompleted(null, "");
            }
            int tmdbNumber = result.getMovieResults().get(0).getId();
            MovieDb mDB = tmdb.getMovies().getMovie(tmdbNumber, DEFAULT_LANG);
            // MovieDb mDB = result.getMovieResults().get(0);

            m.setTmdbNumber(String.valueOf(tmdbNumber));
            m.setImdbNumber(mDB.getImdbID());
            m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
            m.setVotes(String.valueOf(mDB.getVoteCount()));
            m.setUserRating(String.valueOf(mDB.getVoteAverage()));
            m.setTmdbVotes(String.valueOf(mDB.getVoteCount()));
            m.setTmdbUserRating(String.valueOf(mDB.getVoteAverage()));
            m.setRunningTime(String.valueOf(mDB.getRuntime()));
            // m.setPoster("https://image.tmdb.org/t/p/w500" +
            // mDB.getPosterPath());
            m.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + mDB.getPosterPath());

            // ****************
            DecimalFormat formatter = new DecimalFormat("#,###,###");
            String budget = formatter.format(mDB.getBudget());
            m.setBudget("$" + budget);
            // ****************
            StringBuilder sb = new StringBuilder();
            for (Genre obj : mDB.getGenres()) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(", ");
                }
                sb.append(obj.getName());
            }
            m.setGenre(sb.toString());
            // ****************
            sb = new StringBuilder();
            for (ProductionCountry obj : mDB.getProductionCountries()) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(", ");
                }
                sb.append(obj.getName());
            }
            m.setCountry(sb.toString());
            // ****************
            sb = new StringBuilder();
            for (ProductionCompany obj : mDB.getProductionCompanies()) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(", ");
                }
                sb.append(obj.getName());
            }
            m.setProductionCompany(sb.toString());
            // ****************
            sb = new StringBuilder();
            for (Language obj : mDB.getSpokenLanguages()) {
                if (!TextUtils.isEmpty(sb)) {
                    sb.append(", ");
                }
                sb.append(obj.getName());
            }
            m.setLanguage(sb.toString());

            Credits credits = tmdb.getMovies().getCredits(tmdbNumber);

            // ****************
            sb = new StringBuilder();
            for (PersonCrew obj : credits.getCrew()) {
                if (obj.getDepartment().equals("Directing")) {
                    if (!TextUtils.isEmpty(sb)) {
                        sb.append(", ");
                    }
                    sb.append(obj.getName());
                }
            }
            m.setDirector(sb.toString());
            // ****************
            sb = new StringBuilder();
            for (PersonCrew obj : credits.getCrew()) {
                if (obj.getDepartment().equals("Writing")) {
                    if (!TextUtils.isEmpty(sb)) {
                        sb.append(", ");
                    }
                    sb.append(obj.getName());
                }
            }
            m.setWriter(sb.toString());

            // *********************************************
            ArrayList<Cast> castList = new ArrayList<>();
            List<PersonCast> casts = credits.getCast();
            for (PersonCast personCast : casts) {
                Cast c = new Cast();
                c.setCastID(String.valueOf(personCast.getCastId()));
                c.setCharacter(personCast.getCharacter());
                c.setName(personCast.getName());
                if (!TextUtils.isEmpty(personCast.getProfilePath()))
                    c.setImageUrl("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbCastPosterSize + personCast.getProfilePath());
                c.setUrl("https://www.themoviedb.org/person/" + personCast.getId());

                castList.add(c);
            }

            m.setCast(castList);
            // *********************************************
            ArrayList<Backdrop> mBackdrops = new ArrayList<>();
            MovieImages images = tmdb.getMovies().getImages(tmdbNumber, null);

            for (Artwork artwork : images.getBackdrops()) {
                mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), mDB.getImdbID(), mDB.getId(), Constants.CollectionType.Series));
            }
            m.setBackdrops(mBackdrops);
            // ****************
            List<Video> videos = tmdb.getMovies().getVideos(tmdbNumber, "");
            ArrayList<Trailer> trailers = new ArrayList<>();
            for (Video v : videos) {
                trailers.add(new Trailer(0, v.getKey(), v.getSite(), Constants.CollectionType.Series));
            }
            m.setTrailers(trailers);

            // *********************************************
            if (locale.equals(DEFAULT_LANG)) {
                m.setOriginalName(mDB.getTitle());
                m.setEnglishPlot(mDB.getOverview());
            } else {
                mDB = tmdb.getMovies().getMovie(tmdbNumber, locale);

                m.setOriginalName(mDB.getOriginalTitle());
                m.setOtherName(mDB.getTitle());
                m.setOtherPlot(mDB.getOverview());

                sb = new StringBuilder();
                for (Genre obj : mDB.getGenres()) {
                    if (!TextUtils.isEmpty(sb)) {
                        sb.append(", ");
                    }
                    sb.append(obj.getName());
                }
                m.setGenre(sb.toString());
            }

            // mMovie.setWriter(p.getWriter());
            // mMovie.setUpdateDate(DateTimeUtils.DateTimeNowString());
            if (mListener != null)
                mListener.onCompleted(m);
        } catch (Exception e) {
            if (mListener != null)
                mListener.onNotCompleted(null, "");
        }

    }

    public void ParseRatingWithTmdbNumberAsync(final String tmdbNumber) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                Movie m = new Movie();
                m.setContentProvider(ContentProviders.TMDb.value);
                TvSeries mDB = tmdb.getTvSeries().getSeries(Utils.parseInt(tmdbNumber), DEFAULT_LANG, null);
                if (mDB != null) {
                    m.setTmdbNumber(tmdbNumber);
                    m.setVotes(String.valueOf(mDB.getVoteCount()));
                    m.setUserRating(String.valueOf(mDB.getVoteAverage()));
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(m);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void ParseRatingWithImdbNumberAsync(final String imdbNumber) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                Movie m = new Movie();
                m.setContentProvider(ContentProviders.TMDb.value);
                // MovieDb result =
                // tmdb.getMovies().getMovie(Integer.parseInt(url), DEFAULT_LANG);
                FindResults result = tmdb.getFind().find(imdbNumber, ExternalSource.imdb_id, null);
                if (result.getMovieResults().size() == 0) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onNotCompleted(null, "");
                    });
                }
                int tmdbNumber = result.getMovieResults().get(0).getId();
                TvSeries mDB = tmdb.getTvSeries().getSeries(tmdbNumber, DEFAULT_LANG, null);
                m.setTmdbNumber(String.valueOf(tmdbNumber));
                m.setVotes(String.valueOf(mDB.getVoteCount()));
                m.setUserRating(String.valueOf(mDB.getVoteAverage()));

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(m);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }


    public void Search(final String SeriesName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<SearchResult> sr = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                TmdbSearch search = tmdb.getSearch();

                TvResultsPage movieResults = search.searchTv(SeriesName, DEFAULT_LANG, null);
                for (TvSeries result : movieResults.getResults()) {
                    sr.add(new SearchResult(String.valueOf(result.getId()), result.getOriginalName(), "https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + result.getPosterPath(), result.getFirstAirDate()));
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(sr);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }


    public void setTmdbTvEventListener(OnTmdbTvEventListener onTmdbEventListener) {
        mListener = onTmdbEventListener;

    }
}
