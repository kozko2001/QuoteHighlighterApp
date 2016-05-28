package net.coscolla.highlight.net.vision;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static rx.Observable.fromCallable;

/**
 * Copied from https://github.com/GoogleCloudPlatform/cloud-vision/blob/master/android/CloudVision/app/src/main/java/com/google/sample/cloudvision/MainActivity.java
 */
public class VisionApi {

  private static final java.lang.String CLOUD_VISION_API_KEY = "API_KEY";
  private static final String LOGTAG = "VisionApi";

  public Observable<String> googleVisionOCR(Uri url, ContentResolver contentResolver) {
    return fromCallable(() -> uploadImage(url, contentResolver));
  }

  public String uploadImage(Uri uri, ContentResolver contentResolver) throws Exception {
    if (uri != null) {
        // scale the image to save on bandwidth
        Bitmap bitmap =
            scaleBitmapDown(MediaStore.Images.Media.getBitmap(contentResolver, uri),
                1200);

        return callCloudVision(bitmap);
    } else {
      throw new Exception("Image picker gave us a null image.");
    }
  }


  private String callCloudVision(Bitmap bitmap) throws IOException {

    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
    builder.setVisionRequestInitializer(new
        VisionRequestInitializer(CLOUD_VISION_API_KEY));
    Vision vision = builder.build();

    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
        new BatchAnnotateImagesRequest();
    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
      AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

      // Add the image
      Image base64EncodedImage = new Image();
      // Convert the bitmap to a JPEG
      // Just in case it's a format that Android understands but Cloud VisionApi
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
      byte[] imageBytes = byteArrayOutputStream.toByteArray();

      // Base64 encode the JPEG
      base64EncodedImage.encodeContent(imageBytes);
      annotateImageRequest.setImage(base64EncodedImage);

      // add the features we want
      annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
        Feature labelDetection = new Feature();
        labelDetection.setType("TEXT_DETECTION");
        labelDetection.setMaxResults(10);
        add(labelDetection);
      }});

      // Add the list of one thing to the request
      add(annotateImageRequest);
    }});

    Vision.Images.Annotate annotateRequest =
        vision.images().annotate(batchAnnotateImagesRequest);
    // Due to a bug: requests to VisionApi API containing large images fail when GZipped.
    annotateRequest.setDisableGZipContent(true);
    Log.d(LOGTAG, "created Cloud VisionApi request object, sending request");

    BatchAnnotateImagesResponse response = annotateRequest.execute();
    return convertResponseToString(response);
  }

  public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

    int originalWidth = bitmap.getWidth();
    int originalHeight = bitmap.getHeight();
    int resizedWidth = maxDimension;
    int resizedHeight = maxDimension;

    if (originalHeight > originalWidth) {
      resizedHeight = maxDimension;
      resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
    } else if (originalWidth > originalHeight) {
      resizedWidth = maxDimension;
      resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
    } else if (originalHeight == originalWidth) {
      resizedHeight = maxDimension;
      resizedWidth = maxDimension;
    }
    return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);

  }

  private String convertResponseToString(BatchAnnotateImagesResponse response) {
    String message = "I found these things:\n\n";

    List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
    if (labels != null) {
      for (EntityAnnotation label : labels) {
        message += String.format("%s", label.getDescription());
        message += " \n ";
      }
    } else {
      message += "nothing";
    }

    return message;
  }
}
