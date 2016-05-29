package net.coscolla.highlight;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import net.coscolla.highlight.model.Highlight;
import net.coscolla.highlight.utils.BitmapUtils;
import net.coscolla.highlight.view.list.HighlightListFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CaptureActivity extends AppCompatActivity {

  static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final String SAVE_STATE_CURRENT_FILE = "SAVE_STATE_CURRENT_FILE";
  public static final String NEW_HIGHLIGHT_ADDED = "INTENT_NEW_HIGHLIGHT_ADDED";
  public static final String NEW_HIGHLIGHT_ERROR = "INTENT_NEW_HIGHLIGHT_ERROR";

  private String mCurrentPhotoPath;
  private HighlightListFragment listFragment;
  private CoordinatorLayout coordinatorLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_capture);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

    //mCurrentPhotoPath = "/storage/emulated/0/Android/data/net.coscolla.highlight/files/JPEG_20160526_063232_1992924699.jpg";
    //startHighlight();

    if (fab != null) {
      fab.setOnClickListener((e) -> this.askPermissionsToTakePicture());
    }

    addHighlightList();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Highlight newHighlightAdded = intent.getParcelableExtra(NEW_HIGHLIGHT_ADDED);
    String newHighlightError = intent.getStringExtra(NEW_HIGHLIGHT_ERROR);

    if(newHighlightAdded != null && listFragment != null) {
      listFragment.newHighlightAdded(newHighlightAdded);
    }

    if(newHighlightError != null) {
      Snackbar snackbar = Snackbar.make(coordinatorLayout, newHighlightError, Snackbar.LENGTH_LONG);
      snackbar.show();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(SAVE_STATE_CURRENT_FILE, mCurrentPhotoPath);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mCurrentPhotoPath = savedInstanceState.getString(SAVE_STATE_CURRENT_FILE);
  }

  private void addHighlightList() {
    listFragment = HighlightListFragment.create();
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
        .add(R.id.content, listFragment)
        .commit();
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

}
