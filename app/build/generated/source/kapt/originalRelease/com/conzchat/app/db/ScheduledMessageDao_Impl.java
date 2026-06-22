package com.conzchat.app.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
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
public final class ScheduledMessageDao_Impl implements ScheduledMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScheduledMessage> __insertionAdapterOfScheduledMessage;

  private final SharedSQLiteStatement __preparedStmtOfMarkSent;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public ScheduledMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScheduledMessage = new EntityInsertionAdapter<ScheduledMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `scheduled_messages` (`id`,`chatId`,`recipientName`,`message`,`scheduledAt`,`sent`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScheduledMessage entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getChatId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getChatId());
        }
        if (entity.getRecipientName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRecipientName());
        }
        if (entity.getMessage() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getMessage());
        }
        statement.bindLong(5, entity.getScheduledAt());
        final int _tmp = entity.getSent() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__preparedStmtOfMarkSent = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE scheduled_messages SET sent = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scheduled_messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scheduled_messages";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ScheduledMessage msg, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScheduledMessage.insert(msg);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markSent(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSent.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkSent.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final int id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPending(final Continuation<? super List<ScheduledMessage>> $completion) {
    final String _sql = "SELECT * FROM scheduled_messages WHERE sent = 0 ORDER BY scheduledAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScheduledMessage>>() {
      @Override
      @NonNull
      public List<ScheduledMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfScheduledAt = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledAt");
          final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
          final List<ScheduledMessage> _result = new ArrayList<ScheduledMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledMessage _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpChatId;
            if (_cursor.isNull(_cursorIndexOfChatId)) {
              _tmpChatId = null;
            } else {
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            }
            final String _tmpRecipientName;
            if (_cursor.isNull(_cursorIndexOfRecipientName)) {
              _tmpRecipientName = null;
            } else {
              _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            }
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            final long _tmpScheduledAt;
            _tmpScheduledAt = _cursor.getLong(_cursorIndexOfScheduledAt);
            final boolean _tmpSent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSent);
            _tmpSent = _tmp != 0;
            _item = new ScheduledMessage(_tmpId,_tmpChatId,_tmpRecipientName,_tmpMessage,_tmpScheduledAt,_tmpSent);
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

  @Override
  public Object getAll(final Continuation<? super List<ScheduledMessage>> $completion) {
    final String _sql = "SELECT * FROM scheduled_messages ORDER BY scheduledAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ScheduledMessage>>() {
      @Override
      @NonNull
      public List<ScheduledMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfRecipientName = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientName");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfScheduledAt = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledAt");
          final int _cursorIndexOfSent = CursorUtil.getColumnIndexOrThrow(_cursor, "sent");
          final List<ScheduledMessage> _result = new ArrayList<ScheduledMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledMessage _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpChatId;
            if (_cursor.isNull(_cursorIndexOfChatId)) {
              _tmpChatId = null;
            } else {
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            }
            final String _tmpRecipientName;
            if (_cursor.isNull(_cursorIndexOfRecipientName)) {
              _tmpRecipientName = null;
            } else {
              _tmpRecipientName = _cursor.getString(_cursorIndexOfRecipientName);
            }
            final String _tmpMessage;
            if (_cursor.isNull(_cursorIndexOfMessage)) {
              _tmpMessage = null;
            } else {
              _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            }
            final long _tmpScheduledAt;
            _tmpScheduledAt = _cursor.getLong(_cursorIndexOfScheduledAt);
            final boolean _tmpSent;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSent);
            _tmpSent = _tmp != 0;
            _item = new ScheduledMessage(_tmpId,_tmpChatId,_tmpRecipientName,_tmpMessage,_tmpScheduledAt,_tmpSent);
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
