package com.grieex.core;

import android.content.Context;
import android.os.AsyncTask;

import com.grieex.helper.Constants;
import com.grieex.helper.DatabaseHelper;
import com.grieex.helper.NLog;
import com.grieex.core.listener.OnImdb250EventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.movito.themoviedbapi.tools.WebBrowser;

public class Imdb250 {
    private static final String TAG = Imdb250.class.getName();
    private final DatabaseHelper dbHelper;
    private OnImdb250EventListener mListener;

    public void setImdb250EventListener(OnImdb250EventListener eventListener) {
        mListener = eventListener;
    }


    public Imdb250(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public void getMovies() {
        new AsyncTask<Void, Void, Boolean>() {
            protected void onPostExecute(Boolean result) {
                if (result) {
                    if (mListener != null)
                        mListener.onCompleted();
                } else {
                    if (mListener != null)
                        mListener.onNotCompleted();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    WebBrowser web = new WebBrowser();
                    String bodyHtml = web.request("https://www.imdb.com/chart/top?ref_=nv_ch_250_4");

                    TextParseMovie(bodyHtml);
                    return true;
                } catch (Exception e) {
                    return false;
                }

            }

        }.execute();
    }

    public void getTvShows() {
        new AsyncTask<Void, Void, Boolean>() {
            protected void onPostExecute(Boolean result) {
                if (result) {
                    if (mListener != null)
                        mListener.onCompleted();
                } else {
                    if (mListener != null)
                        mListener.onNotCompleted();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    WebBrowser web = new WebBrowser();
                    String bodyHtml = web.request("https://www.imdb.com/chart/toptv/?ref_=nv_tvv_250_4");

                    TextParseSeries(bodyHtml);
                    return true;
                } catch (Exception e) {
                    return false;
                }

            }

        }.execute();
    }

    private void TextParseMovie(String response) {
        try {
            dbHelper.ExecuteQuery("Delete From Imdb250 Where Type=" + Constants.Imdb250Type.Movie.value);

            Pattern pattern = Pattern.compile("<td class=\"posterColumn\">");
            Matcher matcher = pattern.matcher(response);

            int iRank = 0;
            while (matcher.find()) {
                iRank++;

                int nStartPos = matcher.start();// response.indexOf("<td class=\"posterColumn\">",
                // matcher.start());
                int nEndPos;

                nStartPos = response.indexOf("<img src=\"", nStartPos);
                nEndPos = response.indexOf(".jpg", nStartPos);
                String ImageUrl = response.substring(nStartPos + 10, nEndPos + 4);
                ImageUrl = ImageUrl.substring(0, ImageUrl.lastIndexOf("._")) + ".UX250.jpg";

                nStartPos = response.indexOf("/title/tt", nStartPos);
                nEndPos = response.indexOf("/?", nStartPos);
                String ImdbNumber = response.substring(nStartPos + 7, nEndPos);

                nStartPos = response.indexOf(">", nStartPos);
                nEndPos = response.indexOf("</a>", nStartPos);
                String Title = response.substring(nStartPos + 1, nEndPos);

                nStartPos = response.indexOf("<td class=\"ratingColumn imdbRating\">", nStartPos);
                nEndPos = response.indexOf("</td>", nStartPos);
                String Rating = response.substring(nStartPos, nEndPos);
                Rating = CoreUtils.StripHtml(Rating);
                Rating = CoreUtils.StripBlanks(Rating);

                nStartPos = response.indexOf("<td class=\"ratingColumn imdbRating\">", nStartPos);
                nEndPos = response.indexOf("</td>", nStartPos);
                String Votes = response.substring(nStartPos, nEndPos);
                nStartPos = Votes.indexOf("based on ");
                nEndPos = Votes.indexOf("user ratings", nStartPos);
                Votes = Votes.substring(nStartPos + 9, nEndPos - 1);
                Votes = Votes.replace(".", "").replace(",", "");

                dbHelper.addMovieImdb250(new com.grieex.model.tables.Imdb250(iRank, Title, Rating, Integer.parseInt(Votes), ImageUrl, ImdbNumber, Constants.Imdb250Type.Movie.value));
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }

    }

    private void TextParseSeries(String response) {
        try {
            dbHelper.ExecuteQuery("Delete From Imdb250 Where Type=" + Constants.Imdb250Type.Series.value);

            Pattern pattern = Pattern.compile("<td class=\"posterColumn\">");
            Matcher matcher = pattern.matcher(response);

            int iRank = 0;
            while (matcher.find()) {
                iRank++;

                int nStartPos = matcher.start();// response.indexOf("<td class=\"posterColumn\">",
                // matcher.start());
                int nEndPos;

                nStartPos = response.indexOf("<img src=\"", nStartPos);
                nEndPos = response.indexOf(".jpg", nStartPos);
                String ImageUrl = response.substring(nStartPos + 10, nEndPos + 4);
                ImageUrl = ImageUrl.substring(0, ImageUrl.lastIndexOf("._")) + ".UX250.jpg";

                nStartPos = response.indexOf("/title/tt", nStartPos);
                nEndPos = response.indexOf("/?", nStartPos);
                String ImdbNumber = response.substring(nStartPos + 7, nEndPos);

                nStartPos = response.indexOf(">", nStartPos);
                nEndPos = response.indexOf("</a>", nStartPos);
                String Title = response.substring(nStartPos + 1, nEndPos);

                nStartPos = response.indexOf("<td class=\"ratingColumn imdbRating\">", nStartPos);
                nEndPos = response.indexOf("</td>", nStartPos);
                String Rating = response.substring(nStartPos, nEndPos);
                Rating = CoreUtils.StripHtml(Rating);
                Rating = CoreUtils.StripBlanks(Rating);

                nStartPos = response.indexOf("<td class=\"ratingColumn imdbRating\">", nStartPos);
                nEndPos = response.indexOf("</td>", nStartPos);
                String Votes = response.substring(nStartPos, nEndPos);
                nStartPos = Votes.indexOf("based on ");
                nEndPos = Votes.indexOf("user ratings", nStartPos);
                Votes = Votes.substring(nStartPos + 9, nEndPos - 1);
                Votes = Votes.replace(".", "").replace(",", "");

                dbHelper.addMovieImdb250(new com.grieex.model.tables.Imdb250(iRank, Title, Rating, Integer.parseInt(Votes), ImageUrl, ImdbNumber, Constants.Imdb250Type.Series.value));
            }

        } catch (Exception e) {
            NLog.e(TAG, e);
        }

    }
}
