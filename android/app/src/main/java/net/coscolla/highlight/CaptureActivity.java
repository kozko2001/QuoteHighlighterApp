package net.coscolla.highlight;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import net.coscolla.highlight.net.api.UploadImage;
import net.coscolla.highlight.net.vision.VisionApi;
import net.coscolla.highlight.utils.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

import static rx.schedulers.Schedulers.io;

public class CaptureActivity extends AppCompatActivity {

  static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int REQUEST_PERMISSIONS = 2;
  private static final String LOGTAG = "CaptureActivity";
  private ImageView imagePreview;
  private TextView imageText;
  private String mCurrentPhotoPath;
  private UploadImage uploader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_capture);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    imagePreview = (ImageView) this.findViewById(R.id.image_preview); // TODO Databinding
    imageText = (TextView) findViewById(R.id.image_text);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

    mCurrentPhotoPath = "/storage/emulated/0/Android/data/net.coscolla.highlight/files/JPEG_20160526_063232_1992924699.jpg";
    //startHighlight();

    fab.setOnClickListener((e) -> this.takePicture());

  }

  @Override
  protected void onResume() {
    super.onResume();
    checkPermissions();
  }

  private void takePicture() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      File outputFile = null;
      try {
        outputFile = createImageFile();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      BitmapUtils.rotateCameraBitmap(mCurrentPhotoPath);

      setPic();

      startHighlight();
    }
  }

  private void startHighlight() {
    Intent intent = new Intent(this, HighlightActivity.class);
    intent.putExtra(HighlightActivity.EXTRA_IMAGE, mCurrentPhotoPath);
    startActivity(intent);
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = getExternalFilesDir(null);
    File image = File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    );

    // Save a file: path for use with ACTION_VIEW intents
    mCurrentPhotoPath = image.getAbsolutePath();
    return image;
  }


  private void setPic() {
    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
    imagePreview.setImageBitmap(bitmap);
  }

  private void checkPermissions() {
    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.CAMERA},
          REQUEST_PERMISSIONS);
    }

    permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
          REQUEST_PERMISSIONS);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_capture, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
