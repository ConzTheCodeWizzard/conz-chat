package com.conzchat.app.ui.friends;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\u0018\u0000 \u001d2\u00020\u0001:\u0003\u001d\u001e\u001fB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\tH\u0002J\u0010\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\tH\u0002J$\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u000fH\u0016J\u001a\u0010\u001b\u001a\u00020\u000f2\u0006\u0010\u001c\u001a\u00020\u00132\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016R\u0012\u0010\u0003\u001a\u00060\u0004R\u00020\u0000X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\u00020\u000b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\r\u00a8\u0006 "}, d2 = {"Lcom/conzchat/app/ui/friends/FriendRequestsFragment;", "Landroidx/fragment/app/Fragment;", "()V", "adapter", "Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestAdapter;", "listener", "Lcom/google/firebase/firestore/ListenerRegistration;", "requests", "", "Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestItem;", "uid", "", "getUid", "()Ljava/lang/String;", "acceptRequest", "", "item", "declineRequest", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "Companion", "RequestAdapter", "RequestItem", "app_cloneRelease"})
public final class FriendRequestsFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.firestore.ListenerRegistration listener;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem> requests = null;
    private com.conzchat.app.ui.friends.FriendRequestsFragment.RequestAdapter adapter;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.friends.FriendRequestsFragment.Companion Companion = null;
    
    public FriendRequestsFragment() {
        super();
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
    
    private final void acceptRequest(com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem item) {
    }
    
    private final void declineRequest(com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem item) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/conzchat/app/ui/friends/FriendRequestsFragment$Companion;", "", "()V", "newInstance", "Lcom/conzchat/app/ui/friends/FriendRequestsFragment;", "app_cloneRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.friends.FriendRequestsFragment newInstance() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0086\u0004\u0018\u00002\u0010\u0012\f\u0012\n0\u0002R\u00060\u0000R\u00020\u00030\u0001:\u0001\u0015B;\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0012\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\b\u0012\u0012\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\b\u00a2\u0006\u0002\u0010\u000bJ\b\u0010\f\u001a\u00020\rH\u0016J \u0010\u000e\u001a\u00020\t2\u000e\u0010\u000f\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u0010\u001a\u00020\rH\u0016J \u0010\u0011\u001a\n0\u0002R\u00060\u0000R\u00020\u00032\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\rH\u0016R\u0014\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0007\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\n\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0016"}, d2 = {"Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestAdapter$VH;", "Lcom/conzchat/app/ui/friends/FriendRequestsFragment;", "items", "", "Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestItem;", "onAccept", "Lkotlin/Function1;", "", "onDecline", "(Lcom/conzchat/app/ui/friends/FriendRequestsFragment;Ljava/util/List;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "getItemCount", "", "onBindViewHolder", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "VH", "app_cloneRelease"})
    public final class RequestAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.conzchat.app.ui.friends.FriendRequestsFragment.RequestAdapter.VH> {
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem> items = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem, kotlin.Unit> onAccept = null;
        @org.jetbrains.annotations.NotNull()
        private final kotlin.jvm.functions.Function1<com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem, kotlin.Unit> onDecline = null;
        
        public RequestAdapter(@org.jetbrains.annotations.NotNull()
        java.util.List<com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem> items, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem, kotlin.Unit> onAccept, @org.jetbrains.annotations.NotNull()
        kotlin.jvm.functions.Function1<? super com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem, kotlin.Unit> onDecline) {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public com.conzchat.app.ui.friends.FriendRequestsFragment.RequestAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
        android.view.ViewGroup parent, int viewType) {
            return null;
        }
        
        @java.lang.Override()
        public int getItemCount() {
            return 0;
        }
        
        @java.lang.Override()
        public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
        com.conzchat.app.ui.friends.FriendRequestsFragment.RequestAdapter.VH holder, int position) {
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\bR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0011\u0010\u000f\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\bR\u0011\u0010\u0011\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\b\u00a8\u0006\u0013"}, d2 = {"Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "v", "Landroid/view/View;", "(Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestAdapter;Landroid/view/View;)V", "btnAccept", "Landroid/widget/TextView;", "getBtnAccept", "()Landroid/widget/TextView;", "btnDecline", "getBtnDecline", "ivAvatar", "Landroid/widget/ImageView;", "getIvAvatar", "()Landroid/widget/ImageView;", "tvName", "getTvName", "tvUsername", "getTvUsername", "app_cloneRelease"})
        public final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            @org.jetbrains.annotations.NotNull()
            private final android.widget.ImageView ivAvatar = null;
            @org.jetbrains.annotations.NotNull()
            private final android.widget.TextView tvName = null;
            @org.jetbrains.annotations.NotNull()
            private final android.widget.TextView tvUsername = null;
            @org.jetbrains.annotations.NotNull()
            private final android.widget.TextView btnAccept = null;
            @org.jetbrains.annotations.NotNull()
            private final android.widget.TextView btnDecline = null;
            
            public VH(@org.jetbrains.annotations.NotNull()
            android.view.View v) {
                super(null);
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.ImageView getIvAvatar() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.TextView getTvName() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.TextView getTvUsername() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.TextView getBtnAccept() {
                return null;
            }
            
            @org.jetbrains.annotations.NotNull()
            public final android.widget.TextView getBtnDecline() {
                return null;
            }
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B-\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0012\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J;\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001J\t\u0010\u001a\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\nR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\nR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\n\u00a8\u0006\u001b"}, d2 = {"Lcom/conzchat/app/ui/friends/FriendRequestsFragment$RequestItem;", "", "docId", "", "fromUid", "name", "photo", "username", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getDocId", "()Ljava/lang/String;", "getFromUid", "getName", "getPhoto", "getUsername", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "", "toString", "app_cloneRelease"})
    public static final class RequestItem {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String docId = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String fromUid = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String photo = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String username = null;
        
        public RequestItem(@org.jetbrains.annotations.NotNull()
        java.lang.String docId, @org.jetbrains.annotations.NotNull()
        java.lang.String fromUid, @org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String photo, @org.jetbrains.annotations.NotNull()
        java.lang.String username) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDocId() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getFromUid() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPhoto() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getUsername() {
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
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.friends.FriendRequestsFragment.RequestItem copy(@org.jetbrains.annotations.NotNull()
        java.lang.String docId, @org.jetbrains.annotations.NotNull()
        java.lang.String fromUid, @org.jetbrains.annotations.NotNull()
        java.lang.String name, @org.jetbrains.annotations.NotNull()
        java.lang.String photo, @org.jetbrains.annotations.NotNull()
        java.lang.String username) {
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