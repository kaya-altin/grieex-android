package com.grieex.core;

import com.grieex.core.listener.OnImdbEventListener;
import com.grieex.helper.NLog;
import com.grieex.interfaces.ImdbService;
import com.grieex.model.tables.Movie;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Imdb {
    private static final String TAG = Imdb.class.getName();
    private OnImdbEventListener mListener;

    public void setImdbEventListener(OnImdbEventListener eventListener) {
        mListener = eventListener;
    }

    private int iStartPos = 0;

    public Imdb() {
        try {
            // mContext = context;
            // mNotificationHelper = new NotificationHelper(mContext);


        } catch (Exception e) {
            NLog.e(TAG, e);
        }
    }

    public void ParseRatingAsync(final String imdbId) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.imdb.com/")
                    .build();

            ImdbService service = retrofit.create(ImdbService.class);
            service.getRating(imdbId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response == null || response.body() == null)
                        return;

                    try {
                        Movie m = new Movie();
                        String strHtml = response.body().string();
                        String strStart;
                        String strEnd;
                        iStartPos = 0;
                        ParseResult pr;

                        // ********* UserRating ********* //
                        strStart = "<span class=\"ipl-rating-star__rating\">";
                        strEnd = "</span>";
                       pr = CoreUtils.GetText(strHtml, strStart, strEnd, iStartPos);

                        if (pr.text != null && !pr.text.isEmpty()) {
                            pr.text = pr.text.replace(".", ",");
                            m.setUserRating(pr.text);
                        }

                        // ********* Votes ********* //
                        strStart = "<div class=\"allText\">";
                        strEnd = "IMDb users have given";
                        pr = CoreUtils.GetText(strHtml, strStart, strEnd, 0);
                        if (pr.text != null && !pr.text.isEmpty()) {
                            pr.text = CoreUtils.RemoveSpecialCharacters(pr.text);
                            pr.text = pr.text.replace(",", "");
                            m.setVotes(pr.text);
                        }

                        if (mListener != null)
                            mListener.onCompleted(m);


                    } catch (Exception e) {
                        NLog.e(TAG, e);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (mListener != null)
                        mListener.onNotCompleted(t, "");
                }
            });

        } catch (Exception e) {
            if (mListener != null) {
                mListener.onNotCompleted(e, "");
            }
        }
    }

    public void ParseRating(final String imdbId) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.imdb.com/")
                    .build();

            ImdbService service = retrofit.create(ImdbService.class);
            Response<ResponseBody> response = service.getMovie(imdbId).execute();

            if (response == null || response.body() == null)
                return;


                Movie m = new Movie();
                String strHtml = response.body().string();
                String strStart;
                String strEnd;
                iStartPos = 0;
                ParseResult pr;

                // ********* UserRating ********* //
                strStart = "<span itemprop=\"ratingValue\">";
                strEnd = "</span>";
                pr = CoreUtils.GetText(strHtml, strStart, strEnd, 0);
                if (pr.text != null && !pr.text.isEmpty()) {
                    pr.text = pr.text.replace(".", ",");
                    m.setUserRating(pr.text);
                }

                // ********* Votes ********* //
                strStart = "itemprop=\"ratingCount\">";
                strEnd = "</span>";
                pr = CoreUtils.GetText(strHtml, strStart, strEnd, 0);
                if (pr.text != null && !pr.text.isEmpty()) {
                    pr.text = pr.text.replace(",", "");
                    m.setVotes(pr.text);
                }

                if (mListener != null)
                    mListener.onCompleted(m);

        } catch (Exception e) {
            if (mListener != null) {
                mListener.onNotCompleted(e, "");
            }
        }
    }

}
