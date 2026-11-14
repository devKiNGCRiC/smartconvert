package com.unitconverter.app.presentation.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.unitconverter.app.R;

/**
 * Repeats a small vector tile (jaali, block-print buti, truck-art diamond)
 * across its bounds, tinted and faded. Purely decorative.
 *
 * XML: app:pattern="?attr/scPattern" app:patternTint="?attr/scOnHero"
 *      app:patternAlpha="0.12" app:patternFade="1"
 */
public class PatternView extends View {

    public static final int FADE_NONE = 0;
    public static final int FADE_DOWN = 1;
    public static final int FADE_END = 2;

    private Drawable tile;
    private int tileSize;
    private int fade;
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();
    private ShapeAppearanceModel clipShape;
    private boolean sheen;

    public PatternView(Context context) {
        this(context, null);
    }

    public PatternView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float dp = getResources().getDisplayMetrics().density;
        tileSize = Math.round(28 * dp);
        int tint = 0xFFFFFFFF;
        float alpha = 0.12f;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PatternView);
            int res = a.getResourceId(R.styleable.PatternView_pattern, 0);
            if (res != 0) tile = AppCompatResources.getDrawable(context, res);
            tileSize = a.getDimensionPixelSize(R.styleable.PatternView_patternSize, tileSize);
            tint = a.getColor(R.styleable.PatternView_patternTint, tint);
            alpha = a.getFloat(R.styleable.PatternView_patternAlpha, alpha);
            fade = a.getInt(R.styleable.PatternView_patternFade, FADE_NONE);
            a.recycle();
        }
        if (tile != null) {
            tile = DrawableCompat.wrap(tile.mutate());
            DrawableCompat.setTint(tile, tint);
            // Transparency lives on the tile, so the sheen can draw at full strength
            tile.setAlpha(Math.round(Math.max(0f, Math.min(1f, alpha)) * 255));
        }
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void setTint(int color) {
        if (tile != null) {
            DrawableCompat.setTint(tile, color);
            invalidate();
        }
    }

    /**
     * Clips the pattern (and sheen) to a Material shape, so it can run
     * edge to edge inside an arch- or plaque-shaped card.
     */
    public void setClipShape(@Nullable ShapeAppearanceModel shape) {
        clipShape = shape;
        updateClip(getWidth(), getHeight());
        invalidate();
    }

    /** Adds the clay highlight: light from the top-left, a little shade bottom-right. */
    public void setSheen(boolean on) {
        sheen = on;
        updateSheen(getWidth(), getHeight());
        invalidate();
    }

    private void updateClip(int w, int h) {
        clipPath.reset();
        if (clipShape != null && w > 0 && h > 0) {
            ShapeAppearancePathProvider.getInstance()
                    .calculatePath(clipShape, 1f, new RectF(0, 0, w, h), clipPath);
        }
    }

    private void updateSheen(int w, int h) {
        if (w > 0 && h > 0) {
            sheenPaint.setShader(new LinearGradient(0, 0, w, h,
                    new int[]{0x40FFFFFF, 0x08FFFFFF, 0x24000000}, new float[]{0f, 0.55f, 1f},
                    Shader.TileMode.CLAMP));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        updateClip(w, h);
        updateSheen(w, h);
        if (fade == FADE_DOWN) {
            maskPaint.setShader(new LinearGradient(0, 0, 0, h, 0xFF000000, 0x00000000, Shader.TileMode.CLAMP));
        } else if (fade == FADE_END) {
            maskPaint.setShader(new LinearGradient(0, 0, w, 0, 0xFF000000, 0x00000000, Shader.TileMode.CLAMP));
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (tile == null || getWidth() == 0) return;
        int clipSave = -1;
        if (!clipPath.isEmpty()) {
            clipSave = canvas.save();
            canvas.clipPath(clipPath);
        }
        int saved = fade != FADE_NONE ? canvas.saveLayer(0, 0, getWidth(), getHeight(), null) : -1;

        for (int y = 0; y < getHeight(); y += tileSize) {
            for (int x = 0; x < getWidth(); x += tileSize) {
                tile.setBounds(x, y, x + tileSize, y + tileSize);
                tile.draw(canvas);
            }
        }
        if (saved >= 0) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), maskPaint);
            canvas.restoreToCount(saved);
        }
        if (sheen) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), sheenPaint);
        }
        if (clipSave >= 0) canvas.restoreToCount(clipSave);
    }
}
