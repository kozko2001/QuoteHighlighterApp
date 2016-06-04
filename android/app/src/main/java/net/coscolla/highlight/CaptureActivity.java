package net.coscolla.highlight;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import net.coscolla.highlight.model.Highlight;
import net.coscolla.highlight.utils.FileUtils;
import net.coscolla.highlight.view.list.HighlightListFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CaptureActivity extends AppCompatActivity {

  static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int REQUEST_IMAGE_GALLERY = 2;
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

    if(savedInstanceState != null) {
      mCurrentPhotoPath = savedInstanceState.getString(SAVE_STATE_CURRENT_FILE);
    }

    //mCurrentPhotoPath = "/storage/emulated/0/Android/data/net.coscolla.highlight/files/JPEG_20160526_063232_1992924699.jpg";
    //startHighlight();

    if (fab != null) {
      fab.setOnClickListener((e) -> this.askPermissionsToTakePicture());
    }

    if(savedInstanceState == null) {
      addHighlightList();
    }
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
      showError(newHighlightError);
    }
  }

  private void showError(String errorMessage) {
    Snackbar snackbar = Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG);
    snackbar.show();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(SAVE_STATE_CURRENT_FILE, mCurrentPhotoPath);
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
    HighlightApplication.getAnalytics().logEvent("START_CAPTURE");

    Intent takePictureIntent = new Intent(this, CameraActivity.class);
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
      HighlightApplication.getAnalytics().logEvent("CAPTURE_DONE");

      startHighlight(mCurrentPhotoPath);
    } else if(requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
      HighlightApplication.getAnalytics().logEvent("IMPORT_GALLERY_DONE");

      File output = copyFileFromGallery(data.getData());

      if(output != null) {
        startHighlight(output.getAbsolutePath());
      } else {
        showError("Error retrieving the file from gallery");
      }
    }
  }



  private void startHighlight(String path) {
    Intent intent = new Intent(this, HighlightActivity.class);
    intent.putExtra(HighlightActivity.EXTRA_IMAGE, path);
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

  private File copyFileFromGallery(Uri selectedImage) {

    File outputFile = null;
    String picturePath = getFileFromGallery(selectedImage);

    if(picturePath != null) {
      try {
        outputFile = createImageFile();

        FileUtils.copy(new File(picturePath), outputFile);
        return outputFile;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  private String getFileFromGallery(Uri selectedImage) {
    String[] filePathColumn = {MediaStore.Images.Media.DATA};
    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);

    if (cursor == null || cursor.getCount() < 1) {
      return null;
    }

    cursor.moveToFirst();
    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

    if (columnIndex < 0) {
      return null;
    }

    String picturePath = cursor.getString(columnIndex);

    cursor.close(); // close cursor

    return picturePath;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == R.id.action_from_gallery) {
      Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(i, REQUEST_IMAGE_GALLERY);

      return true;
    }

    return false;
  }
}
