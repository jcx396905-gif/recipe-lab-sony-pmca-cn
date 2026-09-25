package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/** A filled star next to the recipe name: the recipe is a favourite. Canvas-drawn (the camera font has no star glyph). */
public class StarView extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;

    public StarView(Context c, AttributeSet a) {
        super(c, a);
        d = c.getResources().getDisplayMetrics().density;
        fill.setColor(0xFFF2B85C);
    }

    @Override
    protected void onMeasure(int w, int h) { setMeasuredDimension((int) (16 * d), (int) (16 * d)); }

    @Override
    protected void onDraw(Canvas c) { Legend.star(c, getWidth() / 2f, getHeight() / 2f, 7 * d, fill); }
}
