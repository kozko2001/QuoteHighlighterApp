package net.coscolla.highlight.net;

import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UploadImage {

  private final Retrofit retrofit;
  private final Api service;

  public UploadImage() {
    OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(new StethoInterceptor())
        .build();

    retrofit = new Retrofit.Builder()
        .baseUrl("http://192.168.11.33:5000/")
        .client(client)
        .build();

    service = retrofit.create(Api.class);
  }

  public void uploadImage(String filePath) {

    File file = new File(filePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
    MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

    Call<ResponseBody> call = service.uploadImage(body);
    call.enqueue(new Callback<ResponseBody>() {
      @Override
      public void onResponse(Call<ResponseBody> call,
                             Response<ResponseBody> response) {
        Log.v("Upload", "success");
      }

      @Override
      public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.e("Upload error:", t.getMessage());
      }
    });

  }
}
