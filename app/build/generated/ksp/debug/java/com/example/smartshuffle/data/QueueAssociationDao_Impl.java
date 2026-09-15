package com.example.smartshuffle.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class QueueAssociationDao_Impl implements QueueAssociationDao {
  private final RoomDatabase __db;

  public QueueAssociationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public List<QueueAssociation> getAllAssociations() {
    final String _sql = "SELECT * FROM queue_associations";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfSongIdA = CursorUtil.getColumnIndexOrThrow(_cursor, "songIdA");
      final int _cursorIndexOfSongIdB = CursorUtil.getColumnIndexOrThrow(_cursor, "songIdB");
      final int _cursorIndexOfCount = CursorUtil.getColumnIndexOrThrow(_cursor, "count");
      final List<QueueAssociation> _result = new ArrayList<QueueAssociation>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final QueueAssociation _item;
        final long _tmpSongIdA;
        _tmpSongIdA = _cursor.getLong(_cursorIndexOfSongIdA);
        final long _tmpSongIdB;
        _tmpSongIdB = _cursor.getLong(_cursorIndexOfSongIdB);
        final int _tmpCount;
        _tmpCount = _cursor.getInt(_cursorIndexOfCount);
        _item = new QueueAssociation(_tmpSongIdA,_tmpSongIdB,_tmpCount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
