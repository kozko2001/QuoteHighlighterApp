package net.coscolla.highlight.recognition.vision;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
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

import net.coscolla.highlight.PrivateConstants;
import net.coscolla.highlight.recognition.Recognition;
import net.coscolla.highlight.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static rx.Observable.fromCallable;
import static rx.Observable.merge;

/**
 * Copied from https://github.com/GoogleCloudPlatform/cloud-vision/blob/master/android/CloudVision/app/src/main/java/com/google/sample/cloudvision/MainActivity.java
 */
public class VisionApi implements Recognition {

  private static final java.lang.String CLOUD_VISION_API_KEY = PrivateConstants.VISION_API;
  private static final String LOGTAG = "VisionApi";


  @Override
  public Observable<String> recognition(String filePath) {

    return fromCallable(() -> {
      Bitmap bitmap_orig = BitmapUtils.loadFromFilePath(filePath);
      Bitmap bitmap = BitmapUtils.scaleBitmapDown(bitmap_orig, 1200);
      bitmap_orig.recycle();

      return callCloudVision(bitmap);
    });
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


  private String convertResponseToString(BatchAnnotateImagesResponse response) {

    List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
    if (labels != null && labels.size() > 0) {

      EntityAnnotation label = labels.get(0);
      return label.getDescription();
    }

    return "nothing found";
  }
}
