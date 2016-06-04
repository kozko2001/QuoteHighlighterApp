package net.coscolla.highlight.recognition.tesseract;

import android.graphics.Bitmap;

/*
import com.googlecode.leptonica.android.Binarize;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;
*/
import net.coscolla.highlight.recognition.Recognition;
import net.coscolla.highlight.utils.BitmapUtils;

import rx.Observable;

import static rx.Observable.fromCallable;

public class Tesseract implements Recognition {

  @Override
  public Observable<String> recognition(String filePath) {
    Bitmap bitmap = BitmapUtils.loadFromFilePath(filePath);
    return fromCallable(() -> tesseract(bitmap));
  }

  private String tesseract(Bitmap bitmap) {
  /*  TessBaseAPI baseApi = new TessBaseAPI();

    baseApi.init("/sdcard/tess", "eng");
    Pix image = ReadFile.readBitmap(bitmap);

    Binarize.sauvolaBinarizeTiled(image);

    baseApi.setImage(image);

    String recognizedText = baseApi.getUTF8Text();
    baseApi.end();

    return recognizedText;
  */
    return "NOT IMPLEMENTED";
  }
}
