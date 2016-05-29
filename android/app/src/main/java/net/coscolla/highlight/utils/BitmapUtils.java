package net.coscolla.highlight.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapUtils {

  public static void rotateCameraBitmap(String file) {
    BitmapFactory.Options bounds = new BitmapFactory.Options();
    bounds.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(file, bounds);

    BitmapFactory.Options opts = new BitmapFactory.Options();
    Bitmap bm = BitmapFactory.decodeFile(file, opts);
    ExifInterface exif = null;
    try {
      exif = new ExifInterface(file);
    } catch (IOException e) {
    }
    String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
    int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

    int rotationAngle = 0;
    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
    if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
    if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

    Matrix matrix = new Matrix();
    matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
    Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);


    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
    byte[] bitmapdata = bos.toByteArray();

    File _file = new File(file);
    try {
      OutputStream os  = new BufferedOutputStream(new FileOutputStream(_file));
      rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
      os.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Bitmap loadFromFilePath(String filepath) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    return BitmapFactory.decodeFile(filepath, options);
  }

  public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

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
}
