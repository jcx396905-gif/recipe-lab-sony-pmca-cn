package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Modal list: title, one row per tool (name + what it does), icon legend. Canvas-drawn, like PromptView. */
public class MenuView extends View {
    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208;
    private static final int[] LEGEND_ICONS = { Legend.UPDOWN, Legend.ENTER, Legend.MENU };
    private static final String[] LEGEND_TEXT = { "移动", "选择", "关闭" };

    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG), edge = new Paint(Paint.ANTI_ALIAS_FLAG), head = new Paint(Paint.ANTI_ALIAS_FLAG),
            item = new Paint(Paint.ANTI_ALIAS_FLAG), small = new Paint(Paint.ANTI_ALIAS_FLAG), row = new Paint(Paint.ANTI_ALIAS_FLAG),
            rule = new Paint();
    private final RectF r = new RectF();
    private final Legend legend;
    private final float d;
    private String[] labels = new String[0], details = new String[0];
    private int selected = 0;

    public MenuView(Context c, AttributeSet a) {
        super(c, a);
        d = c.getResources().getDisplayMetrics().density;
        legend = new Legend(c, d);
        Cn.apply(c, head, item, small);
        bg.setColor(0xF0141414);
        edge.setColor(0x88F2B85C); edge.setStyle(Paint.Style.STROKE); edge.setStrokeWidth(d);
        head.setColor(ACCENT); head.setTextSize(9 * d); head.setFakeBoldText(true);
        item.setTextSize(13 * d); item.setFakeBoldText(true);
        small.setTextSize(10 * d);
        rule.setColor(0x33FFFFFF);
    }

    /** the rows, their explanation lines, and which one is highlighted */
    public void set(String[] labels, String[] details, int selected) {
        this.labels = labels; this.details = details; this.selected = selected;
        requestLayout(); invalidate();
    }

    private float rowHeight() { return 34 * d; }

    @Override
    protected void onMeasure(int w, int hh) {
        float wd = head.measureText(DevTools.TITLE);
        for (int i = 0; i < labels.length; i++) wd = Math.max(wd, Math.max(item.measureText(labels[i]), small.measureText(details[i])));
        wd = Math.min(wd + 40 * d, MeasureSpec.getSize(w));
        float h = 14 * d + 12 * d + labels.length * rowHeight() + 12 * d + legend.height() + 12 * d;
        setMeasuredDimension((int) wd, (int) h);
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), pad = 16 * d;
        r.set(0, 0, w, h); c.drawRoundRect(r, 8 * d, 8 * d, bg); c.drawRoundRect(r, 8 * d, 8 * d, edge);
        c.drawText(DevTools.TITLE, pad, 14 * d + 7 * d, head);

        float y = 14 * d + 12 * d, rh = rowHeight();
        for (int i = 0; i < labels.length; i++, y += rh) {
            boolean on = i == selected;
            if (on) { r.set(pad - 6 * d, y, w - pad + 6 * d, y + rh - 2 * d); row.setColor(ACCENT); c.drawRoundRect(r, 4 * d, 4 * d, row); }
            item.setColor(on ? INK : 0xFFFFFFFF);
            c.drawText(labels[i], pad, y + 15 * d, item);
            small.setColor(on ? 0xCC1A1208 : 0x99FFFFFF);
            c.drawText(details[i], pad, y + 27 * d, small);
        }
        c.drawLine(pad, y + 2 * d, w - pad, y + 2 * d, rule);
        legend.draw(c, pad, y + 12 * d + legend.height() / 2 - 2 * d, w - 2 * pad, LEGEND_ICONS, LEGEND_TEXT);
    }
}
