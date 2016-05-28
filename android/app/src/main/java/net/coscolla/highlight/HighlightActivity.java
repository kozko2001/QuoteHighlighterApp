package net.coscolla.highlight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import net.coscolla.highlight.net.tesseract.Tesseract;
import net.coscolla.highlight.net.vision.VisionApi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import me.panavtec.drawableview.DrawableView;
import me.panavtec.drawableview.DrawableViewConfig;
import rx.android.schedulers.AndroidSchedulers;

import static rx.schedulers.Schedulers.io;

public class HighlightActivity extends AppCompatActivity {

  public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
  private static final String LOGTAG = "HighlightActivity";
  private ImageView capturedImage;
  private DrawableView paintView;

  private float currentScale = 1.0f;
  private Bitmap capturedBitmap;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_highlight);

    capturedImage = (ImageView) findViewById(R.id.captured_image);
    paintView = (DrawableView) findViewById(R.id.paintView);

    capturedBitmap = BitmapFactory.decodeFile(getImageFile());
    capturedImage.setImageBitmap(capturedBitmap);

    final ViewTreeObserver observer = capturedImage.getViewTreeObserver();
    observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override
      public boolean onPreDraw() {
        capturedImage.getViewTreeObserver().removeOnPreDrawListener(this);

        DrawableViewConfig config = new DrawableViewConfig();
        config.setStrokeColor(getResources().getColor(R.color.highlighter));
        config.setShowCanvasBounds(true); // If the view is bigger than canvas, with this the user will see the bounds (Recommended)

        config.setMinZoom(33.333f);
        config.setMaxZoom(33.333f);
        Matrix matrix = capturedImage.getImageMatrix();
        float[] v = new float[9];
        matrix.getValues(v);

        float sx = v[0];
        float sy = v[4];
        float offset_x = v[2];
        float offset_y = v[5];

        int height = (int) (capturedBitmap.getHeight() * sy);
        int width = (int) (capturedBitmap.getWidth() * sx);

        config.setCanvasHeight(height);
        config.setCanvasWidth(width);

        float strokeSize = Math.min(capturedImage.getWidth(), capturedImage.getHeight()) / 20.0f;
        strokeSize = (int) (strokeSize / sx);

        config.setStrokeWidth(strokeSize);

        paintView.setLayoutParams(new FrameLayout.LayoutParams(width, height));
        paintView.setTranslationX(offset_x);
        paintView.setTranslationY(offset_y);

        paintView.onScaleChange(sx);
        paintView.setConfig(config);
        paintView.setAlpha(0.5f);

        currentScale = sx;

        return false;
      }
    });

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    assert fab != null;
    fab.setOnClickListener(view -> {
      paintView.setVisibility(View.GONE);
      int originalWidth = capturedBitmap.getWidth();
      int originalHeight = capturedBitmap.getHeight();
      Bitmap highlightBitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);
      paintView.obtainBitmap(highlightBitmap);

      int w = highlightBitmap.getWidth();
      int h = highlightBitmap.getHeight();

      Log.e(LOGTAG, "height: " + h);

      Bitmap result = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);
      Canvas tempCanvas = new Canvas(result);
      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


      ColorMatrix ma = new ColorMatrix();
      ma.setSaturation(0);


      paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
      tempCanvas.drawBitmap(capturedBitmap, 0, 0, null);
      tempCanvas.drawBitmap(highlightBitmap, 0, 0, paint);
      paint.setXfermode(null);

      capturedImage.setImageBitmap(result);

      /*

        new Tesseract().ocr(result)
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                (text) -> {
                  showData(text);
                  Log.e(LOGTAG, text);
                }, (e) -> {
                  Log.e(LOGTAG, "ERROR: " + e.getMessage());
                  showData("ERROR " + e.getMessage());
                }, () -> {

                });


*/
      try {
      File file = new File(getImageFile() + "-kzk.jpg");
      OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
      result.compress(Bitmap.CompressFormat.JPEG, 100, os);
      os.close();

        new VisionApi().googleVisionOCR(Uri.fromFile(file), getContentResolver())
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
            (text) -> {
              showData(text);
              Log.e(LOGTAG, text);
            }, (e) -> {
              Log.e(LOGTAG, "ERROR: " + e.getMessage());
            }, () -> {

            });
      } catch (Exception ignored) {

      }
    });
  }

  private void showData(String text) {
    new AlertDialog.Builder(this)
        .setTitle("OCR")
        .setMessage(text)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // continue with delete
          }
        })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // do nothing
          }
        })
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show();
  }

  private String getImageFile() {
    return getIntent().getStringExtra(EXTRA_IMAGE);
  }
}
