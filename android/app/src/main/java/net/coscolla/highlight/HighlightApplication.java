package net.coscolla.highlight;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class HighlightApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Stetho.initializeWithDefaults(this);
  }
}
