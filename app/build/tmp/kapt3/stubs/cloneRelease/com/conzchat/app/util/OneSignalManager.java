package com.conzchat.app.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u000e\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u0004J\u0006\u0010\u000e\u001a\u00020\tJ\u0006\u0010\u000f\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/conzchat/app/util/OneSignalManager;", "", "()V", "ONESIGNAL_APP_ID", "", "TAG", "initialized", "", "init", "", "context", "Landroid/content/Context;", "login", "uid", "logout", "optIn", "app_cloneRelease"})
public final class OneSignalManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ONESIGNAL_APP_ID = "72d0a73d-b1ed-4ffa-9356-f84d79a0e0cc";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "OneSignalManager";
    private static boolean initialized = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.OneSignalManager INSTANCE = null;
    
    private OneSignalManager() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    /**
     * Call after user logs in. Links this device to the user's UID so
     * notifications are delivered to the right person.
     */
    public final void login(@org.jetbrains.annotations.NotNull()
    java.lang.String uid) {
    }
    
    /**
     * Call when user logs out so this device stops receiving their notifications.
     */
    public final void logout() {
    }
    
    /**
     * Opt in to push notifications (call after user grants permission).
     */
    public final void optIn() {
    }
}