package com.conzchat.app.ui.chat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001&B\u00a5\u0001\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0018\u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\t\u0012\u0012\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\n0\f\u0012\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\f\u0012\u0012\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\f\u0012\u001e\u0010\u000f\u001a\u001a\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\u0010\u0012\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\f\u00a2\u0006\u0002\u0010\u0012J\b\u0010\u0017\u001a\u00020\u0018H\u0016J\u001c\u0010\u0019\u001a\u00020\n2\n\u0010\u001a\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u001b\u001a\u00020\u0018H\u0016J\u001c\u0010\u001c\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u001d\u001a\u00020\u001e2\u0006\u0010\u001f\u001a\u00020\u0018H\u0016J \u0010 \u001a\u00020\n2\u0006\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020\u00052\u0006\u0010$\u001a\u00020%H\u0002R\u001d\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00070\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000e\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\b\u001a\u0014\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\u0005\u0012\u0004\u0012\u00020\n0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R&\u0010\u000f\u001a\u001a\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\n0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/conzchat/app/ui/chat/MessageAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/conzchat/app/ui/chat/MessageAdapter$MessageViewHolder;", "messages", "", "Lcom/conzchat/app/model/Message;", "myUid", "", "onReaction", "Lkotlin/Function2;", "", "onReply", "Lkotlin/Function1;", "onDelete", "onImageClick", "onViewOnce", "Lkotlin/Function3;", "onProfileClick", "(Ljava/util/List;Ljava/lang/String;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function3;Lkotlin/jvm/functions/Function1;)V", "avatarCache", "", "getAvatarCache", "()Ljava/util/Map;", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "showActionSheet", "anchor", "Landroid/view/View;", "msg", "isMine", "", "MessageViewHolder", "app_originalRelease"})
public final class MessageAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.conzchat.app.ui.chat.MessageAdapter.MessageViewHolder> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.model.Message> messages = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String myUid = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function2<java.lang.String, java.lang.String, kotlin.Unit> onReaction = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<com.conzchat.app.model.Message, kotlin.Unit> onReply = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onDelete = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onImageClick = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function3<java.lang.String, java.lang.String, java.lang.String, kotlin.Unit> onViewOnce = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<java.lang.String, kotlin.Unit> onProfileClick = null;
    
    /**
     * Populated by ChatFragment after fetching the other user's profile
     */
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.String> avatarCache = null;
    
    public MessageAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.conzchat.app.model.Message> messages, @org.jetbrains.annotations.NotNull()
    java.lang.String myUid, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onReaction, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.conzchat.app.model.Message, kotlin.Unit> onReply, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onDelete, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onImageClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function3<? super java.lang.String, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onViewOnce, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onProfileClick) {
        super();
    }
    
    /**
     * Populated by ChatFragment after fetching the other user's profile
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.Map<java.lang.String, java.lang.String> getAvatarCache() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.conzchat.app.ui.chat.MessageAdapter.MessageViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.conzchat.app.ui.chat.MessageAdapter.MessageViewHolder holder, int position) {
    }
    
    private final void showActionSheet(android.view.View anchor, com.conzchat.app.model.Message msg, boolean isMine) {
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\r\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0011\u0010\u0011\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010R\u0011\u0010\u0013\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0010R\u0011\u0010\u0015\u001a\u00020\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010R\u0011\u0010\u0017\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\u001a\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0011\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010 \u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001fR\u0011\u0010\"\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001fR\u0011\u0010$\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001fR\u0011\u0010&\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001fR\u0011\u0010(\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001fR\u0011\u0010*\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001fR\u0011\u0010,\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001fR\u0011\u0010.\u001a\u00020/\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u00101\u00a8\u00062"}, d2 = {"Lcom/conzchat/app/ui/chat/MessageAdapter$MessageViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "view", "Landroid/view/View;", "(Lcom/conzchat/app/ui/chat/MessageAdapter;Landroid/view/View;)V", "ivAvatar", "Lde/hdodenhof/circleimageview/CircleImageView;", "getIvAvatar", "()Lde/hdodenhof/circleimageview/CircleImageView;", "ivImage", "Landroid/widget/ImageView;", "getIvImage", "()Landroid/widget/ImageView;", "llBubble", "Landroid/widget/LinearLayout;", "getLlBubble", "()Landroid/widget/LinearLayout;", "llReactions", "getLlReactions", "llRow", "getLlRow", "llVoice", "getLlVoice", "spacerEnd", "getSpacerEnd", "()Landroid/view/View;", "spacerStart", "getSpacerStart", "tvCameraLabel", "Landroid/widget/TextView;", "getTvCameraLabel", "()Landroid/widget/TextView;", "tvMeta", "getTvMeta", "tvReceipt", "getTvReceipt", "tvReplyPreview", "getTvReplyPreview", "tvText", "getTvText", "tvTranscript", "getTvTranscript", "tvViewOnce", "getTvViewOnce", "tvVoiceLabel", "getTvVoiceLabel", "videoView", "Landroid/widget/VideoView;", "getVideoView", "()Landroid/widget/VideoView;", "app_originalRelease"})
    public final class MessageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final android.widget.LinearLayout llRow = null;
        @org.jetbrains.annotations.NotNull()
        private final de.hdodenhof.circleimageview.CircleImageView ivAvatar = null;
        @org.jetbrains.annotations.NotNull()
        private final android.view.View spacerStart = null;
        @org.jetbrains.annotations.NotNull()
        private final android.view.View spacerEnd = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.LinearLayout llBubble = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvReplyPreview = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvText = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.ImageView ivImage = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.VideoView videoView = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.LinearLayout llVoice = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvVoiceLabel = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvTranscript = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvViewOnce = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvMeta = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvReceipt = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.LinearLayout llReactions = null;
        @org.jetbrains.annotations.NotNull()
        private final android.widget.TextView tvCameraLabel = null;
        
        public MessageViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.View view) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.LinearLayout getLlRow() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final de.hdodenhof.circleimageview.CircleImageView getIvAvatar() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.view.View getSpacerStart() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.view.View getSpacerEnd() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.LinearLayout getLlBubble() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvReplyPreview() {
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
        public final android.widget.VideoView getVideoView() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.LinearLayout getLlVoice() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvVoiceLabel() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvTranscript() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvViewOnce() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvMeta() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvReceipt() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.LinearLayout getLlReactions() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.widget.TextView getTvCameraLabel() {
            return null;
        }
    }
}