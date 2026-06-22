package com.conzchat.app.util;

/**
 * Applies Harley Quinn theme to the entire view hierarchy.
 * Call [applyTheme] from any Fragment's onViewCreated to theme it.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\"\n\u0002\u0010\b\n\u0002\b\r\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0014\u001a\u00020\u0015J\u0018\u0010\u001a\u001a\u00020\u00132\u0006\u0010\u001b\u001a\u00020\u00172\u0006\u0010\u001c\u001a\u00020\u0019H\u0002J\u001a\u0010\u001d\u001a\u00020\u00132\u0006\u0010\u001b\u001a\u00020\u00172\b\b\u0002\u0010\u001c\u001a\u00020\u0019H\u0002R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/conzchat/app/util/HarleyThemeHelper;", "", "()V", "DARK_BG_COLORS", "", "", "HQ_ACCENT", "HQ_BG", "HQ_BG_CARD", "HQ_BG_INPUT", "HQ_BUTTON", "HQ_CHAT_BAR", "HQ_DIVIDER", "HQ_PRIMARY", "HQ_TEXT_PRIMARY", "HQ_TEXT_SECONDARY", "HQ_TOP_BAR", "RED_COLORS", "applyTheme", "", "ctx", "Landroid/content/Context;", "rootView", "Landroid/view/View;", "isActive", "", "themeView", "view", "isRoot", "walkAndTheme", "app_originalRelease"})
public final class HarleyThemeHelper {
    public static final int HQ_PRIMARY = -1499508;
    public static final int HQ_ACCENT = -16728876;
    public static final int HQ_BG = 0;
    public static final int HQ_BG_CARD = -870708704;
    public static final int HQ_BG_INPUT = -14020560;
    public static final int HQ_TOP_BAR = -586022886;
    public static final int HQ_CHAT_BAR = -586547182;
    public static final int HQ_TEXT_PRIMARY = -1;
    public static final int HQ_TEXT_SECONDARY = -32565;
    public static final int HQ_BUTTON = -1499508;
    public static final int HQ_DIVIDER = -12969398;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.Integer> DARK_BG_COLORS = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.Set<java.lang.Integer> RED_COLORS = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.conzchat.app.util.HarleyThemeHelper INSTANCE = null;
    
    private HarleyThemeHelper() {
        super();
    }
    
    public final boolean isActive(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return false;
    }
    
    /**
     * Main entry point — call this in onViewCreated of any fragment.
     * It makes the fragment root transparent and recursively recolors all child views.
     */
    public final void applyTheme(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    android.view.View rootView) {
    }
    
    private final void walkAndTheme(android.view.View view, boolean isRoot) {
    }
    
    private final void themeView(android.view.View view, boolean isRoot) {
    }
}