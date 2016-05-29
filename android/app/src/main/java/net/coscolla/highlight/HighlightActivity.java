package net.coscolla.highlight;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
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

import net.coscolla.highlight.model.Highlight;
import net.coscolla.highlight.model.HighlightRepository;
import net.coscolla.highlight.recognition.Recognition;
import net.coscolla.highlight.recognition.vision.VisionApi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import me.panavtec.drawableview.DrawableView;
import me.panavtec.drawableview.DrawableViewConfig;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static rx.Observable.fromCallable;
import static rx.schedulers.Schedulers.io;

public class HighlightActivity extends AppCompatActivity {

  public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
  private static final String LOGTAG = "HighlightActivity";
  private ImageView capturedImage;
  private DrawableView paintView;
  private Bitmap capturedBitmap;
  private HighlightRepository repository;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_highlight);

    repository = new HighlightRepository(HighlightApplication.getDb());

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

      File file = null;
      try {
        file = new File(getImageFile() + "-kzk.jpg");
        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        result.compress(Bitmap.CompressFormat.JPEG, 100, os);
        os.close();
      }catch (Exception ignored) {
        file = null;
      }

      if(file != null) {
        Recognition recognition = new VisionApi();
        String highlightedImageFilePath = file.getAbsolutePath();
        recognition.recognition(file.getAbsolutePath())
            .flatMap((text) -> insertIntoDatabase(text, highlightedImageFilePath))
            .subscribeOn(io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                (highlight) -> {
                  Timber.d("Highlight model stored correctly :)" + highlight);
                  openListSuccess(highlight);
                }, (e) -> {
                  Timber.e("Highlight model could not be stored :(" + e);
                  openListError("Sorry something went wrong: " + e.getLocalizedMessage());
                }, () -> {

                });
      }
    });
  }

  private void openListSuccess(Highlight highlight) {
    Intent intent = new Intent(this, CaptureActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(CaptureActivity.NEW_HIGHLIGHT_ADDED, highlight);
    startActivity(intent);
  }

  private void openListError(String error) {
    Intent intent = new Intent(this, CaptureActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    intent.putExtra(CaptureActivity.NEW_HIGHLIGHT_ERROR, error);
    startActivity(intent);
  }


  private Observable<Highlight> insertIntoDatabase(String text, String highlight) {

    long ts = System.currentTimeMillis();
    return fromCallable(() -> repository.insert(getImageFile(), highlight, text, ts, ts));
  }

  private String getImageFile() {
    return getIntent().getStringExtra(EXTRA_IMAGE);
  }
}
