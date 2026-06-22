package com.conzchat.app;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u0012\u0010\u000f\u001a\u00020\f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0002J\b\u0010\u0012\u001a\u00020\fH\u0016J\u0012\u0010\u0013\u001a\u00020\f2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0014J\u0010\u0010\u0016\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0014J\b\u0010\u0017\u001a\u00020\fH\u0014J\b\u0010\u0018\u001a\u00020\fH\u0014J\u0010\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\u0006\u0010\u001c\u001a\u00020\fJ\b\u0010\u001d\u001a\u00020\fH\u0002J\u0006\u0010\u001e\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0007\u001a\u0010\u0012\f\u0012\n \n*\u0004\u0018\u00010\t0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/conzchat/app/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "authListener", "Lcom/google/firebase/auth/FirebaseAuth$AuthStateListener;", "notifPermLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "applyBrightness", "", "value", "", "handleNotificationIntent", "intent", "Landroid/content/Intent;", "onBackPressed", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onNewIntent", "onStart", "onStop", "replaceFragment", "fragment", "Landroidx/fragment/app/Fragment;", "showHome", "showSessionKickedDialog", "showWelcome", "app_originalRelease"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.google.firebase.auth.FirebaseAuth auth;
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.auth.FirebaseAuth.AuthStateListener authListener;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> notifPermLauncher = null;
    
    public MainActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    protected void onNewIntent(@org.jetbrains.annotations.NotNull()
    android.content.Intent intent) {
    }
    
    private final void handleNotificationIntent(android.content.Intent intent) {
    }
    
    @java.lang.Override()
    protected void onStart() {
    }
    
    @java.lang.Override()
    protected void onStop() {
    }
    
    public final void showWelcome() {
    }
    
    public final void showHome() {
    }
    
    private final void replaceFragment(androidx.fragment.app.Fragment fragment) {
    }
    
    public final void applyBrightness(int value) {
    }
    
    private final void showSessionKickedDialog() {
    }
    
    @java.lang.Override()
    public void onBackPressed() {
    }
}