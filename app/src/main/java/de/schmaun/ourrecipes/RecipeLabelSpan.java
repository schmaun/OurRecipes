package de.schmaun.ourrecipes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;

public class RecipeLabelSpan extends ReplacementSpan implements LineHeightSpan {
    private static int CORNER_RADIUS = 30;
    private int lineHeight;
    private int backgroundColor = 0;
    private int textColor = 0;

    public RecipeLabelSpan(int lineHeight, int textColor, int backgroundColor) {
        super();
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.lineHeight = lineHeight;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        /*RectF rect = new RectF(x, top, x + measureText(paint, text, start, end), bottom);
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(rect, CORNER_RADIUS, CORNER_RADIUS, paint);
        paint.setColor(textColor);
        canvas.drawText(text, start, end, x, y, paint);
        */

        final float textSize = paint.getTextSize();
        final float textLength = x + measureText(paint, text, start, end);
        final float badgeHeight = textSize * 2.25f;
        final float textOffsetVertical = textSize * 1.45f;

        RectF badge = new RectF(x, y, textLength, y + badgeHeight);
        paint.setColor(backgroundColor);
        canvas.drawRoundRect(badge, CORNER_RADIUS, CORNER_RADIUS, paint);

        paint.setColor(textColor);
        canvas.drawText(text, start, end, x, y + textOffsetVertical, paint);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end));
    }

    private float measureText(Paint paint, CharSequence text, int start, int end) {
        return paint.measureText(text, start, end);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        fm.bottom += lineHeight;
        fm.descent += 5;
    }
}