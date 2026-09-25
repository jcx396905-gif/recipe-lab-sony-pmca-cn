package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/** Key legend under the main panel. */
public class HintBar extends View {
    public static final int RECIPE = 0, CHIPS = 1, EDIT = 2;
    private static final int[][] ICONS = {
        { Legend.FN, Legend.ENTER, Legend.ENTER, Legend.TRASH, Legend.AEL, Legend.MENU },
        { Legend.ENTER, Legend.FN, Legend.TRASH, Legend.AEL, Legend.MENU },
        { Legend.ENTER } };
    private static final String[][] TEXT = {
        { "浏览", "选定", "收藏(长按)", "出厂", "隐藏", "退出" },
        { "编辑", "浏览", "出厂", "隐藏", "退出" },
        { "完成" } };

    private final Legend legend;
    private int mode = RECIPE;

    public HintBar(Context c, AttributeSet a) {
        super(c, a);
        legend = new Legend(c, c.getResources().getDisplayMetrics().density);
    }

    public void setMode(int m) { if (mode != m) { mode = m; invalidate(); } }

    @Override
    protected void onMeasure(int w, int h) { setMeasuredDimension(MeasureSpec.getSize(w), (int) legend.height()); }

    @Override
    protected void onDraw(Canvas c) {
        legend.draw(c, 0, getHeight() / 2f, getWidth(), ICONS[mode], TEXT[mode]);
    }
}
