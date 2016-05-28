package net.coscolla.highlight;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.stetho.Stetho;

import net.coscolla.highlight.model.DB;

public class HighlightApplication extends Application {
  private static SQLiteDatabase db;

  @Override
  public void onCreate() {
    super.onCreate();

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
