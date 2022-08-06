package com.grieex.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ImdbService {
    @GET("title/{id}/")
    Call<ResponseBody> getMovie(@Path("id") String imdbId);

    @GET("title/{id}/ratings")
    Call<ResponseBody> getRating(@Path("id") String imdbId);
}
