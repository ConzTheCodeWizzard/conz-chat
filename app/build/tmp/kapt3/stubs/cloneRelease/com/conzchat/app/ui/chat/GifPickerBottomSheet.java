package com.conzchat.app.ui.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u0000 +2\u00020\u0001:\u0003+,-B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0015\u001a\u00020\u00102\u0006\u0010\u0016\u001a\u00020\u000fH\u0002J\b\u0010\u0017\u001a\u00020\u0010H\u0002J$\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020\u0010H\u0016J\u001a\u0010!\u001a\u00020\u00102\u0006\u0010\"\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\u0010\u0010#\u001a\u00020\u00102\u0006\u0010$\u001a\u00020\u000fH\u0002J\u001a\u0010%\u001a\u00020\u00102\u0012\u0010&\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00100\u000eJ\b\u0010\'\u001a\u00020\u0010H\u0002J\u0010\u0010(\u001a\u00020\u00102\u0006\u0010)\u001a\u00020*H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u0012\u0010\b\u001a\u00060\tR\u00020\u0000X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010\r\u001a\u0010\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006."}, d2 = {"Lcom/conzchat/app/ui/chat/GifPickerBottomSheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/BottomSheetGifPickerBinding;", "binding", "getBinding", "()Lcom/conzchat/app/databinding/BottomSheetGifPickerBinding;", "gifAdapter", "Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifAdapter;", "gifItems", "", "Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifItem;", "onGifSelected", "Lkotlin/Function1;", "", "", "scope", "Lkotlinx/coroutines/CoroutineScope;", "searchJob", "Lkotlinx/coroutines/Job;", "fetchGifs", "query", "fetchTrending", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "parseAndDisplay", "json", "setOnGifSelected", "callback", "showError", "showLoading", "loading", "", "Companion", "GifAdapter", "GifItem", "app_cloneRelease"})
public final class GifPickerBottomSheet extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String GIPHY_KEY = "gvnL7xPoArGXv6249XCJl87Hto1qv9wa";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String GIPHY_BASE = "https://api.giphy.com/v1/gifs";
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.BottomSheetGifPickerBinding _binding;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onGifSelected;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.ui.chat.GifPickerBottomSheet.GifItem> gifItems = null;
    private com.conzchat.app.ui.chat.GifPickerBottomSheet.GifAdapter gifAdapter;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job searchJob;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.chat.GifPickerBottomSheet.Companion Companion = null;
    
    public GifPickerBottomSheet() {
        super();
    }
    
    private final com.conzchat.app.databinding.BottomSheetGifPickerBinding getBinding() {
        return null;
    }
    
    public final void setOnGifSelected(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> callback) {
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
    
    private final void fetchTrending() {
    }
    
    private final void fetchGifs(java.lang.String query) {
    }
    
    private final void parseAndDisplay(java.lang.String json) {
    }
    
    private final void showLoading(boolean loading) {
    }
    
    private final void showError() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0006\u001a\u00020\u0007R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$Companion;", "", "()V", "GIPHY_BASE", "", "GIPHY_KEY", "newInstance", "Lcom/conzchat/app/ui/chat/GifPickerBottomSheet;", "app_cloneRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.chat.GifPickerBottomSheet newInstance() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0082\u0004\u0018\u00002\u0010\u0012\f\u0012\n0\u0002R\u00060\u0000R\u00020\u00030\u0001:\u0001\u0014B\'\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\u0002\u0010\nJ\b\u0010\u000b\u001a\u00020\fH\u0016J \u0010\r\u001a\u00020\t2\u000e\u0010\u000e\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u000f\u001a\u00020\fH\u0016J \u0010\u0010\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\fH\u0016R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifAdapter$GifVH;", "Lcom/conzchat/app/ui/chat/GifPickerBottomSheet;", "items", "", "Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifItem;", "onClick", "Lkotlin/Function1;", "", "(Lcom/conzchat/app/ui/chat/GifPickerBottomSheet;Ljava/util/List;Lkotlin/jvm/functions/Function1;)V", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "GifVH", "app_cloneRelease"})
    final class GifAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.conzchat.app.ui.chat.GifPickerBottomSheet.GifAdapter.GifVH> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.conzchat.app.ui.chat.GifPickerBottomSheet.GifItem> items = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.conzchat.app.ui.chat.GifPickerBottomSheet.GifItem, kotlin.Unit> onClick = null;
        
        public GifAdapter(@org.jetbrains.annotations.NotNull()
        java.util.List<com.conzchat.app.ui.chat.GifPickerBottomSheet.GifItem> items, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.conzchat.app.ui.chat.GifPickerBottomSheet.GifItem, kotlin.Unit> onClick) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.conzchat.app.ui.chat.GifPickerBottomSheet.GifAdapter.GifVH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.conzchat.app.ui.chat.GifPickerBottomSheet.GifAdapter.GifVH holder, int position) {
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\b\u00a8\u0006\t"}, d2 = {"Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifAdapter$GifVH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "view", "Landroid/view/View;", "(Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifAdapter;Landroid/view/View;)V", "iv", "Landroid/widget/ImageView;", "getIv", "()Landroid/widget/ImageView;", "app_cloneRelease"})
        public final class GifVH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull()
            private final android.widget.ImageView iv = null;
            
            public GifVH(@org.jetbrains.annotations.NotNull()
            android.view.View view) {
                super(null);
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.ImageView getIv() {
                return null;
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0080\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/conzchat/app/ui/chat/GifPickerBottomSheet$GifItem;", "", "previewUrl", "", "fullUrl", "(Ljava/lang/String;Ljava/lang/String;)V", "getFullUrl", "()Ljava/lang/String;", "getPreviewUrl", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app_cloneRelease"})
    public static final class GifItem {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String previewUrl = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String fullUrl = null;
        
        public GifItem(@org.jetbrains.annotations.NotNull()
        java.lang.String previewUrl, @org.jetbrains.annotations.NotNull()
        java.lang.String fullUrl) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPreviewUrl() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFullUrl() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.chat.GifPickerBottomSheet.GifItem copy(@org.jetbrains.annotations.NotNull()
        java.lang.String previewUrl, @org.jetbrains.annotations.NotNull()
        java.lang.String fullUrl) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}