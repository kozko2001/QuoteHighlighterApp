package net.coscolla.highlight;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.RxCameraData;
import com.ragnarok.rxcamera.config.CameraUtil;
import com.ragnarok.rxcamera.config.RxCameraConfig;
import com.ragnarok.rxcamera.config.RxCameraConfigChooser;
import com.ragnarok.rxcamera.request.Func;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import timber.log.Timber;

public class CameraActivity extends AppCompatActivity {

  private TextureView textureView;
  private RxCamera camera;
  private ImageButton captureButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);

    textureView = (TextureView) findViewById(R.id.preview_surface);
    captureButton =  (ImageButton) findViewById(R.id.camera_capture);

    if (captureButton != null) {
      captureButton.setOnClickListener(v -> {
        requestTakePicture();
      });
    }

    if(textureView != null) {
      setTouchFocus(textureView);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    openCamera();
  }

  @Override
  protected void onPause() {
    super.onPause();

    closeCamera();
  }


  private void setTouchFocus(@Nonnull TextureView textureView) {

    textureView.setOnTouchListener((v, event) -> {
      if (!checkCamera()) {
        return false;
      }
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        final float x = event.getX();
        final float y = event.getY();
        final Rect rect = CameraUtil.transferCameraAreaFromOuterSize(new Point((int)x, (int)y),
            new Point(textureView.getWidth(), textureView.getHeight()), 100);
        List<Camera.Area> areaList = Collections.singletonList(new Camera.Area(rect, 1000));
        Observable.zip(camera.action().areaFocusAction(areaList),
            camera.action().areaMeterAction(areaList),
            (Func2<RxCamera, RxCamera, Object>) (rxCamera, rxCamera2) -> rxCamera)
            .subscribe((r) -> {
              showLog(String.format("area focus and metering success, x: %s, y: %s, area: %s", x, y, rect.toShortString()));
            }, (e) -> {
              showLog("area focus and metering failed: " + e.getMessage());
            }, () -> {

            });
      }
      return false;
    });

  }

  private void openCamera() {
    RxCameraConfig config = RxCameraConfigChooser.obtain().
        useBackCamera().
        setAutoFocus(true).
        setPreferPreviewFrameRate(15, 30).
        setPreferPreviewSize(new Point(640, 480)).
        setHandleSurfaceEvent(true).
        setPreviewFormat(ImageFormat.NV21).
        get();

    Timber.d("config: " + config);
    RxCamera.open(this, config).flatMap(rxCamera -> {
      showLog("isopen: " + rxCamera.isOpenCamera() + ", thread: " + Thread.currentThread());
      camera = rxCamera;
      return rxCamera.bindTexture(textureView);
    }).flatMap(rxCamera -> {
      showLog("isbindsurface: " + rxCamera.isBindSurface() + ", thread: " + Thread.currentThread());
      return rxCamera.startPreview();
    }).flatMap(rxCamera1 -> rxCamera1.action().flashAction(true))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(rxCamera -> {
          camera = rxCamera;
          showLog("open camera success: " + camera);
        }, (e) -> {
          showLog("open camera error: " + e.getMessage());
        }, () -> {

        });
  }

  private void closeCamera() {
    if (camera != null) {
      camera.closeCamera();
    }
  }


  private void requestTakePicture() {
    if (!checkCamera()) {
      return;
    }

    String path = getDestinationFile();
    File file = new File(path);

    Bitmap bitmap = textureView.getBitmap();

    try {
      file.createNewFile();
      FileOutputStream fos = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    showLog("Save file on " + path);

    setResult(RESULT_OK, new Intent());
    finish();
  }

  private String getDestinationFile() {
    return ((Uri) getIntent().getParcelableExtra(MediaStore.EXTRA_OUTPUT)).getPath();
  }

  private void showLog(String s) {
    Timber.d(s);
  }


  private boolean checkCamera() {
    if (camera == null || !camera.isOpenCamera()) {
      return false;
    }
    return true;
  }
}
