package net.coscolla.highlight.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class HighlightRepository {
  private final SQLiteDatabase db;

  public HighlightRepository(SQLiteDatabase db) {
    this.db = db;
  }

  public void insert(SQLiteDatabase db, long _id, String original, String highlighted, String text, long created, long updated) {
    db.insert(Highlight.TABLE_NAME, null, new Highlight.Marshal()
        ._id(_id)
        .original(original)
        .highlighted(highlighted)
        .text(text)
        .ts_created(created)
        .ts_upldated(updated)
        .asContentValues());

  }

  public List<Highlight> filter(SQLiteDatabase db, String filter) {
    List<Highlight> result = new ArrayList<>();
    Cursor cursor = db.rawQuery(Highlight.SELECT_FILTER, new String[]{filter});
    while (cursor.moveToNext()) {
      result.add(Highlight.MAPPER.map(cursor));
    }
    cursor.close();
    return result;
  }

}
