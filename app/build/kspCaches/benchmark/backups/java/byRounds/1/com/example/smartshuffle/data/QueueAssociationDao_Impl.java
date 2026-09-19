package com.example.smartshuffle.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class QueueAssociationDao_Impl implements QueueAssociationDao {
  private final RoomDatabase __db;

  private final SharedSQLiteStatement __preparedStmtOfIncrementAssociationCount;

  public QueueAssociationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__preparedStmtOfIncrementAssociationCount = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "INSERT INTO queue_associations (songIdA, songIdB, count) VALUES (?, ?, 1) ON CONFLICT(songIdA, songIdB) DO UPDATE SET count = count + 1";
        return _query;
      }
    };
  }

  @Override
  public Object incrementAssociationCount(final long songIdA, final long songIdB,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfIncrementAssociationCount.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, songIdA);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, songIdB);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfIncrementAssociationCount.release(_stmt);
        }
      }
    }, $completion);
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

  @Override
  public Object getAssociationsForSong(final long songId,
      final Continuation<? super List<QueueAssociation>> $completion) {
    final String _sql = "SELECT * FROM queue_associations WHERE songIdA = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, songId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<QueueAssociation>>() {
      @Override
      @NonNull
      public List<QueueAssociation> call() throws Exception {
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
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
