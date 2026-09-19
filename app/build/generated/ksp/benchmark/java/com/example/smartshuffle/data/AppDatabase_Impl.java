package com.example.smartshuffle.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SongDao _songDao;

  private volatile PlayHistoryDao _playHistoryDao;

  private volatile RankDao _rankDao;

  private volatile QueueAssociationDao _queueAssociationDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `songs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `album` TEXT NOT NULL, `duration` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `albumArtUri` TEXT)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_songs_filePath` ON `songs` (`filePath`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `play_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `songId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `playType` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `song_ranks` (`songId` INTEGER NOT NULL, `rankValue` REAL NOT NULL, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`songId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `queue_associations` (`songIdA` INTEGER NOT NULL, `songIdB` INTEGER NOT NULL, `count` INTEGER NOT NULL, PRIMARY KEY(`songIdA`, `songIdB`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4e9baef6fc17ce0bf3912db3ca14301a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `songs`");
        db.execSQL("DROP TABLE IF EXISTS `play_history`");
        db.execSQL("DROP TABLE IF EXISTS `song_ranks`");
        db.execSQL("DROP TABLE IF EXISTS `queue_associations`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSongs = new HashMap<String, TableInfo.Column>(7);
        _columnsSongs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongs.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongs.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongs.put("album", new TableInfo.Column("album", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongs.put("duration", new TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongs.put("filePath", new TableInfo.Column("filePath", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongs.put("albumArtUri", new TableInfo.Column("albumArtUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSongs = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSongs = new HashSet<TableInfo.Index>(1);
        _indicesSongs.add(new TableInfo.Index("index_songs_filePath", true, Arrays.asList("filePath"), Arrays.asList("ASC")));
        final TableInfo _infoSongs = new TableInfo("songs", _columnsSongs, _foreignKeysSongs, _indicesSongs);
        final TableInfo _existingSongs = TableInfo.read(db, "songs");
        if (!_infoSongs.equals(_existingSongs)) {
          return new RoomOpenHelper.ValidationResult(false, "songs(com.example.smartshuffle.data.Song).\n"
                  + " Expected:\n" + _infoSongs + "\n"
                  + " Found:\n" + _existingSongs);
        }
        final HashMap<String, TableInfo.Column> _columnsPlayHistory = new HashMap<String, TableInfo.Column>(4);
        _columnsPlayHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayHistory.put("songId", new TableInfo.Column("songId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayHistory.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayHistory.put("playType", new TableInfo.Column("playType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayHistory = new TableInfo("play_history", _columnsPlayHistory, _foreignKeysPlayHistory, _indicesPlayHistory);
        final TableInfo _existingPlayHistory = TableInfo.read(db, "play_history");
        if (!_infoPlayHistory.equals(_existingPlayHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "play_history(com.example.smartshuffle.data.PlayHistory).\n"
                  + " Expected:\n" + _infoPlayHistory + "\n"
                  + " Found:\n" + _existingPlayHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsSongRanks = new HashMap<String, TableInfo.Column>(3);
        _columnsSongRanks.put("songId", new TableInfo.Column("songId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongRanks.put("rankValue", new TableInfo.Column("rankValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSongRanks.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSongRanks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSongRanks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSongRanks = new TableInfo("song_ranks", _columnsSongRanks, _foreignKeysSongRanks, _indicesSongRanks);
        final TableInfo _existingSongRanks = TableInfo.read(db, "song_ranks");
        if (!_infoSongRanks.equals(_existingSongRanks)) {
          return new RoomOpenHelper.ValidationResult(false, "song_ranks(com.example.smartshuffle.data.SongRank).\n"
                  + " Expected:\n" + _infoSongRanks + "\n"
                  + " Found:\n" + _existingSongRanks);
        }
        final HashMap<String, TableInfo.Column> _columnsQueueAssociations = new HashMap<String, TableInfo.Column>(3);
        _columnsQueueAssociations.put("songIdA", new TableInfo.Column("songIdA", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueAssociations.put("songIdB", new TableInfo.Column("songIdB", "INTEGER", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsQueueAssociations.put("count", new TableInfo.Column("count", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysQueueAssociations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesQueueAssociations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoQueueAssociations = new TableInfo("queue_associations", _columnsQueueAssociations, _foreignKeysQueueAssociations, _indicesQueueAssociations);
        final TableInfo _existingQueueAssociations = TableInfo.read(db, "queue_associations");
        if (!_infoQueueAssociations.equals(_existingQueueAssociations)) {
          return new RoomOpenHelper.ValidationResult(false, "queue_associations(com.example.smartshuffle.data.QueueAssociation).\n"
                  + " Expected:\n" + _infoQueueAssociations + "\n"
                  + " Found:\n" + _existingQueueAssociations);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4e9baef6fc17ce0bf3912db3ca14301a", "3d862b787c1b3339df06781c75fc127a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "songs","play_history","song_ranks","queue_associations");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `songs`");
      _db.execSQL("DELETE FROM `play_history`");
      _db.execSQL("DELETE FROM `song_ranks`");
      _db.execSQL("DELETE FROM `queue_associations`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SongDao.class, SongDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlayHistoryDao.class, PlayHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(RankDao.class, RankDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(QueueAssociationDao.class, QueueAssociationDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SongDao songDao() {
    if (_songDao != null) {
      return _songDao;
    } else {
      synchronized(this) {
        if(_songDao == null) {
          _songDao = new SongDao_Impl(this);
        }
        return _songDao;
      }
    }
  }

  @Override
  public PlayHistoryDao playHistoryDao() {
    if (_playHistoryDao != null) {
      return _playHistoryDao;
    } else {
      synchronized(this) {
        if(_playHistoryDao == null) {
          _playHistoryDao = new PlayHistoryDao_Impl(this);
        }
        return _playHistoryDao;
      }
    }
  }

  @Override
  public RankDao rankDao() {
    if (_rankDao != null) {
      return _rankDao;
    } else {
      synchronized(this) {
        if(_rankDao == null) {
          _rankDao = new RankDao_Impl(this);
        }
        return _rankDao;
      }
    }
  }

  @Override
  public QueueAssociationDao queueAssociationDao() {
    if (_queueAssociationDao != null) {
      return _queueAssociationDao;
    } else {
      synchronized(this) {
        if(_queueAssociationDao == null) {
          _queueAssociationDao = new QueueAssociationDao_Impl(this);
        }
        return _queueAssociationDao;
      }
    }
  }
}
