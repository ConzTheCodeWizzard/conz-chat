package com.conzchat.app.util;

/**
 * Sends push notifications via FCM v1 API using a service account JWT.
 * This approach works regardless of the sender's app version — only the
 * receiver needs to be on v4.0.7+ to have their FCM token stored in Firestore.
 *
 * Flow:
 * 1. Sender calls sendDmNotification / sendGroupNotification / sendFriendRequestNotification
 * 2. This class reads the receiver's fcmToken from Firestore (users/{uid}/fcmToken)
 * 3. Generates a short-lived OAuth2 JWT from the embedded service account private key
 * 4. Exchanges the JWT for an access token from Google's OAuth2 endpoint
 * 5. Sends the FCM v1 notification to the receiver's device token
 * 6. Receiver's device shows the notification even if the app is completely killed
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010$\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0010H\u0002J<\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u00042\u0006\u0010\u0015\u001a\u00020\u00042\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u00162\u0006\u0010\u0017\u001a\u00020\u0004H\u0002J\b\u0010\u0018\u001a\u00020\u0004H\u0002J\u0010\u0010\u0019\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u0004H\u0002J\n\u0010\u001b\u001a\u0004\u0018\u00010\u0004H\u0002J\u0018\u0010\u001c\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u001d\u001a\u00020\u0004H\u0082@\u00a2\u0006\u0002\u0010\u001eJ&\u0010\u001f\u001a\u00020 2\u0006\u0010!\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00042\u0006\u0010$\u001a\u00020\u0004J\u0018\u0010%\u001a\u00020 2\u0006\u0010\u0015\u001a\u00020\u00122\u0006\u0010&\u001a\u00020\u0004H\u0002J\u001e\u0010\'\u001a\u00020 2\u0006\u0010!\u001a\u00020\u00042\u0006\u0010(\u001a\u00020\u00042\u0006\u0010)\u001a\u00020\u0004J4\u0010*\u001a\u00020 2\f\u0010+\u001a\b\u0012\u0004\u0012\u00020\u00040,2\u0006\u0010-\u001a\u00020\u00042\u0006\u0010\"\u001a\u00020\u00042\u0006\u0010#\u001a\u00020\u00042\u0006\u0010.\u001a\u00020\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006/"}, d2 = {"Lcom/conzchat/app/util/FcmNotifier;", "", "()V", "FCM_SCOPE", "", "FCM_URL", "PRIVATE_KEY_BASE64", "PROJECT_ID", "SERVICE_ACCOUNT_EMAIL", "TAG", "TOKEN_URL", "cachedAccessToken", "tokenExpiryMs", "", "base64UrlEncode", "data", "", "buildFcmMessage", "Lorg/json/JSONObject;", "deviceToken", "title", "body", "", "channelId", "buildJwt", "exchangeJwtForAccessToken", "jwt", "getAccessToken", "getFcmToken", "uid", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendDmNotification", "", "toUid", "senderName", "messageText", "senderUid", "sendFcmMessage", "accessToken", "sendFriendRequestNotification", "fromName", "fromUid", "sendGroupNotification", "toUids", "", "groupName", "chatId", "app_cloneRelease"})
public final class FcmNotifier {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "FcmNotifier";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PROJECT_ID = "conzchat";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String FCM_URL = "https://fcm.googleapis.com/v1/projects/conzchat/messages:send";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TOKEN_URL = "https://oauth2.googleapis.com/token";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SERVICE_ACCOUNT_EMAIL = "firebase-adminsdk-fbsvc@conzchat.iam.gserviceaccount.com";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PRIVATE_KEY_BASE64 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC+gUPXRM0QadvT36+QzieOxKtkCLEwVqiUe6ovBbJc64nMNFFe+L4tjL7z06a3xYVtyvj+eBJsAfr71kV9eVmUu0uZmtsx6W6j+LN9FmRb9xZz5MKg1wB9r74QQq1O5fv1J9I5KNgas3ztg3V9xZ7TqVOK7M6yhSYHoXW4wQqSm6wxOfTSJsgZ936g0FNlszj5tMtcbRM14pMr5hM6T93XTf3GCrClw26wOXhVHfG3SfaF8MPwht6tpfqXeYi7zj5CJ7ggVtO/Wf8nJAHjPEiYbrhdDWEwhO1Px39YtD4X6u2uH/wnvUbn12oOH4UTJovip5Mn7HnH/3s+yfFo4+ktAgMBAAECggEAMgK0QQxmOvNXSrEzoTOFbfTSO4IOpe5x9fza6do4fy00MP/hBqoNHt8Od+rtntvDEdYRdJgn2ePRNBCFkJFtQ17B03K0LU+nBHOzBtj8gqoEYmtA7sFSdyxOHG2smoRElxCbnvbKeIP6/U0f6GMbNVyDuno9Lt9GYwVvZo+Cy4aPWznprIMSJ1ZvivgvDUjUFHZNMtfyZbakkLhB3tdjNj6qOiZR++Q4z9eboyyL43N9B01JlI5Npokp1czRrcIlzLd3mLzrqNvFiTQ9AhBkGXihxHTdFS4MHkIBVDzpnb4x4dzRv9dxxmaFFlAmTWQdTiaDfuwA/a9RdoN3LzMX/wKBgQDoxFltyIQNb+IKEyAc+A7hCLcSgDd9+7yZARTgzuoUa8FlqZVGJjt7w8HKKdW/MsB33J+d9A/af4ebuJ6Dm3Ry34St6C5DT7XMqJ+BX4ZpBqBz5RBuIMr2qXvxxyOJPIslJMMrOmFhIgAEeqlJ9qP65mmp9IwuQGbVPTURQIPo7wKBgQDRhQmzkvcj1DUP0208+dOcfvBhRJbRqqd1E6Etq+AgwVsFv52B+bLlgjJsKG/u2CQn7/vpOgv7j0d8R/MQ7q855HHMQ5+D28YtSWCs+I5IeXGqrzKe03nfIoo0Bo/O9C5/3r1xrdOgWImH5DI+RdFShfb766P4REwzflev01j3owKBgQDGnv28q1TbbnRQqn9FmPqBVgF5/QTdMRl+6ihZqNaftE9kI7AoGum5M5LCoq4tJ+6KmS1vCFsa1KXv4DsLDHTyrP63sx7++x6j0+O/7rZwGmKCYp2Bi+QFVRxZdNdC/PGzMUqFMZz02KFxMQYSAi9tIn5Zsz6HHRdisIb/ALFO6wKBgDOOuE9vJ8+yRYhqb6QkmfUVq5NT7IUVqHV/6hayB8onqn1kpJ66UlJ10nCZFspAd804GdZPiWlS3bVwgxi0k3v8giBQt60b5M16FAccdu7Qq67jw+Ifigrllfqtbq/vmI2www95Re1cPCOrfM2kIplbC+b8GnJaZCH7whorOOZfAoGBAMwQvC0giJxHun1tnXf5TRdWjO87jxRbHjLUM4MwLZ2JtbYcVYCJWXz5onCze8mas3/fxWjAeu+EWP9CIGLQEPkICyx3Xwg+IkikQa4lhJzmBbXEU1eZJXyelFFtR1iCoL1ghqUJq035rBX/YMtacEmY5EUVD6hP62F2yLHvFOXf";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.lang.String cachedAccessToken;
    @kotlin.jvm.Volatile()
    private static volatile long tokenExpiryMs = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.FcmNotifier INSTANCE = null;
    
    private FcmNotifier() {
        super();
    }
    
    public final void sendDmNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String toUid, @org.jetbrains.annotations.NotNull()
    java.lang.String senderName, @org.jetbrains.annotations.NotNull()
    java.lang.String messageText, @org.jetbrains.annotations.NotNull()
    java.lang.String senderUid) {
    }
    
    public final void sendGroupNotification(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> toUids, @org.jetbrains.annotations.NotNull()
    java.lang.String groupName, @org.jetbrains.annotations.NotNull()
    java.lang.String senderName, @org.jetbrains.annotations.NotNull()
    java.lang.String messageText, @org.jetbrains.annotations.NotNull()
    java.lang.String chatId) {
    }
    
    public final void sendFriendRequestNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String toUid, @org.jetbrains.annotations.NotNull()
    java.lang.String fromName, @org.jetbrains.annotations.NotNull()
    java.lang.String fromUid) {
    }
    
    private final java.lang.Object getFcmToken(java.lang.String uid, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final org.json.JSONObject buildFcmMessage(java.lang.String deviceToken, java.lang.String title, java.lang.String body, java.util.Map<java.lang.String, java.lang.String> data, java.lang.String channelId) {
        return null;
    }
    
    private final void sendFcmMessage(org.json.JSONObject body, java.lang.String accessToken) {
    }
    
    /**
     * Gets a valid OAuth2 access token for FCM v1 API.
     * Uses a cached token if still valid (expires in 1 hour).
     * Otherwise generates a new JWT and exchanges it for an access token.
     */
    @kotlin.jvm.Synchronized()
    private final synchronized java.lang.String getAccessToken() {
        return null;
    }
    
    /**
     * Builds a signed JWT for service account authentication.
     * Format: base64(header).base64(claims).base64(signature)
     */
    private final java.lang.String buildJwt() {
        return null;
    }
    
    private final java.lang.String exchangeJwtForAccessToken(java.lang.String jwt) {
        return null;
    }
    
    private final java.lang.String base64UrlEncode(byte[] data) {
        return null;
    }
}