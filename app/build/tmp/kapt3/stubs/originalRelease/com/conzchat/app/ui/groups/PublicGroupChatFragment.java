package com.conzchat.app.ui.groups;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 %2\u00020\u0001:\u0001%B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0016\u001a\u00020\u0017H\u0002J$\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010 \u001a\u00020\u0017H\u0016J\u001a\u0010!\u001a\u00020\u00172\u0006\u0010\"\u001a\u00020\u00192\b\u0010\u001e\u001a\u0004\u0018\u00010\u001fH\u0016J\b\u0010#\u001a\u00020\u0017H\u0002J\b\u0010$\u001a\u00020\u0017H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\tR\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\u00020\u000b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006&"}, d2 = {"Lcom/conzchat/app/ui/groups/PublicGroupChatFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/FragmentGroupChatBinding;", "adapter", "Lcom/conzchat/app/ui/groups/GroupMessageAdapter;", "binding", "getBinding", "()Lcom/conzchat/app/databinding/FragmentGroupChatBinding;", "groupId", "", "groupName", "groupTag", "messages", "", "Lcom/conzchat/app/model/GroupMessage;", "messagesListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "uid", "getUid", "()Ljava/lang/String;", "loadMessages", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "openGroupInfo", "sendText", "Companion", "app_originalRelease"})
public final class PublicGroupChatFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.FragmentGroupChatBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String groupId = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String groupName = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String groupTag = "";
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.model.GroupMessage> messages = null;
    private com.conzchat.app.ui.groups.GroupMessageAdapter adapter;
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.firestore.ListenerRegistration messagesListener;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.groups.PublicGroupChatFragment.Companion Companion = null;
    
    public PublicGroupChatFragment() {
        super();
    }
    
    private final com.conzchat.app.databinding.FragmentGroupChatBinding getBinding() {
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
    
    private final void loadMessages() {
    }
    
    private final void sendText() {
    }
    
    private final void openGroupInfo() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J&\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0006\u00a8\u0006\n"}, d2 = {"Lcom/conzchat/app/ui/groups/PublicGroupChatFragment$Companion;", "", "()V", "newInstance", "Lcom/conzchat/app/ui/groups/PublicGroupChatFragment;", "groupId", "", "name", "photo", "tag", "app_originalRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.groups.PublicGroupChatFragment newInstance(@org.jetbrains.annotations.NotNull()
        java.lang.String groupId, @org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String photo, @org.jetbrains.annotations.NotNull()
        java.lang.String tag) {
            return null;
        }
    }
}