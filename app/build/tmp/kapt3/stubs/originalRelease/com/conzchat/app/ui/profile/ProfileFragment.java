package com.conzchat.app.ui.profile;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0010$\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 52\u00020\u0001:\u00015B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001c\u001a\u00020\u001dH\u0002J\b\u0010\u001e\u001a\u00020\u001dH\u0002J$\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010$2\b\u0010%\u001a\u0004\u0018\u00010&H\u0016J\b\u0010\'\u001a\u00020\u001dH\u0016J\u001a\u0010(\u001a\u00020\u001d2\u0006\u0010)\u001a\u00020 2\b\u0010%\u001a\u0004\u0018\u00010&H\u0016J\u0010\u0010*\u001a\u00020\u001d2\u0006\u0010+\u001a\u00020\u0001H\u0002J\b\u0010,\u001a\u00020\u001dH\u0002J\b\u0010-\u001a\u00020\u001dH\u0002J\b\u0010.\u001a\u00020\u001dH\u0002J\b\u0010/\u001a\u00020\u001dH\u0002J\b\u00100\u001a\u00020\u001dH\u0002J\u0010\u00101\u001a\u00020\u001d2\u0006\u00102\u001a\u000203H\u0002J\u0010\u00104\u001a\u00020\u001d2\u0006\u00102\u001a\u000203H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0005\u001a\u0010\u0012\f\u0012\n \b*\u0004\u0018\u00010\u00070\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u001c\u0010\f\u001a\u0010\u0012\f\u0012\n \b*\u0004\u0018\u00010\u00070\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\u00020\u000e8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000fR\u0014\u0010\u0010\u001a\u00020\u00078BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0011\u0010\u0012R\u001a\u0010\u0013\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00150\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0018\u001a\u00020\u00078BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001a\u0010\u001b\u001a\u0004\b\u0019\u0010\u0012\u00a8\u00066"}, d2 = {"Lcom/conzchat/app/ui/profile/ProfileFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/conzchat/app/databinding/FragmentProfileBinding;", "avatarPicker", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "binding", "getBinding", "()Lcom/conzchat/app/databinding/FragmentProfileBinding;", "coverPicker", "isMyProfile", "", "()Z", "myUid", "getMyUid", "()Ljava/lang/String;", "profileData", "", "", "profileListener", "Lcom/google/firebase/firestore/ListenerRegistration;", "profileUid", "getProfileUid", "profileUid$delegate", "Lkotlin/Lazy;", "blockUser", "", "loadProfile", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "openFragment", "fragment", "renderProfile", "sendFriendRequest", "showDevOptions", "showEditProfile", "showEditStatus", "uploadAvatar", "uri", "Landroid/net/Uri;", "uploadCover", "Companion", "app_originalRelease"})
public final class ProfileFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.conzchat.app.databinding.FragmentProfileBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy profileUid$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private java.util.Map<java.lang.String, ? extends java.lang.Object> profileData;
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.firestore.ListenerRegistration profileListener;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> avatarPicker = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> coverPicker = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.ui.profile.ProfileFragment.Companion Companion = null;
    
    public ProfileFragment() {
        super();
    }
    
    private final com.conzchat.app.databinding.FragmentProfileBinding getBinding() {
        return null;
    }
    
    private final java.lang.String getProfileUid() {
        return null;
    }
    
    private final java.lang.String getMyUid() {
        return null;
    }
    
    private final boolean isMyProfile() {
        return false;
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
    
    private final void loadProfile() {
    }
    
    private final void renderProfile() {
    }
    
    private final void showEditStatus() {
    }
    
    private final void showEditProfile() {
    }
    
    private final void uploadAvatar(android.net.Uri uri) {
    }
    
    private final void uploadCover(android.net.Uri uri) {
    }
    
    private final void sendFriendRequest() {
    }
    
    private final void blockUser() {
    }
    
    private final void showDevOptions() {
    }
    
    private final void openFragment(androidx.fragment.app.Fragment fragment) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/conzchat/app/ui/profile/ProfileFragment$Companion;", "", "()V", "newInstance", "Lcom/conzchat/app/ui/profile/ProfileFragment;", "uid", "", "app_originalRelease"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.conzchat.app.ui.profile.ProfileFragment newInstance(@org.jetbrains.annotations.NotNull()
        java.lang.String uid) {
            return null;
        }
    }
}