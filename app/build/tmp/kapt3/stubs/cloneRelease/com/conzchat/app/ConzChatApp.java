package com.conzchat.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \u00062\u00020\u0001:\u0001\u0006B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0002J\b\u0010\u0005\u001a\u00020\u0004H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/conzchat/app/ConzChatApp;", "Landroid/app/Application;", "()V", "createNotificationChannels", "", "onCreate", "Companion", "app_cloneRelease"})
public final class ConzChatApp extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_MESSAGES = "conzchat_messages";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_CALLS = "conzchat_calls";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_GROUPS = "conzchat_groups";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DEV_UID = "GAEtvdjvwla73GscQWnGthTPG6f1";
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ConzChatApp.Companion Companion = null;
    
    public ConzChatApp() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void createNotificationChannels() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/conzchat/app/ConzChatApp$Companion;", "", "()V", "CHANNEL_CALLS", "", "CHANNEL_GROUPS", "CHANNEL_MESSAGES", "DEV_UID", "app_cloneRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}