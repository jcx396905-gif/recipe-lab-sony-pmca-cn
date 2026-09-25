package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * The bundled CJK font (a subset of DroidSansFallback, Apache-2.0). The camera firmware's system font has no
 * Chinese glyphs — every TextView and every Canvas text Paint gets this typeface so the Chinese UI renders.
 */
final class Cn {
    private Cn() {}

    private static Typeface t;

    /** the bundled font, or the system default if it could not be loaded */
    static Typeface font(Context c) {
        if (t == null) { try { t = Typeface.createFromAsset(c.getAssets(), "cn.ttf"); } catch (Throwable e) {} }
        return t != null ? t : Typeface.DEFAULT;
    }

    static void apply(Context c, TextView v) { if (v != null) v.setTypeface(font(c)); }

    static void apply(Context c, Paint... ps) {
        Typeface f = font(c);
        for (Paint p : ps) if (p != null) p.setTypeface(f);
    }
}
