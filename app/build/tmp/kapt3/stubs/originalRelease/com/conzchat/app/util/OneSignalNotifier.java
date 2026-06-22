package com.conzchat.app.util;

/**
 * Sends push notifications via OneSignal REST API.
 * Called from the sender's device whenever they send a message.
 * OneSignal delivers the notification to the recipient's device
 * even if their app is completely closed.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0010 \n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bH\u0002J&\u0010\f\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u0004J\u001e\u0010\u0011\u001a\u00020\t2\u0006\u0010\r\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u0004J4\u0010\u0014\u001a\u00020\t2\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00040\u00162\u0006\u0010\u0017\u001a\u00020\u00042\u0006\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/conzchat/app/util/OneSignalNotifier;", "", "()V", "API_URL", "", "APP_ID", "REST_API_KEY", "TAG", "postToOneSignal", "", "body", "Lorg/json/JSONObject;", "sendDmNotification", "toUid", "senderName", "messageText", "senderUid", "sendFriendRequestNotification", "fromName", "fromUid", "sendGroupNotification", "toUids", "", "groupName", "chatId", "app_originalRelease"})
public final class OneSignalNotifier {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "OneSignalNotifier";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String APP_ID = "72d0a73d-b1ed-4ffa-9356-f84d79a0e0cc";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String REST_API_KEY = "os_v2_app_olikopnr5vh7ve2w7bgxtihazqe4wqly24mumbvuy5rung43ujy3utgfgzypshsumarwxj47t7hbe3t7vw5xrl5mpnh7hyywzbvlx6y";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String API_URL = "https://api.onesignal.com/notifications";
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.OneSignalNotifier INSTANCE = null;
    
    private OneSignalNotifier() {
        super();
    }
    
    /**
     * Send a DM notification to a specific user by their UID.
     * @param toUid Firebase UID of the recipient
     * @param senderName Display name of the sender
     * @param messageText The message text (or "📷 Photo", "🎤 Voice", etc.)
     * @param senderUid Firebase UID of the sender (for tap-to-open)
     */
    public final void sendDmNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String toUid, @org.jetbrains.annotations.NotNull()
    java.lang.String senderName, @org.jetbrains.annotations.NotNull()
    java.lang.String messageText, @org.jetbrains.annotations.NotNull()
    java.lang.String senderUid) {
    }
    
    /**
     * Send a group message notification to multiple users.
     * @param toUids List of Firebase UIDs to notify (excluding the sender)
     * @param groupName Name of the group
     * @param senderName Display name of the sender
     * @param messageText The message text
     * @param chatId The group chat ID (for tap-to-open)
     */
    public final void sendGroupNotification(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> toUids, @org.jetbrains.annotations.NotNull()
    java.lang.String groupName, @org.jetbrains.annotations.NotNull()
    java.lang.String senderName, @org.jetbrains.annotations.NotNull()
    java.lang.String messageText, @org.jetbrains.annotations.NotNull()
    java.lang.String chatId) {
    }
    
    /**
     * Send a friend request notification.
     */
    public final void sendFriendRequestNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String toUid, @org.jetbrains.annotations.NotNull()
    java.lang.String fromName, @org.jetbrains.annotations.NotNull()
    java.lang.String fromUid) {
    }
    
    private final void postToOneSignal(org.json.JSONObject body) {
    }
}