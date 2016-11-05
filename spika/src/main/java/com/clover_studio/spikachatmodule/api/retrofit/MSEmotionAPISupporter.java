package com.clover_studio.spikachatmodule.api.retrofit;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by sbh on 2016-10-26.
 */
public interface MSEmotionAPISupporter {
    @POST("emotion/v1.0/recognize")
    Call<ResponseBody> getFacialEmotion(@Header("Ocp-Apim-Subscription-Key") String key, @Header("Content-Type") String contentType, @Body RequestBody imageBody);
}
