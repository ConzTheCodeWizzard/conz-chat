package com.conzchat.app.db;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ConzDatabase_Impl extends ConzDatabase {
  private volatile ScheduledMessageDao _scheduledMessageDao;

  private volatile VaultMessageDao _vaultMessageDao;

  private volatile SavedAccountDao _savedAccountDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `scheduled_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `chatId` TEXT NOT NULL, `recipientName` TEXT NOT NULL, `message` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, `sent` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vault_messages` (`messageId` TEXT NOT NULL, `chatId` TEXT NOT NULL, `senderName` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isImage` INTEGER NOT NULL, `imageUrl` TEXT NOT NULL, PRIMARY KEY(`messageId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `saved_accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `password` TEXT NOT NULL, `username` TEXT NOT NULL, `avatarUrl` TEXT NOT NULL, `savedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '032eac52acffdab507a4917f434519c7')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `scheduled_messages`");
        db.execSQL("DROP TABLE IF EXISTS `vault_messages`");
        db.execSQL("DROP TABLE IF EXISTS `saved_accounts`");
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
        final HashMap<String, TableInfo.Column> _columnsScheduledMessages = new HashMap<String, TableInfo.Column>(6);
        _columnsScheduledMessages.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("chatId", new TableInfo.Column("chatId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("recipientName", new TableInfo.Column("recipientName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("message", new TableInfo.Column("message", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("scheduledAt", new TableInfo.Column("scheduledAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("sent", new TableInfo.Column("sent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScheduledMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScheduledMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScheduledMessages = new TableInfo("scheduled_messages", _columnsScheduledMessages, _foreignKeysScheduledMessages, _indicesScheduledMessages);
        final TableInfo _existingScheduledMessages = TableInfo.read(db, "scheduled_messages");
        if (!_infoScheduledMessages.equals(_existingScheduledMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "scheduled_messages(com.conzchat.app.db.ScheduledMessage).\n"
                  + " Expected:\n" + _infoScheduledMessages + "\n"
                  + " Found:\n" + _existingScheduledMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsVaultMessages = new HashMap<String, TableInfo.Column>(7);
        _columnsVaultMessages.put("messageId", new TableInfo.Column("messageId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultMessages.put("chatId", new TableInfo.Column("chatId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultMessages.put("senderName", new TableInfo.Column("senderName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultMessages.put("isImage", new TableInfo.Column("isImage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaultMessages.put("imageUrl", new TableInfo.Column("imageUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVaultMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVaultMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVaultMessages = new TableInfo("vault_messages", _columnsVaultMessages, _foreignKeysVaultMessages, _indicesVaultMessages);
        final TableInfo _existingVaultMessages = TableInfo.read(db, "vault_messages");
        if (!_infoVaultMessages.equals(_existingVaultMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "vault_messages(com.conzchat.app.db.VaultMessage).\n"
                  + " Expected:\n" + _infoVaultMessages + "\n"
                  + " Found:\n" + _existingVaultMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsSavedAccounts = new HashMap<String, TableInfo.Column>(6);
        _columnsSavedAccounts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAccounts.put("email", new TableInfo.Column("email", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAccounts.put("password", new TableInfo.Column("password", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAccounts.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAccounts.put("avatarUrl", new TableInfo.Column("avatarUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSavedAccounts.put("savedAt", new TableInfo.Column("savedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSavedAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSavedAccounts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSavedAccounts = new TableInfo("saved_accounts", _columnsSavedAccounts, _foreignKeysSavedAccounts, _indicesSavedAccounts);
        final TableInfo _existingSavedAccounts = TableInfo.read(db, "saved_accounts");
        if (!_infoSavedAccounts.equals(_existingSavedAccounts)) {
          return new RoomOpenHelper.ValidationResult(false, "saved_accounts(com.conzchat.app.db.SavedAccount).\n"
                  + " Expected:\n" + _infoSavedAccounts + "\n"
                  + " Found:\n" + _existingSavedAccounts);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "032eac52acffdab507a4917f434519c7", "d4c2904dca4b1cb7bc9bc60b3d1b13c3");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "scheduled_messages","vault_messages","saved_accounts");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `scheduled_messages`");
      _db.execSQL("DELETE FROM `vault_messages`");
      _db.execSQL("DELETE FROM `saved_accounts`");
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
    _typeConvertersMap.put(ScheduledMessageDao.class, ScheduledMessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VaultMessageDao.class, VaultMessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SavedAccountDao.class, SavedAccountDao_Impl.getRequiredConverters());
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
  public ScheduledMessageDao scheduledMessageDao() {
    if (_scheduledMessageDao != null) {
      return _scheduledMessageDao;
    } else {
      synchronized(this) {
        if(_scheduledMessageDao == null) {
          _scheduledMessageDao = new ScheduledMessageDao_Impl(this);
        }
        return _scheduledMessageDao;
      }
    }
  }

  @Override
  public VaultMessageDao vaultMessageDao() {
    if (_vaultMessageDao != null) {
      return _vaultMessageDao;
    } else {
      synchronized(this) {
        if(_vaultMessageDao == null) {
          _vaultMessageDao = new VaultMessageDao_Impl(this);
        }
        return _vaultMessageDao;
      }
    }
  }

  @Override
  public SavedAccountDao savedAccountDao() {
    if (_savedAccountDao != null) {
      return _savedAccountDao;
    } else {
      synchronized(this) {
        if(_savedAccountDao == null) {
          _savedAccountDao = new SavedAccountDao_Impl(this);
        }
        return _savedAccountDao;
      }
    }
  }
}
