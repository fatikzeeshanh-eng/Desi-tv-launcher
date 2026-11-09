package com.indiatv.launcher;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;

/**
 * tvOS-style focus visuals: focused cards zoom up smoothly
 * (like Apple TV) and get a white focus ring.
 */
final class FocusEffects {

    private FocusEffects() {}

    static int dp(Context c, int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    static GradientDrawable cardBackground(Context c, boolean dark, boolean focused) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(c, 12));
        if (focused) {
            // Apple TV focus: brighter card + white ring
            d.setColor(dark ? 0xFF3A3A3C : 0xFFF2F2F7);
            d.setStroke(dp(c, 2), 0xFFFFFFFF);
        } else {
            d.setColor(dark ? 0x801C1C1E : 0x99FFFFFF);
            d.setStroke(dp(c, 1), dark ? 0x33FFFFFF : 0x22000000);
        }
        return d;
    }

    static void attach(final Context c, final View card, final boolean darkTheme) {
        card.setBackgroundDrawable(cardBackground(c, darkTheme, false));
        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                v.setBackgroundDrawable(cardBackground(c, darkTheme, hasFocus));
                v.animate()
                        .scaleX(hasFocus ? 1.12f : 1.0f)
                        .scaleY(hasFocus ? 1.12f : 1.0f)
                        .setDuration(150L)
                        .start();
            }
        });
    }
}
