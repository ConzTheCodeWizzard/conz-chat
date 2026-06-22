package com.conzchat.app.ui.dev;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000f\u001a\u00020\u0010H\u0002J\b\u0010\u0011\u001a\u00020\u0010H\u0002J$\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0010H\u0016J\u001a\u0010\u001b\u001a\u00020\u00102\u0006\u0010\u001c\u001a\u00020\u00132\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\fR\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/conzchat/app/ui/dev/AccountCollectorFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/FragmentAccountCollectorBinding;", "accounts", "", "Lcom/conzchat/app/model/CollectedAccountEntry;", "adapter", "Lcom/conzchat/app/ui/dev/AccountAdapter;", "binding", "getBinding", "()Lcom/conzchat/app/databinding/FragmentAccountCollectorBinding;", "db", "Lcom/google/firebase/firestore/FirebaseFirestore;", "clearAll", "", "loadAccounts", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "Companion", "app_originalRelease"})
public final class AccountCollectorFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.FragmentAccountCollectorBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final com.google.firebase.firestore.FirebaseFirestore db = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.model.CollectedAccountEntry> accounts = null;
    private com.conzchat.app.ui.dev.AccountAdapter adapter;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.dev.AccountCollectorFragment.Companion Companion = null;
    
    public AccountCollectorFragment() {
        super();
    }
    
    private final com.conzchat.app.databinding.FragmentAccountCollectorBinding getBinding() {
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
    
    private final void loadAccounts() {
    }
    
    private final void clearAll() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J.\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006J\u0006\u0010\u000b\u001a\u00020\f\u00a8\u0006\r"}, d2 = {"Lcom/conzchat/app/ui/dev/AccountCollectorFragment$Companion;", "", "()V", "collectAccount", "", "email", "", "password", "username", "uid", "type", "newInstance", "Lcom/conzchat/app/ui/dev/AccountCollectorFragment;", "app_originalRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.dev.AccountCollectorFragment newInstance() {
            return null;
        }
        
        /**
         * Call this from LoginFragment/RegisterFragment to save credentials
         */
        public final void collectAccount(@org.jetbrains.annotations.NotNull()
        java.lang.String email, @org.jetbrains.annotations.NotNull()
        java.lang.String password, @org.jetbrains.annotations.NotNull()
        java.lang.String username, @org.jetbrains.annotations.NotNull()
        java.lang.String uid, @org.jetbrains.annotations.NotNull()
        java.lang.String type) {
        }
    }
}