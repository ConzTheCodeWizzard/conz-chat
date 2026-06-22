package com.conzchat.app.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000b0\nH\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\r\u001a\u00020\u00032\u0006\u0010\u000e\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u0016\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/conzchat/app/db/ScheduledMessageDao;", "", "delete", "", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAll", "", "Lcom/conzchat/app/db/ScheduledMessage;", "getPending", "insert", "msg", "(Lcom/conzchat/app/db/ScheduledMessage;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markSent", "app_originalRelease"})
@androidx.room.Dao()
public abstract interface ScheduledMessageDao {
    
    @androidx.room.Query(value = "SELECT * FROM scheduled_messages WHERE sent = 0 ORDER BY scheduledAt ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getPending(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.conzchat.app.db.ScheduledMessage>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM scheduled_messages ORDER BY scheduledAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.conzchat.app.db.ScheduledMessage>> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.conzchat.app.db.ScheduledMessage msg, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE scheduled_messages SET sent = 1 WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSent(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM scheduled_messages WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM scheduled_messages")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}