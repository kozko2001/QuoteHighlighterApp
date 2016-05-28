package net.coscolla.highlight.net.tesseract;

import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

import rx.Observable;
import rx.functions.Func0;

import static rx.Observable.defer;
import static rx.Observable.fromCallable;

public class Tesseract {

  public Observable<String> ocr(Bitmap bitmap) {
    return fromCallable(() -> tesseract(bitmap));
  }

  private String tesseract(Bitmap bitmap) {
    TessBaseAPI baseApi = new TessBaseAPI();

    baseApi.init("/sdcard/tess", "eng");
    Pix image = ReadFile.readBitmap(bitmap);

    Binarize.sauvolaBinarizeTiled(image);

    baseApi.setImage(image);

    String recognizedText = baseApi.getUTF8Text();
    baseApi.end();

    return recognizedText;
  }
}
