package net.coscolla.highlight.recognition.api;


import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface Api {
  @Multipart
  @POST("/")
  Observable<UploadImageResult> uploadImage(@Part MultipartBody.Part file);
}
