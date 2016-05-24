package net.coscolla.highlight.net.api;

import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadImage {

  private static final String LOGTAG = "UploadImage";
  private final Retrofit retrofit;
  private final Api service;

  public UploadImage() {
    OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new StethoInterceptor())
        .build();

    retrofit = new Retrofit.Builder()
        .baseUrl("http://192.168.11.33:5000/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build();

    service = retrofit.create(Api.class);
  }

  public void uploadImage(String filePath) {

    File file = new File(filePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
    MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

    Call<UploadImageResult> call = service.uploadImage(body);
    call.enqueue(new Callback<UploadImageResult>() {
      @Override
      public void onResponse(Call<UploadImageResult> call,
                             Response<UploadImageResult> response) {
        if(response.isSuccessful()) {
          if(response.body().result != null) {
            Log.e(LOGTAG, "result: " + response.body().result);
          }
          if(response.body().error != null) {
            Log.e(LOGTAG, "error: " + response.body().error);
          }
        }
      }

      @Override
      public void onFailure(Call<UploadImageResult> call, Throwable t) {

        Log.e("Upload error:", t.getMessage());
      }
    });

  }
}
