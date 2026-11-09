package com.indiatv.launcher;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/**
 * Launcher settings dialog (opened with the remote MENU key).
 */
public class SettingsDialog extends Dialog {

    public interface OnChanged { void onChange(); }

    private final Activity act;
    private final LauncherSettings settings;
    private final OnChanged onChanged;

    public SettingsDialog(Activity act, OnChanged onChanged) {
        super(act);
        this.act = act;
        this.settings = new LauncherSettings(act);
        this.onChanged = onChanged;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_settings);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button theme = (Button) findViewById(R.id.btnToggleTheme);
        theme.setText("Theme: " + (settings.isDarkTheme() ? "Dark" : "Light"));
        theme.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                settings.toggleTheme();
                dismiss();
                notifyChanged();
            }
        });

        Button wallpaper = (Button) findViewById(R.id.btnToggleWallpaper);
        wallpaper.setText("Wallpaper: " + (settings.getWallpaperIndex() + 1) + " / " + LauncherSettings.WALLPAPER_COUNT);
        wallpaper.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                settings.cycleWallpaper();
                notifyChanged();
            }
        });

        Button iconSize = (Button) findViewById(R.id.btnToggleIconSize);
        iconSize.setText("Icon Size: " + settings.getIconSizeLabel());
        iconSize.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                settings.cycleIconSize();
                notifyChanged();
            }
        });

        Button sort = (Button) findViewById(R.id.btnToggleSort);
        sort.setText("Sort: " + (settings.isAlphabetical() ? "A-Z" : "Custom"));
        sort.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                settings.toggleSort();
                notifyChanged();
            }
        });

        Button autoStandby = (Button) findViewById(R.id.btnToggleAutoStandby);
        autoStandby.setText("Auto Standby: " + (settings.isStandbyTimerEnabled() ? "On" : "Off"));
        autoStandby.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                settings.toggleStandbyTimer();
                refreshLabels();
            }
        });

        findViewById(R.id.btnSystemSettings).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                act.startActivity(new Intent(Settings.ACTION_SETTINGS));
                dismiss();
            }
        });

        findViewById(R.id.btnCloseSettings).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dismiss(); }
        });

        refreshLabels();
    }

    private void refreshLabels() {
        ((Button) findViewById(R.id.btnToggleTheme)).setText(
                "Theme: " + (settings.isDarkTheme() ? "Dark" : "Light"));
        ((Button) findViewById(R.id.btnToggleWallpaper)).setText(
                "Wallpaper: " + (settings.getWallpaperIndex() + 1) + " / " + LauncherSettings.WALLPAPER_COUNT);
        ((Button) findViewById(R.id.btnToggleIconSize)).setText(
                "Icon Size: " + settings.getIconSizeLabel());
        ((Button) findViewById(R.id.btnToggleSort)).setText(
                "Sort: " + (settings.isAlphabetical() ? "A-Z" : "Custom"));
        ((Button) findViewById(R.id.btnToggleAutoStandby)).setText(
                "Auto Standby: " + (settings.isStandbyTimerEnabled() ? "On" : "Off"));
    }

    private void notifyChanged() {
        refreshLabels();
        if (onChanged != null) onChanged.onChange();
    }
}
