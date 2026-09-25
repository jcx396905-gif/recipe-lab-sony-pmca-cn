package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Two-column recipe browser: groups left, recipes of the highlighted group right. Canvas-drawn.
 * The first group is Favourites (the marked recipes, in marking order); the rest are the brands.
 */
public class PickerView extends View {
    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208;

    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG), edge = new Paint(Paint.ANTI_ALIAS_FLAG), sel = new Paint(Paint.ANTI_ALIAS_FLAG),
            head = new Paint(Paint.ANTI_ALIAS_FLAG), item = new Paint(Paint.ANTI_ALIAS_FLAG), small = new Paint(Paint.ANTI_ALIAS_FLAG), rule = new Paint(),
            track = new Paint(Paint.ANTI_ALIAS_FLAG), thumb = new Paint(Paint.ANTI_ALIAS_FLAG), tagBg = new Paint(Paint.ANTI_ALIAS_FLAG),
            star = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final float d;
    private final Legend legend;
    private static final int[] BRAND_ICONS = { Legend.ENTER, Legend.FN };
    private static final String[] BRAND_TEXT = { "配方", "关闭" };
    private static final int[] RECIPE_ICONS = { Legend.ENTER, Legend.ENTER, Legend.FN };
    private static final String[] RECIPE_TEXT = { "选定", "收藏(长按)", "关闭" };
    private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selected = 0, column = 1, group = 0;              // column: 0 groups, 1 recipes · group: Favourites.GROUP or a brand
    private List<Integer> favs = new ArrayList<Integer>();

    public PickerView(Context c, AttributeSet a) {
        super(c, a);
        d = c.getResources().getDisplayMetrics().density;
        legend = new Legend(c, d);
        Cn.apply(c, head, item, small);
        bg.setColor(0xF0101010);
        edge.setColor(0x66F2B85C); edge.setStyle(Paint.Style.STROKE); edge.setStrokeWidth(d);
        sel.setColor(ACCENT);
        outline.setColor(ACCENT); outline.setStyle(Paint.Style.STROKE); outline.setStrokeWidth(1.5f * d);
        head.setColor(0x99FFFFFF); head.setTextSize(9 * d); head.setFakeBoldText(true);
        item.setColor(0xFFFFFFFF); item.setTextSize(13 * d);
        small.setColor(0x99FFFFFF); small.setTextSize(10 * d);
        rule.setColor(0x33FFFFFF);
        track.setColor(0x26FFFFFF); thumb.setColor(0xCCF2B85C);
    }

    /** the highlighted recipe, the active column, the group the left column is on, and the favourites in marking order */
    public void set(int recipe, int col, int grp, List<Integer> favourites) { selected = recipe; column = col; group = grp; favs = favourites; invalidate(); }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), pad = 12 * d;
        c.drawRect(0, 0, w, h, bg);

        int g = group;
        boolean favGroup = g == Favourites.GROUP;
        int count = Favourites.groupCount(g, favs);
        float colX = w * 0.30f;                                 // divider
        float top = pad + 12 * d, bottom = h - pad - 20 * d;    // header / footer reserved
        float sbW = 4 * d;                                      // scrollbar width
        head.setColor(column == 0 ? ACCENT : 0x99FFFFFF);
        c.drawText("品牌", pad, pad + 7 * d, head);
        head.setColor(column == 1 ? ACCENT : 0x99FFFFFF);
        c.drawText(Favourites.groupName(g).toUpperCase() + "  ·  " + count, colX + pad, pad + 7 * d, head);
        head.setColor(0x99FFFFFF);
        c.drawLine(colX, pad, colX, h - pad, rule);
        c.drawLine(pad, top + 3 * d, w - pad, top + 3 * d, rule);

        // ---- left: Favourites, then the brands
        int ng = Recipes.GROUPS.length + 1, gRow = Favourites.groupRow(g);
        float listTop = top + 6 * d, listH = bottom - listTop;
        float rowH = 24 * d;
        int gVisible = Math.max(1, (int) (listH / rowH));
        int gFirst = ng > gVisible ? Math.max(0, Math.min(gRow - gVisible / 2, ng - gVisible)) : 0;
        float gRight = colX - 8 * d - (ng > gVisible ? sbW + 4 * d : 0);
        float y = listTop;
        for (int i = gFirst; i < Math.min(ng, gFirst + gVisible); i++, y += rowH) {
            int gi = i - 1;                                     // row 0 is Favourites.GROUP
            boolean on = i == gRow, active = on && column == 0;
            if (on) { r.set(pad - 4 * d, y, gRight, y + rowH); c.drawRoundRect(r, 3 * d, 3 * d, active ? sel : outline); }
            item.setColor(active ? INK : on ? ACCENT : gi == Favourites.GROUP ? 0xFFF2B85C : 0xCCFFFFFF); item.setFakeBoldText(on);
            float tx = pad;
            if (gi == Favourites.GROUP) { star.setColor(active ? INK : ACCENT); Legend.star(c, pad + 5 * d, y + rowH / 2, 5.5f * d, star); tx += 14 * d; }
            c.drawText(Favourites.groupName(gi), tx, y + rowH / 2 + item.getTextSize() * 0.36f, item);
            small.setColor(active ? 0xAA1A1208 : 0x66FFFFFF);
            String n = String.valueOf(Favourites.groupCount(gi, favs));
            c.drawText(n, gRight - 6 * d - small.measureText(n), y + rowH / 2 + small.getTextSize() * 0.36f, small);
            if (i == 0) c.drawLine(pad, y + rowH - d, gRight, y + rowH - d, rule);   // Favourites is set apart from the brands
        }
        item.setFakeBoldText(false);
        if (ng > gVisible) scrollbar(c, colX - 6 * d - sbW, listTop, listH, sbW, gFirst, gVisible, ng);

        // ---- right: the group's recipes, windowed around the highlight
        float x = colX + pad;
        if (count == 0) {                                       // an empty Favourites group says so, and how to fill it
            item.setColor(0xCCFFFFFF);
            c.drawText(Favourites.EMPTY_TITLE, x, listTop + 20 * d, item);
            small.setColor(0x99FFFFFF);
            c.drawText(Favourites.EMPTY_HINT, x, listTop + 36 * d, small);
        } else {
            float rh = 26 * d;
            int visible = Math.max(1, (int) (listH / rh));
            int selPos = Math.max(0, Favourites.positionIn(g, selected, favs));
            int first = 0;
            boolean scroll = count > visible;
            if (scroll) { first = Math.max(0, Math.min(selPos - visible / 2, count - visible)); }
            float xr = w - pad - (scroll ? sbW + 6 * d : 0);
            y = listTop;
            for (int k = first; k < Math.min(count, first + visible); k++, y += rh) {
                int idx = Favourites.recipeAt(g, k, favs); Recipes.Recipe rc = Recipes.ALL[idx];
                boolean on = idx == selected, active = on && column == 1;
                if (on) { r.set(x - 4 * d, y, xr, y + rh); c.drawRoundRect(r, 3 * d, 3 * d, active ? sel : outline); }
                item.setColor(active ? INK : on ? ACCENT : 0xFFFFFFFF); item.setFakeBoldText(on);
                c.drawText(rc.name, x, y + 13 * d, item);
                small.setColor(active ? 0xAA1A1208 : 0x80FFFFFF);
                c.drawText(favGroup ? Recipes.GROUPS[rc.group] + "  ·  " + rc.summary() : rc.summary(), x, y + 22 * d, small);
                float tx = tag(c, rc.isEffect() ? "PE" : "CS", xr - 4 * d, y, active, active ? 0x331A1208 : (rc.isEffect() ? 0x55B8741A : 0x33FFFFFF), active ? INK : 0xCCFFFFFF);
                if (!favGroup && favs.contains(idx)) { star.setColor(active ? INK : ACCENT); Legend.star(c, tx - 4 * d - 6 * d, y + 11 * d, 6 * d, star); }
            }
            item.setFakeBoldText(false);
            if (scroll) scrollbar(c, w - pad - sbW, listTop, listH, sbW, first, visible, count);
        }

        // ---- footer: icon legend
        c.drawLine(pad, h - pad - 16 * d, w - pad, h - pad - 16 * d, rule);
        legend.draw(c, pad, h - pad - 6 * d, w - 2 * pad, column == 0 ? BRAND_ICONS : RECIPE_ICONS, column == 0 ? BRAND_TEXT : RECIPE_TEXT);
    }

    /** a small pill ending at {@code right} on the row at {@code y}; returns its left edge */
    private float tag(Canvas c, String text, float right, float y, boolean active, int bgColor, int textColor) {
        float tw = head.measureText(text) + 8 * d, tx = right - tw;
        r.set(tx, y + 5 * d, tx + tw, y + 17 * d);
        tagBg.setColor(bgColor);
        c.drawRoundRect(r, 2 * d, 2 * d, tagBg);
        head.setColor(textColor);
        c.drawText(text, tx + 4 * d, y + 14 * d, head);
        head.setColor(0x99FFFFFF);
        return tx;
    }

    /** vertical scrollbar: track + thumb proportional to the visible window */
    private void scrollbar(Canvas c, float x, float top, float height, float width, int first, int visible, int total) {
        r.set(x, top, x + width, top + height); c.drawRoundRect(r, width / 2, width / 2, track);
        float thumbH = Math.max(12 * d, height * visible / total);
        float thumbY = top + (height - thumbH) * first / Math.max(1, total - visible);
        r.set(x, thumbY, x + width, thumbY + thumbH); c.drawRoundRect(r, width / 2, width / 2, thumb);
    }
}
