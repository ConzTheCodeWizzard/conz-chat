package com.conzchat.app.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u0016\u0010\u000f\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0010\u001a\u00020\u0007J\u0016\u0010\u0011\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\u0007J\u0006\u0010\u0013\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/conzchat/app/util/SoundManager;", "", "()V", "loaded", "", "soundIds", "", "", "", "soundPool", "Landroid/media/SoundPool;", "init", "", "context", "Landroid/content/Context;", "play", "name", "playNamed", "soundName", "release", "app_originalRelease"})
public final class SoundManager {
    @org.jetbrains.annotations.Nullable()
    private static android.media.SoundPool soundPool;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Map<java.lang.String, java.lang.Integer> soundIds = null;
    private static boolean loaded = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.SoundManager INSTANCE = null;
    
    private SoundManager() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void play(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void playNamed(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String soundName) {
    }
    
    public final void release() {
    }
}