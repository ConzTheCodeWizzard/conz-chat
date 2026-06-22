package com.conzchat.app.ui.groups;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0015B/\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0012\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\u0002\u0010\u000bJ\b\u0010\f\u001a\u00020\rH\u0016J\u001c\u0010\u000e\u001a\u00020\n2\n\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\rH\u0016J\u001c\u0010\u0011\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\rH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/conzchat/app/ui/groups/GroupMessageAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/conzchat/app/ui/groups/GroupMessageAdapter$VH;", "messages", "", "Lcom/conzchat/app/model/GroupMessage;", "myUid", "", "onProfileClick", "Lkotlin/Function1;", "", "(Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)V", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "VH", "app_originalRelease"})
public final class GroupMessageAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.conzchat.app.ui.groups.GroupMessageAdapter.VH> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.model.GroupMessage> messages = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String myUid = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onProfileClick = null;
    
    public GroupMessageAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.conzchat.app.model.GroupMessage> messages, @org.jetbrains.annotations.NotNull()
    java.lang.String myUid, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onProfileClick) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.conzchat.app.ui.groups.GroupMessageAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.conzchat.app.ui.groups.GroupMessageAdapter.VH holder, int position) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\bR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0013\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0012R\u0011\u0010\u0015\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0012\u00a8\u0006\u0017"}, d2 = {"Lcom/conzchat/app/ui/groups/GroupMessageAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "view", "Landroid/view/View;", "(Lcom/conzchat/app/ui/groups/GroupMessageAdapter;Landroid/view/View;)V", "ivAvatar", "Landroid/widget/ImageView;", "getIvAvatar", "()Landroid/widget/ImageView;", "ivImage", "getIvImage", "llWrap", "Landroid/widget/LinearLayout;", "getLlWrap", "()Landroid/widget/LinearLayout;", "tvSenderName", "Landroid/widget/TextView;", "getTvSenderName", "()Landroid/widget/TextView;", "tvText", "getTvText", "tvTime", "getTvTime", "app_originalRelease"})
    public final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.LinearLayout llWrap = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageView ivAvatar = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvSenderName = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvText = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageView ivImage = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTime = null;
        
        public VH(@org.jetbrains.annotations.NotNull()
        android.view.View view) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.LinearLayout getLlWrap() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.ImageView getIvAvatar() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvSenderName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvText() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.ImageView getIvImage() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvTime() {
            return null;
        }
    }
}