package net.coscolla.highlight.net;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface Api {
  @Multipart
  @POST("/")
  Call<UploadImageResult> uploadImage(@Part MultipartBody.Part file);
}
