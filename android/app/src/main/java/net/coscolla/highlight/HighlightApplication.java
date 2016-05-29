package net.coscolla.highlight;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;

import net.coscolla.highlight.model.DB;

import timber.log.Timber;

public class HighlightApplication extends Application {
  private static SQLiteDatabase db;

  @Override
  public void onCreate() {
    super.onCreate();

    Timber.plant(new Timber.DebugTree());

    initDB();

    Stetho.initializeWithDefaults(this);
  }

  private void initDB() {
    db = new DB(this).getWritableDatabase();
  }

  public static SQLiteDatabase getDb() {
    return db;
  }
}
