package com.conzchat.app.model;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\b/\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u00bb\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0003\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0003\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u0012\b\b\u0002\u0010\r\u001a\u00020\f\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0010\u0012\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012\u0012\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012\u0012\b\b\u0002\u0010\u0014\u001a\u00020\f\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0017J\t\u0010-\u001a\u00020\u0003H\u00c6\u0003J\t\u0010.\u001a\u00020\fH\u00c6\u0003J\t\u0010/\u001a\u00020\u0003H\u00c6\u0003J\t\u00100\u001a\u00020\u0010H\u00c6\u0003J\u000f\u00101\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012H\u00c6\u0003J\u000f\u00102\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012H\u00c6\u0003J\t\u00103\u001a\u00020\fH\u00c6\u0003J\t\u00104\u001a\u00020\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\u0003H\u00c6\u0003J\t\u00106\u001a\u00020\u0003H\u00c6\u0003J\t\u00107\u001a\u00020\u0003H\u00c6\u0003J\t\u00108\u001a\u00020\u0003H\u00c6\u0003J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\t\u0010:\u001a\u00020\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0003H\u00c6\u0003J\t\u0010<\u001a\u00020\u0003H\u00c6\u0003J\t\u0010=\u001a\u00020\fH\u00c6\u0003J\u00bf\u0001\u0010>\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u00032\b\b\u0002\u0010\n\u001a\u00020\u00032\b\b\u0002\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\u00032\b\b\u0002\u0010\u000f\u001a\u00020\u00102\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u00122\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\u00122\b\b\u0002\u0010\u0014\u001a\u00020\f2\b\b\u0002\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010?\u001a\u00020\f2\b\u0010@\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010A\u001a\u00020BH\u00d6\u0001J\t\u0010C\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\r\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001dR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001dR\u0011\u0010\u000e\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001dR\u0011\u0010\u0014\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u0019R\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00030\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u001bR\u0011\u0010\u0015\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001dR\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001dR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0019R\u0011\u0010\u0016\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u001dR\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001dR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001dR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001dR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001d\u00a8\u0006D"}, d2 = {"Lcom/conzchat/app/model/User;", "", "uid", "", "username", "usernameLower", "displayName", "email", "photo", "coverPhoto", "status", "premium", "", "banned", "fcmToken", "created", "", "friends", "", "blockedUsers", "forceLogout", "logoutMessage", "premiumPopup", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZLjava/lang/String;JLjava/util/List;Ljava/util/List;ZLjava/lang/String;Ljava/lang/String;)V", "getBanned", "()Z", "getBlockedUsers", "()Ljava/util/List;", "getCoverPhoto", "()Ljava/lang/String;", "getCreated", "()J", "getDisplayName", "getEmail", "getFcmToken", "getForceLogout", "getFriends", "getLogoutMessage", "getPhoto", "getPremium", "getPremiumPopup", "getStatus", "getUid", "getUsername", "getUsernameLower", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "", "toString", "app_cloneRelease"})
public final class User {
    @com.google.firebase.firestore.DocumentId()
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String uid = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String username = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String usernameLower = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String email = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String photo = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String coverPhoto = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String status = null;
    private final boolean premium = false;
    private final boolean banned = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String fcmToken = null;
    private final long created = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> friends = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> blockedUsers = null;
    private final boolean forceLogout = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String logoutMessage = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String premiumPopup = null;
    
    public User(@org.jetbrains.annotations.NotNull()
    java.lang.String uid, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    java.lang.String usernameLower, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String photo, @org.jetbrains.annotations.NotNull()
    java.lang.String coverPhoto, @org.jetbrains.annotations.NotNull()
    java.lang.String status, boolean premium, boolean banned, @org.jetbrains.annotations.NotNull()
    java.lang.String fcmToken, long created, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> friends, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> blockedUsers, boolean forceLogout, @org.jetbrains.annotations.NotNull()
    java.lang.String logoutMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String premiumPopup) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUid() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsername() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUsernameLower() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEmail() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPhoto() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCoverPhoto() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatus() {
        return null;
    }
    
    public final boolean getPremium() {
        return false;
    }
    
    public final boolean getBanned() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFcmToken() {
        return null;
    }
    
    public final long getCreated() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getFriends() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getBlockedUsers() {
        return null;
    }
    
    public final boolean getForceLogout() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLogoutMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPremiumPopup() {
        return null;
    }
    
    public User() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component10() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    public final long component12() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component13() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> component14() {
        return null;
    }
    
    public final boolean component15() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component17() {
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
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    public final boolean component9() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.conzchat.app.model.User copy(@org.jetbrains.annotations.NotNull()
    java.lang.String uid, @org.jetbrains.annotations.NotNull()
    java.lang.String username, @org.jetbrains.annotations.NotNull()
    java.lang.String usernameLower, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.NotNull()
    java.lang.String email, @org.jetbrains.annotations.NotNull()
    java.lang.String photo, @org.jetbrains.annotations.NotNull()
    java.lang.String coverPhoto, @org.jetbrains.annotations.NotNull()
    java.lang.String status, boolean premium, boolean banned, @org.jetbrains.annotations.NotNull()
    java.lang.String fcmToken, long created, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> friends, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> blockedUsers, boolean forceLogout, @org.jetbrains.annotations.NotNull()
    java.lang.String logoutMessage, @org.jetbrains.annotations.NotNull()
    java.lang.String premiumPopup) {
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