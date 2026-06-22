package com.conzchat.app.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\u00042\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006J\u0006\u0010\r\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/conzchat/app/util/SessionManager;", "", "()V", "mySessionId", "", "onKicked", "Lkotlin/Function0;", "", "sessionListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "startSessionGuard", "uid", "onKickedCallback", "stopSessionGuard", "app_originalRelease"})
public final class SessionManager {
    @org.jetbrains.annotations.NotNull()
    private static java.lang.String mySessionId = "";
    @org.jetbrains.annotations.Nullable()
    private static com.google.firebase.firestore.ListenerRegistration sessionListener;
    @org.jetbrains.annotations.Nullable()
    private static kotlin.jvm.functions.Function0<kotlin.Unit> onKicked;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.SessionManager INSTANCE = null;
    
    private SessionManager() {
        super();
    }
    
    public final void startSessionGuard(@org.jetbrains.annotations.NotNull()
    java.lang.String uid, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onKickedCallback) {
    }
    
    public final void stopSessionGuard() {
    }
}