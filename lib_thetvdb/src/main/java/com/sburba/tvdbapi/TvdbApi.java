package com.sburba.tvdbapi;

import android.util.Log;

import com.sburba.tvdbapi.model.Actor;
import com.sburba.tvdbapi.model.Banner;
import com.sburba.tvdbapi.model.Episode;
import com.sburba.tvdbapi.model.Season;
import com.sburba.tvdbapi.model.Series;
import com.sburba.tvdbapi.parser.ActorListParser;
import com.sburba.tvdbapi.parser.BannerListParser;
import com.sburba.tvdbapi.parser.EpisodeParser;
import com.sburba.tvdbapi.parser.SeasonListParser;
import com.sburba.tvdbapi.parser.SeriesParser;
import com.sburba.tvdbapi.tools.WebBrowser;
import com.sburba.tvdbapi.xml.XmlException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("unused")
public class TvdbApi {

    private static final String TAG = "TvdbApi";
    private static final String CHAR_ENCODING = "UTF-8";
    private static final String BASE_URL = "https://thetvdb.com/api/";
    private static final String SERIES_SEARCH = BASE_URL + "GetSeries.php?seriesname=";
    private static final String IMDB_SERIES_SEARCH = BASE_URL + "GetSeriesByRemoteID.php?imdbid=";

    private final String mApiKey;
    private final String mLanguage;

    /**
     * Create a new TvdbApi instance. This does not need to be a singleton object
     *
     * @param apiKey   Your TVDB api key
     * @param language The two letter language code to use for queries, if null defaults to "en"
     */
    public TvdbApi(String apiKey, String language) {
        mApiKey = apiKey;
        mLanguage = language;
    }

    public Collection<Series> searchSeries(String seriesName) {
        String query;
        try {
            query = URLEncoder.encode(seriesName, CHAR_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.wtf(TAG, "How the hell is " + CHAR_ENCODING + " not supported? Dropping request");
            return null;
        }

        String requestUrl = SERIES_SEARCH + query + "&language=" + mLanguage;

        WebBrowser web = new WebBrowser();
        String bodyHtml = web.request(requestUrl);

        SeriesParser sp = new SeriesParser();

        try {
            return sp.parseListFromXmlString(bodyHtml);
        } catch (XmlException e) {

        }
        return null;
    }

    public Collection<Series> getSeriesFromImdbId(String imdbId) {
        String requestUrl = IMDB_SERIES_SEARCH + imdbId + "&language=" + mLanguage;

        WebBrowser web = new WebBrowser();
        String bodyHtml = web.request(requestUrl);

        SeriesParser sp = new SeriesParser();

        try {
            return sp.parseListFromXmlString(bodyHtml);
        } catch (XmlException e) {

        }
        return null;
    }


    public void getSeasons(Series series) {
        getSeasons(series.id);
    }

    public Collection<Season> getSeasons(int seriesId) {
        Collection<Season> seasons = new ArrayList<>();
        String requestUrl = getSeriesRequestUrl(seriesId);
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());
            SeasonListParser sp = new SeasonListParser(mLanguage);
            Map<String, String> xmlStrings = unpackZip(in);
            in.close();
            seasons = sp.parseListFromXmlStrings(xmlStrings);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return seasons;
    }


    public void getEpisodes(Series series) {
        getEpisodes(series.id);
    }


    public Collection<Episode> getEpisodes(int seriesId) {
        return getEpisodes(seriesId, EpisodeParser.ALL_SEASONS);
    }


    public Collection<Episode> getEpisodes(Season season) {
        return getEpisodes(season.seriesId, season.seasonNumber);
    }


    public Collection<Episode> getEpisodes(int seriesId, int seasonNumber) {
        Collection<Episode> seasons = new ArrayList<>();
        String requestUrl = getSeriesRequestUrl(seriesId);
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());

            EpisodeParser sp = new EpisodeParser(mLanguage);
            Map<String, String> xmlStrings = unpackZip(in);

            in.close();
            seasons = sp.parseListFromXmlStrings(xmlStrings);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return seasons;
    }


    public void getEpisode(Series series, int seasonNumber, int episodeNumber) {
        getEpisode(series, seasonNumber, episodeNumber, SHOW_ORDER.DEFAULT);
    }

    public void getEpisode(Series series, int seasonNumber, int episodeNumber, SHOW_ORDER showOrder) {
        getEpisode(series.id, seasonNumber, episodeNumber, showOrder);
    }

    public Episode getEpisode(int seriesId, int seasonNumber, int episodeNumber, SHOW_ORDER showOrder) {
        String showOrderModifier;
        if (showOrder == SHOW_ORDER.ABSOLUTE) {
            showOrderModifier = "/absolute/";
        } else if (showOrder == SHOW_ORDER.DVD) {
            showOrderModifier = "/dvd/";
        } else {
            showOrderModifier = "/default/";
        }
        String requestUrl =
                BASE_URL + mApiKey + "/series/" + seriesId + showOrderModifier + seasonNumber +
                        "/" + episodeNumber + "/" + mLanguage + ".xml";


        WebBrowser web = new WebBrowser();
        String bodyHtml = web.request(requestUrl);
        EpisodeParser sp = new EpisodeParser(mLanguage);

        try {
            return sp.parseXmlString(bodyHtml);
        } catch (XmlException e) {

        }
        return null;
    }


    public Episode getEpisode(Season season, int episodeNumber, SHOW_ORDER showOrder) {
        String showOrderModifier;
        if (showOrder == SHOW_ORDER.ABSOLUTE) {
            showOrderModifier = "/absolute/";
        } else if (showOrder == SHOW_ORDER.DVD) {
            showOrderModifier = "/dvd/";
        } else {
            showOrderModifier = "/default/";
        }
        String requestUrl =
                BASE_URL + mApiKey + "/series/" + season.seriesId + showOrderModifier + season.seasonNumber +
                        "/" + episodeNumber + "/" + mLanguage + ".xml";

        WebBrowser web = new WebBrowser();
        String bodyHtml = web.request(requestUrl);
        EpisodeParser sp = new EpisodeParser(mLanguage);

        try {
            return sp.parseXmlString(bodyHtml);
        } catch (XmlException e) {

        }
        return null;
    }


    public void getBanners(Series series) {
        getBanners(series.id);
    }

    public void getBanners(int seriesId) {
        getBanners(seriesId, BannerListParser.ALL_SEASONS);
    }


    public Collection<Banner> getBanners(int seriesId, int seasonNumber) {
        String requstUrl = getSeriesRequestUrl(seriesId);

        Collection<Banner> banners = new ArrayList<>();
        String requestUrl = getSeriesRequestUrl(seriesId);
        InputStream in = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());

            BannerListParser sp = new BannerListParser();
            Map<String, String> xmlStrings = unpackZip(in);

            in.close();
            banners = sp.parseListFromXmlStrings(xmlStrings);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return banners;
    }


    public void getActors(Series series) {
        getActors(series.id);
    }


    public Collection<Actor> getActors(int seriesId) {
        String requestUrl = BASE_URL + mApiKey + "/series/" + seriesId + "/actors.xml";
        WebBrowser web = new WebBrowser();
        String bodyHtml = web.request(requestUrl);
        ActorListParser sp = new ActorListParser();

        try {
            return sp.parseListFromXmlString(bodyHtml);
        } catch (XmlException e) {

        }
        return null;
    }

    //**********************************

    private Map<String, String> unpackZip(InputStream dataStream) throws IOException {
        ZipInputStream zipStream = new ZipInputStream(dataStream);
        byte[] buffer = new byte[1024];
        ZipEntry ze;
        int count;
        Map<String, String> xmlStrings = new HashMap<>();
        while ((ze = zipStream.getNextEntry()) != null) {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            while ((count = zipStream.read(buffer)) != -1) {
                byteStream.write(buffer, 0, count);
            }
            xmlStrings.put(ze.getName(), byteStream.toString("utf-8"));
        }

        return xmlStrings;
    }

    private String getSeriesRequestUrl(int seriesId) {
        return BASE_URL + mApiKey + "/series/" + seriesId + "/all/" + mLanguage + ".zip";
    }

    public enum SHOW_ORDER {DEFAULT, DVD, ABSOLUTE}
}
