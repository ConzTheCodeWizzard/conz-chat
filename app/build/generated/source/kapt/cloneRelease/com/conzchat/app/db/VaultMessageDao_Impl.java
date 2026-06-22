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
public final class VaultMessageDao_Impl implements VaultMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VaultMessage> __insertionAdapterOfVaultMessage;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public VaultMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVaultMessage = new EntityInsertionAdapter<VaultMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `vault_messages` (`messageId`,`chatId`,`senderName`,`content`,`timestamp`,`isImage`,`imageUrl`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VaultMessage entity) {
        if (entity.getMessageId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getMessageId());
        }
        if (entity.getChatId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getChatId());
        }
        if (entity.getSenderName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSenderName());
        }
        if (entity.getContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getContent());
        }
        statement.bindLong(5, entity.getTimestamp());
        final int _tmp = entity.isImage() ? 1 : 0;
        statement.bindLong(6, _tmp);
        if (entity.getImageUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getImageUrl());
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM vault_messages WHERE messageId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM vault_messages";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final VaultMessage msg, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfVaultMessage.insert(msg);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
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
  public Object getAll(final Continuation<? super List<VaultMessage>> $completion) {
    final String _sql = "SELECT * FROM vault_messages ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<VaultMessage>>() {
      @Override
      @NonNull
      public List<VaultMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "messageId");
          final int _cursorIndexOfChatId = CursorUtil.getColumnIndexOrThrow(_cursor, "chatId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsImage = CursorUtil.getColumnIndexOrThrow(_cursor, "isImage");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final List<VaultMessage> _result = new ArrayList<VaultMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VaultMessage _item;
            final String _tmpMessageId;
            if (_cursor.isNull(_cursorIndexOfMessageId)) {
              _tmpMessageId = null;
            } else {
              _tmpMessageId = _cursor.getString(_cursorIndexOfMessageId);
            }
            final String _tmpChatId;
            if (_cursor.isNull(_cursorIndexOfChatId)) {
              _tmpChatId = null;
            } else {
              _tmpChatId = _cursor.getString(_cursorIndexOfChatId);
            }
            final String _tmpSenderName;
            if (_cursor.isNull(_cursorIndexOfSenderName)) {
              _tmpSenderName = null;
            } else {
              _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpIsImage;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsImage);
            _tmpIsImage = _tmp != 0;
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            _item = new VaultMessage(_tmpMessageId,_tmpChatId,_tmpSenderName,_tmpContent,_tmpTimestamp,_tmpIsImage,_tmpImageUrl);
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
