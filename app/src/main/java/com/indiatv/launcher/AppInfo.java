package com.indiatv.launcher;

import android.graphics.drawable.Drawable;

/**
 * Simple immutable model for one installed app.
 */
public class AppInfo {

    public final String label;
    public final String packageName;
    public final String className;
    public final Drawable icon;

    public AppInfo(String label, String packageName, String className, Drawable icon) {
        this.label = label;
        this.packageName = packageName;
        this.className = className;
        this.icon = icon;
    }

    public String key() {
        return packageName + "/" + className;
    }
}
