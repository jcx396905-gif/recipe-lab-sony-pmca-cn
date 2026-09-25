package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Key legend drawn with Canvas (camera firmware font has no arrow / symbol glyphs).
 * Fits the available width: first squeezes the gaps between items, then scales icons and text down.
 */
public class Legend {
    public static final int WHEEL = 0, UPDOWN = 1, LEFTRIGHT = 2, DIAL = 3, ENTER = 4, AEL = 5, TRASH = 6, MENU = 7, C1 = 8, FN = 9;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG), stroke = new Paint(Paint.ANTI_ALIAS_FLAG),
            text = new Paint(Paint.ANTI_ALIAS_FLAG), keyText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final Canvas nowhere = new Canvas();      // measuring pass draws into this
    private final float d;

    public Legend(Context c, float density) {
        d = density;
        fill.setColor(0xCCFFFFFF); fill.setStyle(Paint.Style.FILL);
        stroke.setColor(0xCCFFFFFF); stroke.setStyle(Paint.Style.STROKE);
        text.setColor(0x99FFFFFF);
        keyText.setColor(0xCCFFFFFF); keyText.setTextAlign(Paint.Align.CENTER); keyText.setFakeBoldText(true);
        Cn.apply(c, text, keyText);
    }

    private static final Path STAR = new Path();

    /** a five-point star centred on (cx, cy) with outer radius r — the favourite mark, shared by every view that draws one */
    public static void star(Canvas c, float cx, float cy, float r, Paint p) {
        STAR.reset();
        for (int i = 0; i < 10; i++) {
            double ang = Math.toRadians(-90 + i * 36);
            float rr = (i % 2 == 0) ? r : r * 0.45f;
            float x = cx + (float) Math.cos(ang) * rr, y = cy + (float) Math.sin(ang) * rr;
            if (i == 0) STAR.moveTo(x, y); else STAR.lineTo(x, y);
        }
        STAR.close();
        c.drawPath(STAR, p);
    }

    /** natural height for a legend row at scale 1 */
    public float height() { return 16 * d; }

    /** draws icons+labels starting at x, vertically centred on cy, within width; returns the width actually used */
    public float draw(Canvas c, float x, float cy, float width, int[] icons, String[] labels) {
        float scale = 1f, gap = 14 * d, minGap = 5 * d;
        float need = measure(scale, minGap, icons, labels);
        if (need > width) scale = Math.max(0.6f, width / need);          // shrink everything, gaps stay minimal
        float used = measure(scale, minGap, icons, labels);
        if (used < width) gap = Math.min(14 * d, minGap + (width - used) / Math.max(1, icons.length - 1));
        else gap = minGap;
        setScale(scale);
        float s = 6 * d * scale, ty = cy - (text.ascent() + text.descent()) / 2f, x0 = x;
        for (int i = 0; i < icons.length; i++) {
            x += icon(c, icons[i], x, cy, s) + 4 * d * scale;
            c.drawText(labels[i], x, ty, text);
            x += text.measureText(labels[i]) + (i < icons.length - 1 ? gap : 0);
        }
        return x - x0;
    }

    private float measure(float scale, float gap, int[] icons, String[] labels) {
        setScale(scale);
        float s = 6 * d * scale, w = 0;
        for (int i = 0; i < icons.length; i++) w += icon(nowhere, icons[i], 0, 0, s) + 4 * d * scale + text.measureText(labels[i]) + (i < icons.length - 1 ? gap : 0);
        return w;
    }

    private void setScale(float k) {
        text.setTextSize(10 * d * k);
        keyText.setTextSize(6.5f * d * k);
        stroke.setStrokeWidth(1.2f * d * k);
    }

    private void tri(Canvas c, float x1, float y1, float x2, float y2, float x3, float y3) {
        path.reset(); path.moveTo(x1, y1); path.lineTo(x2, y2); path.lineTo(x3, y3); path.close(); c.drawPath(path, fill);
    }

    private float keyLabel(Canvas c, float x, float cy, float s, float w, String label) {
        rect.set(x, cy - s * 0.8f, x + w, cy + s * 0.8f);
        c.drawRoundRect(rect, 2 * d, 2 * d, stroke);
        c.drawText(label, x + w / 2, cy - (keyText.ascent() + keyText.descent()) / 2f, keyText);
        return w;
    }

    /** draws icon with left edge at x, vertically centred on cy, half-size s; returns width */
    private float icon(Canvas c, int icon, float x, float cy, float s) {
        float a = s * 0.55f, k = s / (6 * d);                     // a = arrow size, k = scale
        switch (icon) {
            case WHEEL: {                                          // ring + left/right arrows outside
                float cx = x + a + s + 1.5f * d * k;
                c.drawCircle(cx, cy, s * 0.8f, stroke);
                c.drawCircle(cx, cy, s * 0.25f, fill);
                tri(c, x, cy, x + a, cy - a * 0.8f, x + a, cy + a * 0.8f);
                float r = cx + s + 1.5f * d * k;
                tri(c, r + a, cy, r, cy - a * 0.8f, r, cy + a * 0.8f);
                return 2 * a + 2 * s + 3 * d * k;
            }
            case DIAL: {                                           // top dial: arc with ticks + arrows
                float cx = x + a + s + 1.5f * d * k;
                rect.set(cx - s, cy - s * 0.4f, cx + s, cy + s * 1.6f);
                c.drawArc(rect, 200, 140, false, stroke);
                for (int i = -2; i <= 2; i++) {
                    double ang = Math.toRadians(270 + i * 28);
                    float ox = (float) Math.cos(ang), oy = (float) Math.sin(ang), ccy = cy + s * 0.6f;
                    c.drawLine(cx + ox * s * 0.75f, ccy + oy * s * 0.75f, cx + ox * s, ccy + oy * s, stroke);
                }
                tri(c, x, cy, x + a, cy - a * 0.8f, x + a, cy + a * 0.8f);
                float r = cx + s + 1.5f * d * k;
                tri(c, r + a, cy, r, cy - a * 0.8f, r, cy + a * 0.8f);
                return 2 * a + 2 * s + 3 * d * k;
            }
            case UPDOWN: {
                float cx = x + a;
                tri(c, cx, cy - s, cx - a * 0.8f, cy - s + a, cx + a * 0.8f, cy - s + a);
                tri(c, cx, cy + s, cx - a * 0.8f, cy + s - a, cx + a * 0.8f, cy + s - a);
                return 2 * a;
            }
            case LEFTRIGHT: {
                tri(c, x, cy, x + a, cy - a * 0.8f, x + a, cy + a * 0.8f);
                float r = x + a + 3 * d * k;
                tri(c, r + a, cy, r, cy - a * 0.8f, r, cy + a * 0.8f);
                return 2 * a + 3 * d * k;
            }
            case ENTER: {                                          // centre button: ring + dot
                float cx = x + s;
                c.drawCircle(cx, cy, s * 0.85f, stroke);
                c.drawCircle(cx, cy, s * 0.4f, fill);
                return 2 * s;
            }
            case AEL: return keyLabel(c, x, cy, s, 2.6f * s, "AEL");
            case C1: return keyLabel(c, x, cy, s, 2.0f * s, "C1");
            case FN: return keyLabel(c, x, cy, s, 2.0f * s, "Fn");
            case TRASH: {                                          // bin: lid + body
                float w = 1.6f * s, cx = x + w / 2, top = cy - s * 0.9f, bot = cy + s * 0.9f, u = d * k;
                c.drawLine(x, top + 2 * u, x + w, top + 2 * u, stroke);
                c.drawLine(cx - 2 * u, top, cx + 2 * u, top, stroke);
                rect.set(x + 2 * u, top + 2 * u, x + w - 2 * u, bot);
                c.drawRoundRect(rect, 1.5f * u, 1.5f * u, stroke);
                c.drawLine(cx, top + 5 * u, cx, bot - 3 * u, stroke);
                return w;
            }
            case MENU: {                                           // rounded key with three lines
                float u = d * k;
                rect.set(x, cy - s * 0.8f, x + 2 * s, cy + s * 0.8f);
                c.drawRoundRect(rect, 2 * u, 2 * u, stroke);
                for (int i = -1; i <= 1; i++) c.drawLine(x + 3.5f * u, cy + i * 3 * u, x + 2 * s - 3.5f * u, cy + i * 3 * u, stroke);
                return 2 * s;
            }
        }
        return 0;
    }
}
