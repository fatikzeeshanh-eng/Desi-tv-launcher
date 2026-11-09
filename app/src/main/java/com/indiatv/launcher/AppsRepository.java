package com.indiatv.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Loads installed apps and persists recents + favorites.
 * Ordered lists are stored as comma-joined strings because
 * SharedPreferences StringSet does not preserve order.
 */
public class AppsRepository {

    private static final int MAX_RECENTS = 10;

    private final Context ctx;
    private final SharedPreferences prefs;

    public AppsRepository(Context c) {
        ctx = c.getApplicationContext();
        prefs = ctx.getSharedPreferences("tvos_prefs", Context.MODE_PRIVATE);
    }

    /** All launchable apps on the device, except the launcher itself. */
    public List<AppInfo> loadApps(boolean alphabetical) {
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolved = ctx.getPackageManager().queryIntentActivities(main, 0);
        List<AppInfo> out = new ArrayList<AppInfo>();
        String self = ctx.getPackageName();

        for (ResolveInfo ri : resolved) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(self)) continue;
            out.add(new AppInfo(
                    ri.loadLabel(ctx.getPackageManager()).toString(),
                    pkg,
                    ri.activityInfo.name,
                    ri.loadIcon(ctx.getPackageManager())));
        }

        if (alphabetical) {
            Collections.sort(out, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo a, AppInfo b) {
                    return a.label.compareToIgnoreCase(b.label);
                }
            });
        }
        return out;
    }

    /* ---------------- recents ---------------- */

    public void recordLaunch(AppInfo app) {
        String k = app.key();
        List<String> order = split(prefs.getString("recents", ""));
        order.remove(k);
        order.add(0, k);
        while (order.size() > MAX_RECENTS) order.remove(order.size() - 1);
        prefs.edit().putString("recents", join(order)).commit();
    }

    public List<AppInfo> getRecents(List<AppInfo> all) {
        List<AppInfo> out = new ArrayList<AppInfo>();
        for (String k : split(prefs.getString("recents", ""))) {
            AppInfo a = findByKey(all, k);
            if (a != null) out.add(a);
        }
        return out;
    }

    /* ---------------- favorites ---------------- */

    public boolean isFavorite(AppInfo app) {
        return split(prefs.getString("favorites", "")).contains(app.key());
    }

    /** @return true if the app is now a favorite, false if it was removed. */
    public boolean toggleFavorite(AppInfo app) {
        String k = app.key();
        List<String> favs = split(prefs.getString("favorites", ""));
        boolean nowFavorite;
        if (favs.contains(k)) {
            favs.remove(k);
            nowFavorite = false;
        } else {
            favs.add(k);
            nowFavorite = true;
        }
        prefs.edit().putString("favorites", join(favs)).commit();
        return nowFavorite;
    }

    /** Favorites in the same order as the (sorted) master list. */
    public List<AppInfo> getFavorites(List<AppInfo> all) {
        List<String> favs = split(prefs.getString("favorites", ""));
        List<AppInfo> out = new ArrayList<AppInfo>();
        for (AppInfo a : all) {
            if (favs.contains(a.key())) out.add(a);
        }
        return out;
    }

    /* ---------------- helpers ---------------- */

    private static AppInfo findByKey(List<AppInfo> all, String key) {
        for (AppInfo a : all) {
            if (a.key().equals(key)) return a;
        }
        return null;
    }

    private static List<String> split(String s) {
        List<String> out = new ArrayList<String>();
        if (s == null || s.length() == 0) return out;
        String[] parts = s.split(",");
        for (String p : parts) {
            if (p.length() > 0) out.add(p);
        }
        return out;
    }

    private static String join(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
