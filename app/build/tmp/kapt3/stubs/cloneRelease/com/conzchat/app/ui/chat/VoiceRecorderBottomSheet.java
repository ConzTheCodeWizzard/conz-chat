package com.conzchat.app.ui.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\u0018\u0000 &2\u00020\u0001:\u0001&B\u0005\u00a2\u0006\u0002\u0010\u0002J$\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020!H\u0016J\u001a\u0010\"\u001a\u00020!2\u0006\u0010#\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010$\u001a\u00020!H\u0002J\b\u0010%\u001a\u00020!H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\u00020\u00158BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006\'"}, d2 = {"Lcom/conzchat/app/ui/chat/VoiceRecorderBottomSheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/BottomSheetVoiceRecorderBinding;", "audioFile", "Ljava/io/File;", "binding", "getBinding", "()Lcom/conzchat/app/databinding/BottomSheetVoiceRecorderBinding;", "handler", "Landroid/os/Handler;", "isRecording", "", "recorder", "Landroid/media/MediaRecorder;", "seconds", "", "timerRunnable", "Ljava/lang/Runnable;", "uid", "", "getUid", "()Ljava/lang/String;", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "", "onViewCreated", "view", "startRecording", "stopRecording", "Companion", "app_cloneRelease"})
public final class VoiceRecorderBottomSheet extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.BottomSheetVoiceRecorderBinding _binding;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaRecorder recorder;
    @org.jetbrains.annotations.Nullable()
    private java.io.File audioFile;
    private boolean isRecording = false;
    private int seconds = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Runnable timerRunnable;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.chat.VoiceRecorderBottomSheet.Companion Companion = null;
    
    public VoiceRecorderBottomSheet() {
        super();
    }
    
    private final com.conzchat.app.databinding.BottomSheetVoiceRecorderBinding getBinding() {
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
    
    private final void startRecording() {
    }
    
    private final void stopRecording() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/conzchat/app/ui/chat/VoiceRecorderBottomSheet$Companion;", "", "()V", "newInstance", "Lcom/conzchat/app/ui/chat/VoiceRecorderBottomSheet;", "toUid", "", "app_cloneRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.chat.VoiceRecorderBottomSheet newInstance(@org.jetbrains.annotations.NotNull()
        java.lang.String toUid) {
            return null;
        }
    }
}