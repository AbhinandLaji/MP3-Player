package com.example.smartshuffle.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlayHistoryDao_Impl implements PlayHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayHistory> __insertionAdapterOfPlayHistory;

  private final Converters __converters = new Converters();

  public PlayHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayHistory = new EntityInsertionAdapter<PlayHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `play_history` (`id`,`songId`,`timestamp`,`playType`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayHistory entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getSongId());
        statement.bindLong(3, entity.getTimestamp());
        final String _tmp = __converters.fromPlayType(entity.getPlayType());
        statement.bindString(4, _tmp);
      }
    };
  }

  @Override
  public Object insert(final PlayHistory playHistory,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayHistory.insert(playHistory);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public List<PlayHistory> getAllHistory() {
    final String _sql = "SELECT * FROM play_history";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSongId = CursorUtil.getColumnIndexOrThrow(_cursor, "songId");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfPlayType = CursorUtil.getColumnIndexOrThrow(_cursor, "playType");
      final List<PlayHistory> _result = new ArrayList<PlayHistory>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final PlayHistory _item;
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        final long _tmpSongId;
        _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        final PlayType _tmpPlayType;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPlayType);
        _tmpPlayType = __converters.toPlayType(_tmp);
        _item = new PlayHistory(_tmpId,_tmpSongId,_tmpTimestamp,_tmpPlayType);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public Flow<List<PlayCount>> getPlayCounts() {
    final String _sql = "SELECT songId, COUNT(id) as playCount FROM play_history GROUP BY songId";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"play_history"}, new Callable<List<PlayCount>>() {
      @Override
      @NonNull
      public List<PlayCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSongId = 0;
          final int _cursorIndexOfPlayCount = 1;
          final List<PlayCount> _result = new ArrayList<PlayCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlayCount _item;
            final long _tmpSongId;
            _tmpSongId = _cursor.getLong(_cursorIndexOfSongId);
            final int _tmpPlayCount;
            _tmpPlayCount = _cursor.getInt(_cursorIndexOfPlayCount);
            _item = new PlayCount(_tmpSongId,_tmpPlayCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
