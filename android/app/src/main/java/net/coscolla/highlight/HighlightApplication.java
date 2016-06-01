package net.coscolla.highlight;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.coscolla.highlight.model.DB;
import net.coscolla.highlight.utils.AnalyticsUtils;

import timber.log.Timber;

public class HighlightApplication extends Application {
  private static SQLiteDatabase db;
  private static AnalyticsUtils analyticsUtils;

  @Override
  public void onCreate() {
    super.onCreate();

    Timber.plant(new Timber.DebugTree());

    initDB();

    Stetho.initializeWithDefaults(this);

    analyticsUtils = new AnalyticsUtils(FirebaseAnalytics.getInstance(this));
  }

  private void initDB() {
    db = new DB(this).getWritableDatabase();
  }

  public static SQLiteDatabase getDb() {
    return db;
  }

  public static AnalyticsUtils getAnalytics() {
    return analyticsUtils;
  }
}
