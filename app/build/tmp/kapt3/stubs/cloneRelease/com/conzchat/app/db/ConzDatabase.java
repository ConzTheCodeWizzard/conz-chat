package com.conzchat.app.db;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \t2\u00020\u0001:\u0001\tB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&\u00a8\u0006\n"}, d2 = {"Lcom/conzchat/app/db/ConzDatabase;", "Landroidx/room/RoomDatabase;", "()V", "savedAccountDao", "Lcom/conzchat/app/db/SavedAccountDao;", "scheduledMessageDao", "Lcom/conzchat/app/db/ScheduledMessageDao;", "vaultMessageDao", "Lcom/conzchat/app/db/VaultMessageDao;", "Companion", "app_cloneRelease"})
@androidx.room.Database(entities = {com.conzchat.app.db.ScheduledMessage.class, com.conzchat.app.db.VaultMessage.class, com.conzchat.app.db.SavedAccount.class}, version = 2, exportSchema = false)
public abstract class ConzDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.conzchat.app.db.ConzDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.db.ConzDatabase.Companion Companion = null;
    
    public ConzDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.conzchat.app.db.ScheduledMessageDao scheduledMessageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.conzchat.app.db.VaultMessageDao vaultMessageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.conzchat.app.db.SavedAccountDao savedAccountDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/conzchat/app/db/ConzDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/conzchat/app/db/ConzDatabase;", "get", "context", "Landroid/content/Context;", "app_cloneRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.db.ConzDatabase get(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}