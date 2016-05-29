package net.coscolla.highlight;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import net.coscolla.highlight.recognition.api.UploadImage;
import net.coscolla.highlight.utils.BitmapUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    if (fab != null) {
      fab.setOnClickListener((e) -> this.askPermissionsToTakePicture());
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  private void askPermissionsToTakePicture() {

    new TedPermission(this)
        .setPermissionListener(new PermissionListener() {
          @Override
          public void onPermissionGranted() {
            takePicture();
          }

          @Override
          public void onPermissionDenied(ArrayList<String> arrayList) {

          }
        })
        .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
        .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        .check();
  }

  private void takePicture() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      File outputFile;
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

}
