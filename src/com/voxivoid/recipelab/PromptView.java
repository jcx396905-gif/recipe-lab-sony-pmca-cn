package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Modal question: title, one explanation line, option pills, icon legend. Canvas-drawn. */
public class PromptView extends View {
    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208;
    private static final int[] LEGEND_ICONS = { Legend.ENTER, Legend.MENU };
    private static final String[] LEGEND_TEXT = { "确认", "取消" };

    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG), edge = new Paint(Paint.ANTI_ALIAS_FLAG), title = new Paint(Paint.ANTI_ALIAS_FLAG),
            body = new Paint(Paint.ANTI_ALIAS_FLAG), opt = new Paint(Paint.ANTI_ALIAS_FLAG), pill = new Paint(Paint.ANTI_ALIAS_FLAG), note = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Legend legend;
    private final float d;
    private String titleText = "", bodyText = "", noteText = null;
    private String[] options = new String[0];
    private int selected = 0;

    public PromptView(Context c, AttributeSet a) {
        super(c, a);
        d = c.getResources().getDisplayMetrics().density;
        legend = new Legend(c, d);
        Cn.apply(c, title, body, opt, note);
        bg.setColor(0xF0141414);
        edge.setColor(0x88F2B85C); edge.setStyle(Paint.Style.STROKE); edge.setStrokeWidth(d);
        title.setColor(0xFFFFFFFF); title.setTextSize(15 * d); title.setFakeBoldText(true);
        body.setColor(0xCCFFFFFF); body.setTextSize(12 * d);
        opt.setTextSize(13 * d); opt.setFakeBoldText(true); opt.setTextAlign(Paint.Align.CENTER);
        note.setColor(0x88FFFFFF); note.setTextSize(10 * d);
    }

    public void set(String titleText, String bodyText, String[] options, int selected, String noteText) {
        this.titleText = titleText; this.bodyText = bodyText; this.options = options; this.selected = selected; this.noteText = noteText;
        invalidate();
    }

    @Override
    protected void onMeasure(int w, int hh) {
        float wd = Math.max(title.measureText(titleText), body.measureText(bodyText)) + 40 * d;
        float ow = 0; for (String o : options) ow += opt.measureText(o) + 36 * d;
        wd = Math.max(wd, ow + 20 * d);
        wd = Math.min(wd, MeasureSpec.getSize(w));
        float h = 14 * d + 20 * d + 18 * d + 12 * d + 30 * d + 14 * d + (noteText != null ? 14 * d : 0) + legend.height() + 12 * d;
        setMeasuredDimension((int) wd, (int) h);
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), pad = 16 * d;
        r.set(0, 0, w, h); c.drawRoundRect(r, 8 * d, 8 * d, bg); c.drawRoundRect(r, 8 * d, 8 * d, edge);
        float y = 14 * d + 15 * d;
        c.drawText(titleText, pad, y, title); y += 18 * d;
        c.drawText(bodyText, pad, y, body); y += 12 * d;

        // option pills, centred
        float total = 0; for (String o : options) total += opt.measureText(o) + 28 * d;
        total += (options.length - 1) * 8 * d;
        float x = (w - total) / 2, ph = 24 * d, py = y + 3 * d;
        for (int i = 0; i < options.length; i++) {
            float pw = opt.measureText(options[i]) + 28 * d;
            r.set(x, py, x + pw, py + ph);
            pill.setColor(i == selected ? ACCENT : 0x33FFFFFF);
            c.drawRoundRect(r, 5 * d, 5 * d, pill);
            opt.setColor(i == selected ? INK : 0xFFFFFFFF);
            c.drawText(options[i], x + pw / 2, py + ph / 2 - (opt.ascent() + opt.descent()) / 2, opt);
            x += pw + 8 * d;
        }
        y = py + ph + 14 * d;
        if (noteText != null) {
            float ns = 10 * d, avail = w - 2 * pad;
            while (note.measureText(noteText) > avail && ns > 7 * d) { ns -= 0.5f * d; note.setTextSize(ns); }
            c.drawText(noteText, pad, y, note); note.setTextSize(10 * d); y += 14 * d;
        }
        legend.draw(c, pad, y + legend.height() / 2 - 2 * d, w - 2 * pad, LEGEND_ICONS, LEGEND_TEXT);
    }
}
