package com.indiatv.launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Desi tvOS Launcher - main home screen.
 *
 * Layout: header (clock + search) -> featured banner -> rows:
 *   Favorites / Recents / All Apps.
 *
 * Remote keys:
 *   D-pad  = move focus (native focus engine does the work)
 *   CENTER/ENTER = open focused app
 *   MENU   = launcher settings dialog
 *   BACK   = standby clock overlay (BACK again or any key wakes)
 *   SEARCH = focus the search bar
 * Long-press CENTER on an app = toggle favorite.
 */
public class MainActivity extends Activity {

    private static final int STANDBY_AFTER_MS = 60000; // auto-standby: 60s idle

    private LauncherSettings settings;
    private AppsRepository repo;

    private LinearLayout rowsContainer;
    private EditText searchBar;
    private TextView txtClock, txtStandbyClock, txtStandbyDate;
    private TextView txtCalMonth, txtCalDay, txtCalDate;
    private FrameLayout standbyOverlay;

    private List<AppInfo> allApps;
    private List<AppInfo> favorites;
    private List<AppInfo> recents;

    private Handler handler = new Handler();
    private Runnable clockTick, standbyWatchdog;
    private boolean standbyMode = false;
    private String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new LauncherSettings(this);
        repo = new AppsRepository(this);

        rowsContainer = (LinearLayout) findViewById(R.id.rowsContainer);
        searchBar = (EditText) findViewById(R.id.searchBar);
        txtClock = (TextView) findViewById(R.id.txtClock);
        txtStandbyClock = (TextView) findViewById(R.id.txtStandbyClock);
        txtStandbyDate = (TextView) findViewById(R.id.txtStandbyDate);
        txtCalMonth = (TextView) findViewById(R.id.txtCalendarMonth);
        txtCalDay = (TextView) findViewById(R.id.txtCalendarDay);
        txtCalDate = (TextView) findViewById(R.id.txtCalendarDateNum);

        setupStandbyOverlay();
        setupSearch();
        loadAndRender();
        startClock();
        setupRemoteKeys();

        // Keep screen on while the launcher is visible (it is a TV/launcher).
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /* ================= DATA + ROWS ================= */

    private void loadAndRender() {
        allApps = repo.loadApps(settings.isAlphabetical());
        favorites = repo.getFavorites(allApps);
        recents = repo.getRecents(allApps);

        renderRows();
    }

    private void renderRows() {
        rowsContainer.removeAllViews();

        List<AppInfo> visible = filter(allApps, query);

        if (!favorites.isEmpty()) addRow("Favorites", favorites);
        if (!recents.isEmpty()) addRow("Recent", recents);
        addRow("Apps", visible);
        if (visible.isEmpty()) {
            Toast.makeText(this, "Koi app nahi mila: " + query, Toast.LENGTH_SHORT).show();
        }

        // Focus the first app card of the first row.
        rowsContainer.post(new Runnable() {
            @Override
            public void run() {
                View first = rowsContainer.findViewById(R.id.appCard);
                if (first != null) first.requestFocus();
            }
        });
    }

    /** Adds one tvOS-style horizontal row with a label. */
    private void addRow(String label, List<AppInfo> apps) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(0xAAFFFFFF);
        tv.setTextSize(14f);
        tv.setPadding(dp(40), dp(12), 0, dp(6));
        rowsContainer.addView(tv);

        LinearLayout scroller = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.row_tvos_apps, rowsContainer, false);
        LinearLayout items = (LinearLayout) scroller.findViewById(R.id.rowItems);
        int iconDp = settings.getIconSizeDp();

        for (final AppInfo app : apps) {
            View card = getLayoutInflater().inflate(R.layout.item_tvos_app, items, false);
            ImageView icon = (ImageView) card.findViewById(R.id.appIcon);
            TextView name = (TextView) card.findViewById(R.id.appName);

            ViewGroup.LayoutParams lp = icon.getLayoutParams();
            lp.width = dp(iconDp);
            lp.height = dp(iconDp);
            icon.setLayoutParams(lp);

            icon.setImageDrawable(app.icon);
            name.setText(app.label);
            FocusEffects.attach(this, card, settings.isDarkTheme());

            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launch(app);
                }
            });

            card.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    boolean nowFav = repo.toggleFavorite(app);
                    Toast.makeText(MainActivity.this,
                            nowFav ? "Favorite added" : "Favorite removed",
                            Toast.LENGTH_SHORT).show();
                    loadAndRender();
                    return true;
                }
            });

            items.addView(card);
        }
        rowsContainer.addView(scroller);
    }

    private static List<AppInfo> filter(List<AppInfo> in, String q) {
        java.util.ArrayList<AppInfo> out = new java.util.ArrayList<AppInfo>();
        if (q == null || q.length() == 0) return in;
        String needle = q.toLowerCase(Locale.US);
        for (AppInfo a : in) {
            if (a.label.toLowerCase(Locale.US).contains(needle)) out.add(a);
        }
        return out;
    }

    private void launch(AppInfo app) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(app.packageName);
            if (i != null) {
                repo.recordLaunch(app);
                startActivity(i);
            } else {
                Toast.makeText(this, "Cannot open " + app.label, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open " + app.label, Toast.LENGTH_SHORT).show();
        }
    }

    /* ================= SEARCH ================= */

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                renderRows();
            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                renderRows(); // re-focus first result
                return true;
            }
        });
    }

    /* ================= STANDBY ================= */

    private void setupStandbyOverlay() {
        standbyOverlay = new FrameLayout(this);
        standbyOverlay.setBackgroundColor(0xFF000000);
        getLayoutInflater().inflate(R.layout.overlay_standby, standbyOverlay, true);
        addContentView(standbyOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        standbyOverlay.setVisibility(View.GONE);
        standbyOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { wakeUp(); }
        });
    }

    private void enterStandby() {
        standbyMode = true;
        standbyOverlay.setVisibility(View.VISIBLE);
        standbyOverlay.setAlpha(0f);
        standbyOverlay.animate().alpha(1f).setDuration(500L).start();
        resetStandbyWatchdog();
    }

    private void wakeUp() {
        standbyMode = false;
        standbyOverlay.animate().alpha(0f).setDuration(400L)
                .withEndAction(new Runnable() {
                    @Override public void run() {
                        standbyOverlay.setVisibility(View.GONE);
                        standbyOverlay.setAlpha(1f);
                    }
                }).start();
        resetStandbyWatchdog();
    }

    /** Auto-standby after STANDBY_AFTER_MS without any key input. */
    private void resetStandbyWatchdog() {
        handler.removeCallbacks(standbyWatchdog);
        if (settings.isStandbyTimerEnabled()) {
            handler.postDelayed(standbyWatchdog, STANDBY_AFTER_MS);
        }
    }

    private void setupRemoteKeys() {
        standbyWatchdog = new Runnable() {
            @Override public void run() { if (!standbyMode) enterStandby(); }
        };
        resetStandbyWatchdog();
    }

    /* ================= CLOCK ================= */

    private void startClock() {
        clockTick = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                String t = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(now);
                txtClock.setText(t);
                txtStandbyClock.setText(t);
                txtStandbyDate.setText(
                        new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(now));
                txtCalMonth.setText(
                        new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now).toUpperCase());
                txtCalDay.setText(
                        new SimpleDateFormat("EEEE", Locale.getDefault()).format(now).toUpperCase());
                txtCalDate.setText(new SimpleDateFormat("d", Locale.getDefault()).format(now));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(clockTick);
    }

    /* ================= REMOTE / HARDWARE KEYS ================= */

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // Any key while in standby wakes it up (except letting BACK through).
        if (standbyMode && e.getAction() == KeyEvent.ACTION_DOWN) {
            wakeUp();
            return true;
        }
        if (e.getAction() == KeyEvent.ACTION_DOWN) {
            switch (e.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    showSettings();
                    return true;
                case KeyEvent.KEYCODE_SEARCH:
                    searchBar.requestFocus();
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    // BACK on home = standby, like Apple TV's sleep
                    if (!searchBar.hasFocus() || searchBar.getText().length() == 0) {
                        enterStandby();
                        return true;
                    }
                    // fall-through: clear search instead
                    searchBar.setText("");
                    return true;
            }
        }
        return super.dispatchKeyEvent(e);
    }

    /* ================= SETTINGS ================= */

    private void showSettings() {
        new SettingsDialog(this, new SettingsDialog.OnChanged() {
            @Override
            public void onChange() {
                applyTheme();
                loadAndRender();
            }
        }).show();
    }

    private void applyTheme() {
        findViewById(R.id.rootContainer).setBackgroundColor(settings.isDarkTheme() ? 0xFF000000 : 0xFFF2F2F7);
        ((LinearLayout) findViewById(R.id.rootContainer))
                .setBackgroundDrawable(settings.isDarkTheme() ? settings.wallpaper() : null);
    }

    private int dp(int v) {
        return (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    /* ================= LIFECYCLE ================= */

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        loadAndRender(); // apps may have changed while away
        resetStandbyWatchdog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(standbyWatchdog);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
