package com.conzchat.app.ui.feed;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0015B\u001b\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010\u000b\u001a\u00020\fH\u0016J\u001c\u0010\r\u001a\u00020\u000e2\n\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\fH\u0016J\u001c\u0010\u0011\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\fH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/conzchat/app/ui/feed/CommentAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/conzchat/app/ui/feed/CommentAdapter$VH;", "comments", "", "Lcom/conzchat/app/model/FeedComment;", "ctx", "Landroid/content/Context;", "(Ljava/util/List;Landroid/content/Context;)V", "dateFormat", "Ljava/text/SimpleDateFormat;", "getItemCount", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "VH", "app_originalRelease"})
public final class CommentAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.conzchat.app.ui.feed.CommentAdapter.VH> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.model.FeedComment> comments = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.NotNull()
    private final java.text.SimpleDateFormat dateFormat = null;
    
    public CommentAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.conzchat.app.model.FeedComment> comments, @org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.conzchat.app.ui.feed.CommentAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.conzchat.app.ui.feed.CommentAdapter.VH holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/conzchat/app/ui/feed/CommentAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/conzchat/app/databinding/ItemCommentBinding;", "(Lcom/conzchat/app/ui/feed/CommentAdapter;Lcom/conzchat/app/databinding/ItemCommentBinding;)V", "getBinding", "()Lcom/conzchat/app/databinding/ItemCommentBinding;", "app_originalRelease"})
    public final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.conzchat.app.databinding.ItemCommentBinding binding = null;
        
        public VH(@org.jetbrains.annotations.NotNull()
        com.conzchat.app.databinding.ItemCommentBinding binding) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.databinding.ItemCommentBinding getBinding() {
            return null;
        }
    }
}