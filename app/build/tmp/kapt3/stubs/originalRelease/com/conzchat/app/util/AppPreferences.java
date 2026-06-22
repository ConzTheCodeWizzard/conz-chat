package com.conzchat.app.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0004J\u001e\u0010\r\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\u0004J\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\fJ\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0016\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0018\u001a\u00020\nJ\u001e\u0010\u0019\u001a\u00020\u00172\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u000eJ\u001e\u0010\u0019\u001a\u00020\u00172\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u0004J\u0016\u0010\u001b\u001a\u00020\u00172\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001c\u001a\u00020\u0004J\u0016\u0010\u001d\u001a\u00020\u00172\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001e\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/conzchat/app/util/AppPreferences;", "", "()V", "KEY_BRIGHTNESS", "", "KEY_LAST_SEEN_PREFIX", "KEY_LAST_VERSION_SEEN", "KEY_THEME", "PREFS_NAME", "getBrightness", "", "context", "Landroid/content/Context;", "getLastSeen", "", "uid", "key", "defaultVal", "getLastVersionSeen", "getTheme", "prefs", "Landroid/content/SharedPreferences;", "setBrightness", "", "value", "setLastSeen", "time", "setLastVersionSeen", "version", "setTheme", "theme", "app_originalRelease"})
public final class AppPreferences {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "conzchat_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BRIGHTNESS = "brightness";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_THEME = "theme";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_SEEN_PREFIX = "last_seen_";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_LAST_VERSION_SEEN = "last_version_seen";
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.AppPreferences INSTANCE = null;
    
    private AppPreferences() {
        super();
    }
    
    private final android.content.SharedPreferences prefs(android.content.Context context) {
        return null;
    }
    
    public final int getBrightness(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return 0;
    }
    
    public final void setBrightness(@org.jetbrains.annotations.NotNull()
    android.content.Context context, int value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTheme(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void setTheme(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String theme) {
    }
    
    public final long getLastSeen(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String uid) {
        return 0L;
    }
    
    public final void setLastSeen(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String uid, long time) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLastSeen(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    java.lang.String defaultVal) {
        return null;
    }
    
    public final void setLastSeen(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLastVersionSeen(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void setLastVersionSeen(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String version) {
    }
}