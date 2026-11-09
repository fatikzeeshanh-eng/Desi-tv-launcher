package com.indiatv.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;

/**
 * Launcher preferences, persisted in SharedPreferences.
 * No external dependencies - pure Android 4.4 (API 19) compatible.
 */
public class LauncherSettings {

    public static final int WALLPAPER_COUNT = 5;

    private final SharedPreferences prefs;

    public LauncherSettings(Context c) {
        prefs = c.getSharedPreferences("tvos_settings", Context.MODE_PRIVATE);
    }

    /* ---------------- wallpaper ---------------- */

    public int getWallpaperIndex() {
        int i = prefs.getInt("wallpaper", 0);
        if (i < 0 || i >= WALLPAPER_COUNT) i = 0;
        return i;
    }

    public void cycleWallpaper() {
        prefs.edit().putInt("wallpaper", (getWallpaperIndex() + 1) % WALLPAPER_COUNT).apply();
    }

    /** tvOS-style dark gradient wallpapers, built in code (no image assets needed). */
    public GradientDrawable wallpaper() {
        int[][] pairs = {
            {0xFF0A0A0A, 0xFF1C1C1E}, // graphite
            {0xFF000428, 0xFF004E92}, // deep blue
            {0xFF232526, 0xFF414345}, // slate
            {0xFF1A2980, 0xFF26D0CE}, // teal
            {0xFF2C1B47, 0xFF7F53AC}  // purple
        };
        int[] c = pairs[getWallpaperIndex()];
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, c);
    }

    /* ---------------- icon size ---------------- */

    /** 0 = small, 1 = medium, 2 = large */
    public int getIconSizeIndex() {
        return prefs.getInt("icon_size", 1);
    }

    public int getIconSizeDp() {
        int i = getIconSizeIndex();
        return i == 0 ? 48 : (i == 1 ? 56 : 68);
    }

    public String getIconSizeLabel() {
        int i = getIconSizeIndex();
        return i == 0 ? "Small" : (i == 1 ? "Medium" : "Large");
    }

    public void cycleIconSize() {
        prefs.edit().putInt("icon_size", (getIconSizeIndex() + 1) % 3).apply();
    }

    /* ---------------- theme ---------------- */

    public boolean isDarkTheme() {
        return prefs.getBoolean("dark_theme", true);
    }

    public void toggleTheme() {
        prefs.edit().putBoolean("dark_theme", !isDarkTheme()).apply();
    }

    /* ---------------- sort ---------------- */

    public boolean isAlphabetical() {
        return prefs.getBoolean("alpha_sort", true);
    }

    public void toggleSort() {
        prefs.edit().putBoolean("alpha_sort", !isAlphabetical()).apply();
    }

    /* ---------------- auto standby ---------------- */

    public boolean isStandbyTimerEnabled() {
        return prefs.getBoolean("auto_standby", true);
    }

    public void toggleStandbyTimer() {
        prefs.edit().putBoolean("auto_standby", !isStandbyTimerEnabled()).apply();
    }
}
