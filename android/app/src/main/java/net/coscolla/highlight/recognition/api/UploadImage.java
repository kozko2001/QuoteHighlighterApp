package net.coscolla.highlight.recognition.api;

import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import net.coscolla.highlight.recognition.Recognition;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.exceptions.Exceptions;

public class UploadImage implements Recognition {

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
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .client(client)
        .build();

    service = retrofit.create(Api.class);
  }


  @Override
  public Observable<String> recognition(String filePath) {
    File file = new File(filePath);
    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
    MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

    return service.uploadImage(body)
        .map(uploadImageResult ->  {
          if(uploadImageResult.result != null) {
            return uploadImageResult.result;
          } else {
            String error;
            if(uploadImageResult.error != null) {
              error = uploadImageResult.error;
            } else {
              error = "unrecoverable error";
            }

            Exceptions.propagate(new Exception("error :" + error));
            return "";
          }
        }).doOnError((e) -> {
          Log.e("Upload error:", e.getMessage());
        });
  }
}
