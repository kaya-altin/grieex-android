package com.grieex.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.grieex.core.listener.OnTmdbEventListener;
import com.grieex.helper.Constants;
import com.grieex.helper.Constants.ContentProviders;
import com.grieex.helper.DateUtils;
import com.grieex.helper.GrieeXSettings;
import com.grieex.helper.NLog;
import com.grieex.helper.Utils;
import com.grieex.model.tables.Backdrop;
import com.grieex.model.tables.Cast;
import com.grieex.model.tables.Movie;
import com.grieex.model.tables.Trailer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbFind.ExternalSource;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
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
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;

public class Tmdb {
    private static final String TAG = Tmdb.class.getName();
    private static final String DEFAULT_LANG = "en";

    private OnTmdbEventListener mListener;


    public Tmdb() {

    }

    public void Parse(int tmdbNumber, final String locale) {

        try {
            TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);
            MovieDb mDB = tmdb.getMovies().getMovie(tmdbNumber, DEFAULT_LANG, TmdbMovies.MovieMethod.videos, TmdbMovies.MovieMethod.credits);

            Movie m = new Movie();

            m.setTmdbNumber(String.valueOf(tmdbNumber));
            m.setImdbNumber(mDB.getImdbID());
            m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
            m.setVotes(String.valueOf(mDB.getVoteCount()));
            m.setUserRating(String.valueOf(mDB.getVoteAverage()));
            m.setTmdbVotes(String.valueOf(mDB.getVoteCount()));
            m.setTmdbUserRating(String.valueOf(mDB.getVoteAverage()));
            m.setRunningTime(String.valueOf(mDB.getRuntime()));
            m.setReleaseDate(mDB.getReleaseDate());
            if (!TextUtils.isEmpty(mDB.getPosterPath()))
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

            // ****************
            sb = new StringBuilder();
            for (PersonCrew obj : mDB.getCredits().getCrew()) {
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
            for (PersonCrew obj : mDB.getCredits().getCrew()) {
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
            for (PersonCast personCast : mDB.getCredits().getCast()) {
                Cast c = new Cast();
                c.setCastID(String.valueOf(personCast.getId()));
                c.setCharacter(personCast.getCharacter());
                c.setName(personCast.getName());
                if (!TextUtils.isEmpty(personCast.getProfilePath()))
                    c.setImageUrl("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbCastPosterSize + personCast.getProfilePath());
                c.setUrl("https://www.themoviedb.org/person/" + personCast.getId());

                castList.add(c);
            }

            m.setCast(castList);
            // *********************************************
//            ArrayList<Backdrop> mBackdrops = new ArrayList<Backdrop>();
//            for (Artwork artwork : mDB.getImages()) {
//                if (artwork.getArtworkType() == ArtworkType.BACKDROP)
//                    mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/w500" + artwork.getFilePath(), mDB.getImdbID(), mDB.getId()));
//            }
//            m.setBackdrops(mBackdrops);
            ArrayList<Backdrop> mBackdrops = new ArrayList<>();
            MovieImages images = tmdb.getMovies().getImages(tmdbNumber, null);

            for (Artwork artwork : images.getBackdrops()) {
                mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), mDB.getImdbID(), mDB.getId(), Constants.CollectionType.Movie));
            }
            m.setBackdrops(mBackdrops);
            // ****************
            ArrayList<Trailer> trailers = new ArrayList<>();
            for (Video v : mDB.getVideos()) {
                trailers.add(new Trailer(0, v.getKey(), v.getSite(), Constants.CollectionType.Movie));
            }
            m.setTrailers(trailers);

            // *********************************************
            m.setOriginalName(mDB.getTitle());
            if (locale.equals(DEFAULT_LANG)) {
                m.setOriginalName(mDB.getTitle());
                m.setEnglishPlot(mDB.getOverview());
            } else {
                mDB = tmdb.getMovies().getMovie(tmdbNumber, locale);

                //m.setOriginalName(mDB.getOriginalTitle());
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


            if (mListener != null)
                mListener.onCompleted(m);
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
            // tmdb.getMovies().getMovie(Integer.parseInt(url), DEFAULT_LANG);
            FindResults result = tmdb.getFind().find(imdbNumber, ExternalSource.imdb_id, null);
            if (result.getMovieResults().size() == 0) {
                if (mListener != null)
                    mListener.onNotCompleted(null, "");
            }
            int tmdbNumber = result.getMovieResults().get(0).getId();
            MovieDb mDB = tmdb.getMovies().getMovie(tmdbNumber, DEFAULT_LANG, TmdbMovies.MovieMethod.videos, TmdbMovies.MovieMethod.credits);
            // MovieDb mDB = result.getMovieResults().get(0);

            m.setTmdbNumber(String.valueOf(tmdbNumber));
            m.setImdbNumber(mDB.getImdbID());
            m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
            m.setVotes(String.valueOf(mDB.getVoteCount()));
            m.setUserRating(String.valueOf(mDB.getVoteAverage()));
            m.setTmdbVotes(String.valueOf(mDB.getVoteCount()));
            m.setTmdbUserRating(String.valueOf(mDB.getVoteAverage()));
            m.setRunningTime(String.valueOf(mDB.getRuntime()));
            m.setReleaseDate(mDB.getReleaseDate());

            if (!TextUtils.isEmpty(mDB.getPosterPath()))
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

            // ****************
            sb = new StringBuilder();
            for (PersonCrew obj : mDB.getCredits().getCrew()) {
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
            for (PersonCrew obj : mDB.getCredits().getCrew()) {
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
            for (PersonCast personCast : mDB.getCredits().getCast()) {
                Cast c = new Cast();
                c.setCastID(String.valueOf(personCast.getId()));
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
                mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), mDB.getImdbID(), mDB.getId(), Constants.CollectionType.Movie));
            }
            m.setBackdrops(mBackdrops);
            // ****************
            ArrayList<Trailer> trailers = new ArrayList<>();
            for (Video v : mDB.getVideos()) {
                trailers.add(new Trailer(0, v.getKey(), v.getSite(), Constants.CollectionType.Movie));
            }
            m.setTrailers(trailers);

            // *********************************************
            m.setOriginalName(mDB.getTitle());
            if (locale.equals(DEFAULT_LANG)) {
                m.setOriginalName(mDB.getTitle());
                m.setEnglishPlot(mDB.getOverview());
            } else {
                mDB = tmdb.getMovies().getMovie(tmdbNumber, locale);

                // m.setOriginalName(mDB.getOriginalTitle());
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

    public void ParseTmdbNumberAsync(final int tmdbNumber, final String locale) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                Movie m = new Movie();
                m.setContentProvider(ContentProviders.TMDb.value);
                // MovieDb result =
                // tmdb.getMovies().getMovie(Integer.parseInt(url), DEFAULT_LANG);
                MovieDb mDB = tmdb.getMovies().getMovie(tmdbNumber, DEFAULT_LANG, TmdbMovies.MovieMethod.videos, TmdbMovies.MovieMethod.credits);
                if (mDB == null) {
                    handler.post(() -> {
                        if (mListener != null)
                            mListener.onNotCompleted(null, "");
                    });

                    return;
                }
                //  MovieDb mDB = tmdb.getMovies().getMovie(result.getMovieResults().get(0).getId(), DEFAULT_LANG);
                // MovieDb mDB = result.getMovieResults().get(0);

                m.setTmdbNumber(String.valueOf(tmdbNumber));
                m.setImdbNumber(mDB.getImdbID());
                m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
                m.setVotes(String.valueOf(mDB.getVoteCount()));
                m.setUserRating(String.valueOf(mDB.getVoteAverage()));
                m.setTmdbVotes(String.valueOf(mDB.getVoteCount()));
                m.setTmdbUserRating(String.valueOf(mDB.getVoteAverage()));
                m.setRunningTime(String.valueOf(mDB.getRuntime()));
                m.setReleaseDate(mDB.getReleaseDate());
                if (!TextUtils.isEmpty(mDB.getPosterPath()))
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

                Credits credits = mDB.getCredits();

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
                for (PersonCast personCast : credits.getCast()) {
                    Cast c = new Cast();
                    c.setCastID(String.valueOf(personCast.getId()));
                    c.setCharacter(personCast.getCharacter());
                    c.setName(personCast.getName());
                    if (!TextUtils.isEmpty(personCast.getProfilePath()))
                        c.setImageUrl("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbCastPosterSize + personCast.getProfilePath());
                    c.setUrl("https://www.themoviedb.org/person/" + personCast.getId());

                    castList.add(c);
                }

                m.setCast(castList);
                // mMovie.setWriter(p.getWriter());
                // mMovie.setUpdateDate(DateTimeUtils.DateTimeNowString());

                // *********************************************
                ArrayList<Backdrop> mBackdrops = new ArrayList<>();
                MovieImages images = tmdb.getMovies().getImages(mDB.getId(), null);

                for (Artwork artwork : images.getBackdrops()) {
                    mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), mDB.getImdbID(), mDB.getId(), Constants.CollectionType.Movie));
                }
                m.setBackdrops(mBackdrops);
                // *********************************************
                ArrayList<Trailer> trailers = new ArrayList<>();
                for (Video v : mDB.getVideos()) {
                    trailers.add(new Trailer(mDB.getId(), v.getKey(), v.getSite(), Constants.CollectionType.Movie));
                }
                m.setTrailers(trailers);

                // *********************************************
                m.setOriginalName(mDB.getTitle());
                if (locale.equals(DEFAULT_LANG)) {
                    m.setOriginalName(mDB.getTitle());
                    m.setEnglishPlot(mDB.getOverview());
                } else {
                    mDB = tmdb.getMovies().getMovie(tmdbNumber, locale);

                    // m.setOriginalName(mDB.getOriginalTitle());
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

    public void ParseImdbNumberAsync(final String imdbNumber, final String locale) {
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

                    return;
                }
                int tmdbNumber = result.getMovieResults().get(0).getId();
                MovieDb mDB = tmdb.getMovies().getMovie(tmdbNumber, DEFAULT_LANG, TmdbMovies.MovieMethod.videos, TmdbMovies.MovieMethod.credits);
                // MovieDb mDB = result.getMovieResults().get(0);

                m.setTmdbNumber(String.valueOf(tmdbNumber));
                m.setImdbNumber(mDB.getImdbID());
                m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
                m.setVotes(String.valueOf(mDB.getVoteCount()));
                m.setUserRating(String.valueOf(mDB.getVoteAverage()));
                m.setTmdbVotes(String.valueOf(mDB.getVoteCount()));
                m.setTmdbUserRating(String.valueOf(mDB.getVoteAverage()));
                m.setRunningTime(String.valueOf(mDB.getRuntime()));
                m.setReleaseDate(mDB.getReleaseDate());

                if (!TextUtils.isEmpty(mDB.getPosterPath()))
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

                Credits credits = mDB.getCredits();

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
                for (PersonCast personCast : credits.getCast()) {
                    Cast c = new Cast();
                    c.setCastID(String.valueOf(personCast.getId()));
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
                MovieImages images = tmdb.getMovies().getImages(mDB.getId(), null);

                for (Artwork artwork : images.getBackdrops()) {
                    mBackdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), mDB.getImdbID(), mDB.getId(), Constants.CollectionType.Movie));
                }
                m.setBackdrops(mBackdrops);
                // ****************
                ArrayList<Trailer> trailers = new ArrayList<>();
                for (Video v : mDB.getVideos()) {
                    trailers.add(new Trailer(0, v.getKey(), v.getSite(), Constants.CollectionType.Movie));
                }
                m.setTrailers(trailers);


                // *********************************************
                m.setOriginalName(mDB.getTitle());
                if (locale.equals(DEFAULT_LANG)) {
                    m.setOriginalName(mDB.getTitle());
                    m.setEnglishPlot(mDB.getOverview());
                } else {
                    mDB = tmdb.getMovies().getMovie(tmdbNumber, locale);

                    // m.setOriginalName(mDB.getOriginalTitle());
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

    public void ParseRatingWithTmdbNumberAsync(final String tmdbNumber) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                Movie m = new Movie();
                m.setContentProvider(ContentProviders.TMDb.value);
                MovieDb mDB = tmdb.getMovies().getMovie(Utils.parseInt(tmdbNumber), DEFAULT_LANG);
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

                    return;
                }
                int tmdbNumber = result.getMovieResults().get(0).getId();
                MovieDb mDB = tmdb.getMovies().getMovie(tmdbNumber, DEFAULT_LANG);
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

    public void Search(final String MovieName) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<SearchResult> sr = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                TmdbSearch search = tmdb.getSearch();

                MovieResultsPage movieResults = search.searchMovie(MovieName, null, null, false, null);
                for (MovieDb result : movieResults.getResults()) {
                    SearchResult item = new SearchResult();
                    item.setKey(String.valueOf(result.getId()));
                    item.setTitle(result.getTitle());
                    if (!TextUtils.isEmpty(result.getPosterPath()))
                        item.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + result.getPosterPath());
                    item.setYear(result.getReleaseDate());

                    sr.add(item);
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

    public void getPopularListSearchResult(final String locale, final int page) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<SearchResult> sr = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);
                List<MovieDb> movieResults = tmdb.getMovies().getPopularMovies(locale, page).getResults();

                for (MovieDb result : movieResults) {
                    SearchResult item = new SearchResult();
                    item.setKey(String.valueOf(result.getId()));
                    item.setTitle(result.getTitle());
                    if (!TextUtils.isEmpty(result.getPosterPath()))
                        item.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + result.getPosterPath());
                    item.setYear(result.getReleaseDate());
                    sr.add(item);
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

    public void getPopularList(final String locale, final int page) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Movie> returnList = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);
                List<MovieDb> result = tmdb.getMovies().getPopularMovies(locale, page).getResults();

                for (MovieDb mDB : result) {
                    Movie m = new Movie();
                    //m.setID(mDB.getId());
                    m.setTmdbNumber(String.valueOf(mDB.getId()));
                    m.setOriginalName(mDB.getOriginalTitle());
                    m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
                    m.setRunningTime(String.valueOf(mDB.getRuntime()));
                    if (!TextUtils.isEmpty(mDB.getPosterPath()))
                        m.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + mDB.getPosterPath());
                    m.setEnglishPlot(mDB.getOverview());

                    returnList.add(m);
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(returnList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void getNowPlayingList(final String locale, final int page) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                ArrayList<Movie> returnList = new ArrayList<>();
                List<MovieDb> result = tmdb.getMovies().getNowPlayingMovies(locale, page, "").getResults();

                for (MovieDb mDB : result) {
                    Movie m = new Movie();
                    //m.setID(mDB.getId());
                    m.setTmdbNumber(String.valueOf(mDB.getId()));
                    m.setOriginalName(mDB.getOriginalTitle());
                    m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
                    m.setRunningTime(String.valueOf(mDB.getRuntime()));
                    if (!TextUtils.isEmpty(mDB.getPosterPath()))
                        m.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + mDB.getPosterPath());
                    m.setEnglishPlot(mDB.getOverview());

                    returnList.add(m);
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(returnList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void getUpcomingList(final String locale, final int page) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                ArrayList<Movie> returnList = new ArrayList<>();
                List<MovieDb> result = tmdb.getMovies().getUpcoming(locale, page, "").getResults();

                for (MovieDb mDB : result) {
                    Movie m = new Movie();
                    //m.setID(mDB.getId());
                    m.setTmdbNumber(String.valueOf(mDB.getId()));
                    m.setOriginalName(mDB.getOriginalTitle());
                    m.setYear(String.valueOf(DateUtils.getYear(mDB.getReleaseDate())));
                    m.setRunningTime(String.valueOf(mDB.getRuntime()));
                    if (!TextUtils.isEmpty(mDB.getPosterPath()))
                        m.setPoster("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbPosterSize + mDB.getPosterPath());
                    m.setEnglishPlot(mDB.getOverview());

                    returnList.add(m);
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(returnList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void getBackDrops(final String imdb) {
        if (TextUtils.isEmpty(imdb))
            return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Backdrop> backdrops = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                String imdbnumber = imdb;
                if (!imdbnumber.contains("tt")) {
                    imdbnumber = "tt" + imdbnumber;
                }

                FindResults result = tmdb.getFind().find(imdbnumber, ExternalSource.imdb_id, null);


                if (result.getMovieResults().size() > 0) {
                    MovieImages images = tmdb.getMovies().getImages(result.getMovieResults().get(0).getId(), null);

                    for (Artwork artwork : images.getBackdrops()) {
                        backdrops.add(new Backdrop("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbBackdropPosterSize + artwork.getFilePath(), imdbnumber, 0, Constants.CollectionType.Movie));
                    }
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(backdrops);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void getTrailers(final String imdb) {
        if (TextUtils.isEmpty(imdb))
            return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Trailer> trailers = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                String imdbnumber = imdb;
                if (!imdbnumber.contains("tt")) {
                    imdbnumber = "tt" + imdbnumber;
                }

                FindResults result = tmdb.getFind().find(imdbnumber, ExternalSource.imdb_id, null);

                if (result.getMovieResults().size() > 0) {
                    List<Video> videos = tmdb.getMovies().getVideos(result.getMovieResults().get(0).getId(), "");

                    for (Video v : videos) {
                        trailers.add(new Trailer(0, v.getKey(), v.getSite(), Constants.CollectionType.Movie));
                    }
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(trailers);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void getCasts(final String imdb) {
        if (TextUtils.isEmpty(imdb))
            return;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                ArrayList<Cast> castList = new ArrayList<>();
                TmdbApi tmdb = new TmdbApi(GrieeXSettings.TmdbApiKey);

                String imdbnumber = imdb;
                if (!imdbnumber.contains("tt")) {
                    imdbnumber = "tt" + imdbnumber;
                }

                FindResults result = tmdb.getFind().find(imdbnumber, ExternalSource.imdb_id, null);

                if (result.getMovieResults().size() > 0) {
                    Credits credits = tmdb.getMovies().getCredits(result.getMovieResults().get(0).getId());

                    for (PersonCast personCast : credits.getCast()) {
                        Cast c = new Cast();
                        c.setCastID(String.valueOf(personCast.getId()));
                        c.setCharacter(personCast.getCharacter());
                        c.setName(personCast.getName());
                        if (!TextUtils.isEmpty(personCast.getProfilePath()))
                            c.setImageUrl("https://image.tmdb.org/t/p/" + GrieeXSettings.TmdbCastPosterSize + personCast.getProfilePath());
                        c.setUrl("https://www.themoviedb.org/person/" + personCast.getId());

                        castList.add(c);
                    }
                }

                handler.post(() -> {
                    if (mListener != null)
                        mListener.onCompleted(castList);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    if (mListener != null)
                        mListener.onNotCompleted(null, "");
                });
            }
        });
    }

    public void setTmdbEventListener(OnTmdbEventListener onTmdbEventListener) {
        mListener = onTmdbEventListener;

    }

    public enum ProfileImageSize {

        W45("w45"),
        W185("w185"),
        H632("h632"),
        ORIGINAL("original");

        private final String value;

        ProfileImageSize(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
