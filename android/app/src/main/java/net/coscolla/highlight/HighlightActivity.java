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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;

import net.coscolla.highlight.model.Highlight;
import net.coscolla.highlight.model.HighlightRepository;
import net.coscolla.highlight.recognition.Recognition;
import net.coscolla.highlight.recognition.vision.VisionApi;
import net.coscolla.highlight.view.dialogs.RecognizingProgressDialog;

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

        initializePaintView();

        return false;
      }
    });

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    if (fab != null) {
      fab.setOnClickListener(view -> {
        HighlightApplication.getAnalytics().logEvent("HIGHLIGHT");

        paintView.setVisibility(View.GONE);
        int originalWidth = capturedBitmap.getWidth();
        int originalHeight = capturedBitmap.getHeight();
        Bitmap highlightBitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888);
        paintView.obtainBitmap(highlightBitmap);

        Bitmap maskedBitmap = createMaskedBitmap(originalWidth, originalHeight, highlightBitmap);
        Bitmap combinedBitmap = createCombinedBitmap(capturedBitmap, highlightBitmap);

        File maskedImageFile = writeBitmapToFile(maskedBitmap, "-masked");
        File combinedFile = writeBitmapToFile(combinedBitmap, "-combined");

        if (maskedBitmap != null) {
          maskedBitmap.recycle();
        }

        if(combinedBitmap != null) {
          combinedBitmap.recycle();
        }

        startRecognition(maskedImageFile, combinedFile);
      });
    }
  }

  /**
   * Initializes the paint view, important since the paint view must have the same size
   * as the imageview at the background, is important that the imageview is already measured
   */
  private void initializePaintView() {
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
  }

  private void startRecognition(File maskImage, File combinedImage) {
    startProgress();

    Recognition recognition = new VisionApi();
    String highlightedImageFilePath = combinedImage.getAbsolutePath();
    recognition.recognition(maskImage.getAbsolutePath())
        .flatMap((text) -> insertIntoDatabase(text, highlightedImageFilePath))
        .subscribeOn(io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            (highlight) -> {
              Timber.d("Highlight model stored correctly :)" + highlight);
              HighlightApplication.getAnalytics().logEvent("OCR_PROCESS_OK");
              stopProgress();
              openListSuccess(highlight);
            }, (e) -> {
              HighlightApplication.getAnalytics().logEvent("OCR_PROCESS_ERROR");
              Timber.e("Highlight model could not be stored :(" + e);
              stopProgress();
              openListError("Sorry something went wrong: " + e.getLocalizedMessage());
            }, () -> {
              maskImage.delete();
              combinedImage.delete();
            });
  }

  private void startProgress() {
    RecognizingProgressDialog dialog = new RecognizingProgressDialog();
    dialog.show(getSupportFragmentManager(), "PROGRESS-DIALOG");
  }

  private void stopProgress() {
    DialogFragment dialog = (DialogFragment) getSupportFragmentManager().findFragmentByTag("PROGRESS-DIALOG");
    if(dialog != null) {
      dialog.dismiss();
    }
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

  /**
   * Creates a bitmap that is the original bitmap but masked with the selection all that is not selected
   * is white
   *
   * @param originalWidth
   * @param originalHeight
   * @param highlightBitmap
   * @return
   */
  @Nullable
  private Bitmap createMaskedBitmap(int originalWidth, int originalHeight, Bitmap highlightBitmap) {
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

    return  result;
  }

  @Nullable
  private Bitmap createCombinedBitmap(Bitmap capturedBitmap, Bitmap highlightBitmap) {
    int h = highlightBitmap.getHeight();

    Log.e(LOGTAG, "height: " + h);

    Bitmap result = Bitmap.createBitmap(capturedBitmap.getWidth(), capturedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas tempCanvas = new Canvas(result);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    tempCanvas.drawBitmap(capturedBitmap, 0, 0, null);
    paint.setAlpha(127);
    tempCanvas.drawBitmap(highlightBitmap, 0, 0, paint);

    return  result;
  }

  @Nullable
  private File writeBitmapToFile(Bitmap result, String name) {
    File file = null;
    try {
      file = new File(getImageFile() + "name" + ".jpg");
      OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
      result.compress(Bitmap.CompressFormat.JPEG, 100, os);
      os.close();
    }catch (Exception ignored) {
      file = null;
    }
    return file;
  }


  private Observable<Highlight> insertIntoDatabase(String text, String highlight) {
    long ts = System.currentTimeMillis();
    return fromCallable(() -> repository.insert(getImageFile(), highlight, text, ts, ts));
  }

  private String getImageFile() {
    return getIntent().getStringExtra(EXTRA_IMAGE);
  }
}
