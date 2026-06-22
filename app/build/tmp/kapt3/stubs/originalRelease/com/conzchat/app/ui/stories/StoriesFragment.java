package com.conzchat.app.ui.stories;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 22\u00020\u0001:\u00012B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001a\u001a\u00020\u001bH\u0002J\b\u0010\u001c\u001a\u00020\u001bH\u0002J\u0010\u0010\u001d\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\fH\u0002J$\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010$2\b\u0010%\u001a\u0004\u0018\u00010&H\u0016J\b\u0010\'\u001a\u00020\u001bH\u0016J\u001a\u0010(\u001a\u00020\u001b2\u0006\u0010)\u001a\u00020 2\b\u0010%\u001a\u0004\u0018\u00010&H\u0016J\u0010\u0010*\u001a\u00020\u001b2\u0006\u0010+\u001a\u00020,H\u0002J\b\u0010-\u001a\u00020\u001bH\u0002J\b\u0010.\u001a\u00020\u001bH\u0002J\b\u0010/\u001a\u00020\u001bH\u0002J\b\u00100\u001a\u00020\u001bH\u0002J\b\u00101\u001a\u00020\u001bH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\n\u001a\u0010\u0012\f\u0012\n \r*\u0004\u0018\u00010\f0\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0010\u001a\u0004\u0018\u00010\u0011X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\f0\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\u00020\f8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0017\u0010\u0018R\u0010\u0010\u0019\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00063"}, d2 = {"Lcom/conzchat/app/ui/stories/StoriesFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/FragmentStoriesBinding;", "binding", "getBinding", "()Lcom/conzchat/app/databinding/FragmentStoriesBinding;", "currentIndex", "", "galleryLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "handler", "Landroid/os/Handler;", "progressRunnable", "Ljava/lang/Runnable;", "storyListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "storyUrls", "", "uid", "getUid", "()Ljava/lang/String;", "viewUid", "advanceStory", "", "loadMyStory", "loadUserStory", "targetUid", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "postStory", "uri", "Landroid/net/Uri;", "previousStory", "showEmpty", "showStory", "startAutoAdvance", "stopAutoAdvance", "Companion", "app_originalRelease"})
public final class StoriesFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.FragmentStoriesBinding _binding;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String viewUid;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<java.lang.String> storyUrls;
    private int currentIndex = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Runnable progressRunnable;
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.firestore.ListenerRegistration storyListener;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> galleryLauncher = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.stories.StoriesFragment.Companion Companion = null;
    
    public StoriesFragment() {
        super();
    }
    
    private final com.conzchat.app.databinding.FragmentStoriesBinding getBinding() {
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
    
    private final void loadMyStory() {
    }
    
    private final void loadUserStory(java.lang.String targetUid) {
    }
    
    private final void showStory() {
    }
    
    private final void showEmpty() {
    }
    
    private final void startAutoAdvance() {
    }
    
    private final void stopAutoAdvance() {
    }
    
    private final void advanceStory() {
    }
    
    private final void previousStory() {
    }
    
    private final void postStory(android.net.Uri uri) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0003\u001a\u00020\u00042\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/conzchat/app/ui/stories/StoriesFragment$Companion;", "", "()V", "newInstance", "Lcom/conzchat/app/ui/stories/StoriesFragment;", "viewUid", "", "app_originalRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.stories.StoriesFragment newInstance(@org.jetbrains.annotations.Nullable()
        java.lang.String viewUid) {
            return null;
        }
    }
}