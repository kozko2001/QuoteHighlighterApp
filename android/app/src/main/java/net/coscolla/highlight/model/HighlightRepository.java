package net.coscolla.highlight.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

import static rx.Observable.from;
import static rx.Observable.fromCallable;

public class HighlightRepository {
  private final SQLiteDatabase db;

  public HighlightRepository(SQLiteDatabase db) {
    this.db = db;
  }

  public Highlight insert(String original, String highlighted, String text, long created, long updated) {
    long id = db.insertOrThrow(Highlight.TABLE_NAME, null, new Highlight.Marshal()
        .original(original)
        .highlighted(highlighted)
        .text(text)
        .ts_created(created)
        .ts_upldated(updated)
        .asContentValues());

    return findById(id);
  }

  private Highlight findById(long id) {
    Cursor cursor = db.rawQuery(Highlight.SELECT_BY_ID, new String[]{"" + id});
    Highlight highlight = null;
    while (cursor.moveToNext()) {
      highlight = Highlight.MAPPER.map(cursor);
    }
    cursor.close();
    return highlight;
  }


  private List<Highlight> filter(SQLiteDatabase db, String filter) {
    List<Highlight> result = new ArrayList<>();
    Cursor cursor = db.rawQuery(Highlight.SELECT_FILTER, new String[] {"%" + filter + "%"});
    while (cursor.moveToNext()) {
      result.add(Highlight.MAPPER.map(cursor));
    }
    cursor.close();
    return result;
  }

  public void updateText(String text, long id) {
    db.execSQL(Highlight.UPDATE_TEXT, new String[]{text, "" + id});
  }

  public Observable<Highlight> filter(String filter) {
    return fromCallable(() -> filter(db, filter))
        .flatMap(list -> from(list));
  }
}
