package com.conzchat.app.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eJ\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\fH\u0002J&\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0019\u001a\u00020\u0013J\u000e\u0010\u001a\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013J\u0010\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/conzchat/app/util/VibeSyncManager;", "", "()V", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "db", "Lcom/google/firebase/firestore/FirebaseFirestore;", "listener", "Lcom/google/firebase/firestore/ListenerRegistration;", "pulseAnimator", "Landroid/animation/ValueAnimator;", "getVibeColor", "", "myUid", "", "theirUid", "startPulse", "", "view", "Landroid/view/View;", "color", "startVibeSync", "context", "Landroid/content/Context;", "chatId", "targetView", "stop", "stopPulse", "app_originalRelease"})
public final class VibeSyncManager {
    @org.jetbrains.annotations.NotNull()
    private static final com.google.firebase.firestore.FirebaseFirestore db = null;
    @org.jetbrains.annotations.NotNull()
    private static final com.google.firebase.auth.FirebaseAuth auth = null;
    @org.jetbrains.annotations.Nullable()
    private static com.google.firebase.firestore.ListenerRegistration listener;
    @org.jetbrains.annotations.Nullable()
    private static android.animation.ValueAnimator pulseAnimator;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.VibeSyncManager INSTANCE = null;
    
    private VibeSyncManager() {
        super();
    }
    
    public final int getVibeColor(@org.jetbrains.annotations.NotNull()
    java.lang.String myUid, @org.jetbrains.annotations.NotNull()
    java.lang.String theirUid) {
        return 0;
    }
    
    public final void startVibeSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String chatId, @org.jetbrains.annotations.NotNull()
    java.lang.String theirUid, @org.jetbrains.annotations.NotNull()
    android.view.View targetView) {
    }
    
    private final void startPulse(android.view.View view, int color) {
    }
    
    private final void stopPulse(android.view.View view) {
    }
    
    public final void stop(@org.jetbrains.annotations.NotNull()
    android.view.View view) {
    }
}