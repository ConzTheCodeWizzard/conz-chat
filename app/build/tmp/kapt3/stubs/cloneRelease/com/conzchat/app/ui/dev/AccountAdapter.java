package com.conzchat.app.ui.dev;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\f\u0012\b\u0012\u00060\u0002R\u00020\u00000\u0001:\u0001\u0013B\u0013\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\b\u0010\t\u001a\u00020\nH\u0016J\u001c\u0010\u000b\u001a\u00020\f2\n\u0010\r\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u000e\u001a\u00020\nH\u0016J\u001c\u0010\u000f\u001a\u00060\u0002R\u00020\u00002\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\nH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/conzchat/app/ui/dev/AccountAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Lcom/conzchat/app/ui/dev/AccountAdapter$VH;", "accounts", "", "Lcom/conzchat/app/model/CollectedAccountEntry;", "(Ljava/util/List;)V", "dateFormat", "Ljava/text/SimpleDateFormat;", "getItemCount", "", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "VH", "app_cloneRelease"})
public final class AccountAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<com.conzchat.app.ui.dev.AccountAdapter.VH> {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.conzchat.app.model.CollectedAccountEntry> accounts = null;
    @org.jetbrains.annotations.NotNull()
    private final java.text.SimpleDateFormat dateFormat = null;
    
    public AccountAdapter(@org.jetbrains.annotations.NotNull()
    java.util.List<com.conzchat.app.model.CollectedAccountEntry> accounts) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.conzchat.app.ui.dev.AccountAdapter.VH onCreateViewHolder(@org.jetbrains.annotations.NotNull()
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override()
    public int getItemCount() {
        return 0;
    }
    
    @java.lang.Override()
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull()
    com.conzchat.app.ui.dev.AccountAdapter.VH holder, int position) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/conzchat/app/ui/dev/AccountAdapter$VH;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "binding", "Lcom/conzchat/app/databinding/ItemCollectedAccountBinding;", "(Lcom/conzchat/app/ui/dev/AccountAdapter;Lcom/conzchat/app/databinding/ItemCollectedAccountBinding;)V", "getBinding", "()Lcom/conzchat/app/databinding/ItemCollectedAccountBinding;", "app_cloneRelease"})
    public final class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull()
        private final com.conzchat.app.databinding.ItemCollectedAccountBinding binding = null;
        
        public VH(@org.jetbrains.annotations.NotNull()
        com.conzchat.app.databinding.ItemCollectedAccountBinding binding) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.databinding.ItemCollectedAccountBinding getBinding() {
            return null;
        }
    }
}