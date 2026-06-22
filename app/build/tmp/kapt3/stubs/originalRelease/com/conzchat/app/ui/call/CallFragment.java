package com.conzchat.app.ui.call;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\u0018\u0000 :2\u00020\u0001:\u0001:B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010 \u001a\u00020!H\u0002J\b\u0010\"\u001a\u00020!H\u0002J\b\u0010#\u001a\u00020!H\u0002J\b\u0010$\u001a\u00020!H\u0002J\b\u0010%\u001a\u00020!H\u0002J$\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020)2\b\u0010*\u001a\u0004\u0018\u00010+2\b\u0010,\u001a\u0004\u0018\u00010-H\u0016J\b\u0010.\u001a\u00020!H\u0016J\u001a\u0010/\u001a\u00020!2\u0006\u00100\u001a\u00020\'2\b\u0010,\u001a\u0004\u0018\u00010-H\u0016J\b\u00101\u001a\u00020!H\u0002J\b\u00102\u001a\u00020!H\u0002J\b\u00103\u001a\u00020!H\u0002J\b\u00104\u001a\u00020!H\u0002J\b\u00105\u001a\u00020!H\u0002J\b\u00106\u001a\u00020!H\u0002J\b\u00107\u001a\u00020!H\u0002J\b\u00108\u001a\u00020!H\u0002J\b\u00109\u001a\u00020!H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001d\u001a\u00020\t8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u001e\u0010\u001f\u00a8\u0006;"}, d2 = {"Lcom/conzchat/app/ui/call/CallFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/FragmentCallBinding;", "binding", "getBinding", "()Lcom/conzchat/app/databinding/FragmentCallBinding;", "callId", "", "callListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "callSeconds", "", "callStatus", "callType", "handler", "Landroid/os/Handler;", "isIncoming", "", "isMuted", "isSpeaker", "ringtonePlayer", "Landroid/media/MediaPlayer;", "timerRunnable", "Ljava/lang/Runnable;", "toName", "toPhoto", "toUid", "uid", "getUid", "()Ljava/lang/String;", "acceptCall", "", "declineCall", "endCall", "initiateCall", "listenForCallChanges", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "playRingtone", "setupUI", "showActiveUI", "showIncomingUI", "startTimer", "stopRingtone", "stopTimer", "toggleMute", "toggleSpeaker", "Companion", "app_originalRelease"})
public final class CallFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.FragmentCallBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String toUid = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String toName = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String toPhoto = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String callType = "voice";
    private boolean isIncoming = false;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String callId = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String callStatus = "ringing";
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.firestore.ListenerRegistration callListener;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaPlayer ringtonePlayer;
    private int callSeconds = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Runnable timerRunnable;
    private boolean isMuted = false;
    private boolean isSpeaker = false;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.call.CallFragment.Companion Companion = null;
    
    public CallFragment() {
        super();
    }
    
    private final com.conzchat.app.databinding.FragmentCallBinding getBinding() {
        return null;
    }
    
    private final java.lang.String getUid() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupUI() {
    }
    
    private final void showIncomingUI() {
    }
    
    private final void showActiveUI() {
    }
    
    private final void initiateCall() {
    }
    
    private final void acceptCall() {
    }
    
    private final void declineCall() {
    }
    
    private final void endCall() {
    }
    
    private final void listenForCallChanges() {
    }
    
    private final void playRingtone() {
    }
    
    private final void stopRingtone() {
    }
    
    private final void startTimer() {
    }
    
    private final void stopTimer() {
    }
    
    private final void toggleMute() {
    }
    
    private final void toggleSpeaker() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J6\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0006\u00a8\u0006\r"}, d2 = {"Lcom/conzchat/app/ui/call/CallFragment$Companion;", "", "()V", "newInstance", "Lcom/conzchat/app/ui/call/CallFragment;", "toUid", "", "toName", "toPhoto", "callType", "isIncoming", "", "callId", "app_originalRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.call.CallFragment newInstance(@org.jetbrains.annotations.NotNull()
        java.lang.String toUid, @org.jetbrains.annotations.NotNull()
        java.lang.String toName, @org.jetbrains.annotations.NotNull()
        java.lang.String toPhoto, @org.jetbrains.annotations.NotNull()
        java.lang.String callType, boolean isIncoming, @org.jetbrains.annotations.NotNull()
        java.lang.String callId) {
            return null;
        }
    }
}