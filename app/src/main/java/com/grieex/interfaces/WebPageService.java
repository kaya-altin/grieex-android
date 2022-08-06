package com.grieex.interfaces;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface WebPageService {
    @GET("/")
    Call<ResponseBody> getSite();
}
