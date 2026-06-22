package com.conzchat.app.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u00107\u001a\u00020\u00042\u0006\u00108\u001a\u00020\u0012J\u000e\u00109\u001a\u00020\u00042\u0006\u00108\u001a\u00020\u0012J\u000e\u0010:\u001a\u00020;2\u0006\u0010<\u001a\u00020\u0012R\u0011\u0010\u0003\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0011\u0010\u0007\u001a\u00020\b8F\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0011\u0010\u000b\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\u0006R\u0011\u0010\r\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u000e\u0010\u0006R\u0011\u0010\u000f\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0006R\u0011\u0010\u0011\u001a\u00020\u00128F\u00a2\u0006\u0006\u001a\u0004\b\u0013\u0010\u0014R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u00168F\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0019\u001a\u00020\u001a8F\u00a2\u0006\u0006\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u001d\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\u001e\u0010\u0006R\u0011\u0010\u001f\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b \u0010\u0006R\u0011\u0010!\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b\"\u0010\u0006R\u0011\u0010#\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b$\u0010\u0006R\u0011\u0010%\u001a\u00020&8F\u00a2\u0006\u0006\u001a\u0004\b\'\u0010(R\u0011\u0010)\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b*\u0010\u0006R\u0011\u0010+\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b,\u0010\u0006R\u0011\u0010-\u001a\u00020.8F\u00a2\u0006\u0006\u001a\u0004\b/\u00100R\u0011\u00101\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b2\u0010\u0006R\u0011\u00103\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b4\u0010\u0006R\u0011\u00105\u001a\u00020\u00048F\u00a2\u0006\u0006\u001a\u0004\b6\u0010\u0006\u00a8\u0006="}, d2 = {"Lcom/conzchat/app/util/FirebaseManager;", "", "()V", "appConfigRef", "Lcom/google/firebase/firestore/CollectionReference;", "getAppConfigRef", "()Lcom/google/firebase/firestore/CollectionReference;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "getAuth", "()Lcom/google/firebase/auth/FirebaseAuth;", "callsRef", "getCallsRef", "chatSettingsRef", "getChatSettingsRef", "conzAIChatsRef", "getConzAIChatsRef", "currentUid", "", "getCurrentUid", "()Ljava/lang/String;", "currentUser", "Lcom/google/firebase/auth/FirebaseUser;", "getCurrentUser", "()Lcom/google/firebase/auth/FirebaseUser;", "db", "Lcom/google/firebase/firestore/FirebaseFirestore;", "getDb", "()Lcom/google/firebase/firestore/FirebaseFirestore;", "dmTypingRef", "getDmTypingRef", "friendRequestsRef", "getFriendRequestsRef", "groupsRef", "getGroupsRef", "messagesRef", "getMessagesRef", "messaging", "Lcom/google/firebase/messaging/FirebaseMessaging;", "getMessaging", "()Lcom/google/firebase/messaging/FirebaseMessaging;", "publicGroupsRef", "getPublicGroupsRef", "sessionsRef", "getSessionsRef", "storage", "Lcom/google/firebase/storage/FirebaseStorage;", "getStorage", "()Lcom/google/firebase/storage/FirebaseStorage;", "storiesRef", "getStoriesRef", "suggestionsRef", "getSuggestionsRef", "usersRef", "getUsersRef", "groupMessagesRef", "groupId", "publicGroupMessagesRef", "updateFcmToken", "", "token", "app_originalRelease"})
public final class FirebaseManager {
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.FirebaseManager INSTANCE = null;
    
    private FirebaseManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.auth.FirebaseAuth getAuth() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.FirebaseFirestore getDb() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.storage.FirebaseStorage getStorage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.messaging.FirebaseMessaging getMessaging() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.auth.FirebaseUser getCurrentUser() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCurrentUid() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getUsersRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getMessagesRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference groupMessagesRef(@org.jetbrains.annotations.NotNull()
    java.lang.String groupId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference publicGroupMessagesRef(@org.jetbrains.annotations.NotNull()
    java.lang.String groupId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getGroupsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getPublicGroupsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getStoriesRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getCallsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getSuggestionsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getFriendRequestsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getDmTypingRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getSessionsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getConzAIChatsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getChatSettingsRef() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.firestore.CollectionReference getAppConfigRef() {
        return null;
    }
    
    public final void updateFcmToken(@org.jetbrains.annotations.NotNull()
    java.lang.String token) {
    }
}