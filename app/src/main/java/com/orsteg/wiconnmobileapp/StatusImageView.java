package com.orsteg.wiconnmobileapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.CENTER_INSIDE;

/**
 * Copyright (C) 2018 Mikhael LOPEZ
 * Licensed under the Apache License Version 2.0
 */
public class StatusImageView extends AppCompatImageView {

    // Default Values
    private static final float DEFAULT_BORDER_WIDTH = 2;
    private static final float DEFAULT_SHADOW_RADIUS = 8.0f;

    // Properties
    private float borderWidth;
    private int canvasSize;
    private float shadowRadius;
    private int shadowColor = Color.BLACK;
    private ShadowGravity shadowGravity = ShadowGravity.BOTTOM;
    private ColorFilter colorFilter;

    // Object used to draw
    private Bitmap image;
    private Drawable drawable;
    private Paint paint;
    private Paint paintBackground;

    // Status properties
    private int mStatusCount;
    private int mViewCount;
    private Paint activePaintBorder;
    private Paint inactivePaintBorder;
    private RectF mRect;
    private float mMargin;
    private float mStroke;
    private float mSpace;

    //region Constructor & Init Method
    public StatusImageView(final Context context) {
        this(context, null);
    }

    public StatusImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        // Init paint
        paint = new Paint();
        paint.setAntiAlias(true);

        activePaintBorder = new Paint();
        activePaintBorder.setAntiAlias(true);
        activePaintBorder.setStyle(Paint.Style.STROKE);
        activePaintBorder.setStrokeCap(Paint.Cap.ROUND);

        inactivePaintBorder = new Paint();
        inactivePaintBorder.setAntiAlias(true);
        inactivePaintBorder.setStyle(Paint.Style.STROKE);
        inactivePaintBorder.setStrokeCap(Paint.Cap.ROUND);

        mSpace = 10f;
        mRect = new RectF();
        paintBackground = new Paint();
        paintBackground.setAntiAlias(true);

        // Load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.StatusImageView, defStyleAttr, 0);

        mStatusCount = attributes.getInteger(R.styleable.StatusImageView_civ_status_count, 2);
        mViewCount = attributes.getInteger(R.styleable.StatusImageView_civ_view_count, 1);

        float defaultBorderSize = DEFAULT_BORDER_WIDTH * getContext().getResources().getDisplayMetrics().density;

        mMargin = defaultBorderSize;
        // Init Border
        if (attributes.getBoolean(R.styleable.StatusImageView_civ_border, true)) {
            setBorderWidth(attributes.getDimension(R.styleable.StatusImageView_civ_border_width, defaultBorderSize));

            setBorderColors(attributes.getColor(R.styleable.StatusImageView_civ_active_border_color, Color.BLUE),
                    attributes.getColor(R.styleable.StatusImageView_civ_inactive_border_color, Color.GRAY));
        }

        setBackgroundColor(attributes.getColor(R.styleable.StatusImageView_civ_background_color, Color.WHITE));

        // Init Shadow
        if (attributes.getBoolean(R.styleable.StatusImageView_civ_shadow, false)) {
            shadowRadius = DEFAULT_SHADOW_RADIUS;
            drawShadow(attributes.getFloat(R.styleable.StatusImageView_civ_shadow_radius, shadowRadius),
                    attributes.getColor(R.styleable.StatusImageView_civ_shadow_color, shadowColor));
            int shadowGravityIntValue = attributes.getInteger(R.styleable.StatusImageView_civ_shadow_grav, ShadowGravity.BOTTOM.getValue());
            shadowGravity = ShadowGravity.fromValue(shadowGravityIntValue);
        }

        attributes.recycle();
    }
    //endregion

    //region Set Attr Method
    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth + mMargin;
        this.mStroke = borderWidth;

        activePaintBorder.setStrokeWidth(borderWidth);
        inactivePaintBorder.setStrokeWidth(borderWidth);

        requestLayout();
        invalidate();
    }

    public void setInactiveBorderColor(int borderColor) {
        if (inactivePaintBorder != null)
            inactivePaintBorder.setColor(borderColor);
        invalidate();
    }

    public void setactiveBorderColor(int borderColor) {
        if (activePaintBorder != null)
            activePaintBorder.setColor(borderColor);
        invalidate();
    }

    public void setBorderColors(int activeBorderColor, int inactiveBorderColor) {
        if (inactivePaintBorder != null)
            inactivePaintBorder.setColor(inactiveBorderColor);

        if (activePaintBorder != null)
            activePaintBorder.setColor(activeBorderColor);
        invalidate();
    }

    public void setBackgroundColor(int backgroundColor) {
        if (paintBackground != null)
            paintBackground.setColor(backgroundColor);
        invalidate();
    }

    public void setStatusCount(int count) {
        mStatusCount = count;
        if (mViewCount > count) mViewCount = count;
        invalidate();
    }

    public void setViewCount(int count) {
        if (!(count > mStatusCount)) mViewCount = count;
        else mViewCount = mStatusCount;
        invalidate();
    }

    public int getStatusCount() {
        return mStatusCount;
    }

    public int getViewCount() {
        return mViewCount;
    }

    public void addShadow() {
        if (shadowRadius == 0)
            shadowRadius = DEFAULT_SHADOW_RADIUS;
        drawShadow(shadowRadius, shadowColor);
        invalidate();
    }

    public void setShadowRadius(float shadowRadius) {
        drawShadow(shadowRadius, shadowColor);
        invalidate();
    }

    public void setShadowColor(int shadowColor) {
        drawShadow(shadowRadius, shadowColor);
        invalidate();
    }

    public void setShadowGravity(ShadowGravity shadowGravity) {
        this.shadowGravity = shadowGravity;
        invalidate();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (this.colorFilter == colorFilter)
            return;
        this.colorFilter = colorFilter;
        drawable = null; // To force re-update shader
        invalidate();
    }

    @Override
    public ScaleType getScaleType() {
        ScaleType currentScaleType = super.getScaleType();
        return currentScaleType == null || currentScaleType != CENTER_INSIDE ? CENTER_CROP : currentScaleType;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType != CENTER_CROP && scaleType != CENTER_INSIDE) {
            throw new IllegalArgumentException(String.format("ScaleType %s not supported. " +
                    "Just ScaleType.CENTER_CROP & ScaleType.CENTER_INSIDE are available for this library.", scaleType));
        } else {
            super.setScaleType(scaleType);
        }
    }
    //endregion

    //region Draw Method
    @Override
    public void onDraw(Canvas canvas) {
        // Load the bitmap
        loadBitmap();

        // Check if image isn't null
        if (image == null)
            return;

        if (!isInEditMode()) {
            canvasSize = Math.min(canvas.getWidth(), canvas.getHeight());
        }

        // circleCenter is the x or y of the view's center
        // radius is the radius in pixels of the cirle to be drawn
        // paint contains the shader that will texture the shape
        int circleCenter = (int) (canvasSize - (borderWidth * 2)) / 2;
        float margeWithShadowRadius = shadowRadius * 2;

        mRect.set(mStroke/2, mStroke/2, canvasSize - mStroke/2, canvasSize - mStroke/2);

        // Draw Border
        float wS = mSpace / ((int)Math.floor(mStatusCount/10) + 1);
        if (mStatusCount < 2) wS = 0;

        float sw = (360f / mStatusCount) - wS;


        for (int i = 0;  i < mStatusCount; i++) {
            Path arc = new Path();
            arc.arcTo(mRect, 270 - wS/2 - (wS + sw) * i, -sw, true);

            if (i < mViewCount) canvas.drawPath(arc, inactivePaintBorder);
            else canvas.drawPath(arc, activePaintBorder);
        }

        // Draw Circle background
        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - margeWithShadowRadius, paintBackground);
        // Draw.StatusImageView
        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter - margeWithShadowRadius, paint);
    }

    private void loadBitmap() {
        if (drawable == getDrawable())
            return;

        drawable = getDrawable();
        image = drawableToBitmap(drawable);
        updateShader();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasSize = Math.min(w, h);
        if (image != null)
            updateShader();
    }

    private void drawShadow(float shadowRadius, int shadowColor) {
        this.shadowRadius = shadowRadius;
        this.shadowColor = shadowColor;

        setLayerType(LAYER_TYPE_SOFTWARE, inactivePaintBorder);
        setLayerType(LAYER_TYPE_SOFTWARE, activePaintBorder);

        float dx = 0.0f;
        float dy = 0.0f;

        switch (shadowGravity) {
            case CENTER:
                dx = 0.0f;
                dy = 0.0f;
                break;
            case TOP:
                dx = 0.0f;
                dy = -shadowRadius / 2;
                break;
            case BOTTOM:
                dx = 0.0f;
                dy = shadowRadius / 2;
                break;
            case START:
                dx = -shadowRadius / 2;
                dy = 0.0f;
                break;
            case END:
                dx = shadowRadius / 2;
                dy = 0.0f;
                break;
        }

        inactivePaintBorder.setShadowLayer(shadowRadius, dx, dy, shadowColor);
        activePaintBorder.setShadowLayer(shadowRadius, dx, dy, shadowColor);
    }

    private void updateShader() {
        if (image == null)
            return;

        // Create Shader
        BitmapShader shader = new BitmapShader(image, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // Center Image in Shader
        float scale = 0;
        float dx = 0;
        float dy = 0;

        switch (getScaleType()) {
            case CENTER_CROP:
                if (image.getWidth() * getHeight() > getWidth() * image.getHeight()) {
                    scale = getHeight() / (float) image.getHeight();
                    dx = (getWidth() - image.getWidth() * scale) * 0.5f;
                } else {
                    scale = getWidth() / (float) image.getWidth();
                    dy = (getHeight() - image.getHeight() * scale) * 0.5f;
                }
                break;
            case CENTER_INSIDE:
                if (image.getWidth() * getHeight() < getWidth() * image.getHeight()) {
                    scale = getHeight() / (float) image.getHeight();
                    dx = (getWidth() - image.getWidth() * scale) * 0.5f;
                } else {
                    scale = getWidth() / (float) image.getWidth();
                    dy = (getHeight() - image.getHeight() * scale) * 0.5f;
                }
                break;
        }

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
        shader.setLocalMatrix(matrix);

        // Set Shader in Paint
        paint.setShader(shader);

        // Apply colorFilter
        paint.setColorFilter(colorFilter);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            // Create Bitmap object out of the drawable
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //endregion

    //region Measure Method
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // The parent has determined an exact size for the child.
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // The parent has not imposed any constraint on the child.
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize;
        }

        return result + 2;
    }
    //endregion

    public enum ShadowGravity {
        CENTER,
        TOP,
        BOTTOM,
        START,
        END;

        public int getValue() {
            switch (this) {
                case CENTER:
                    return 1;
                case TOP:
                    return 2;
                case BOTTOM:
                    return 3;
                case START:
                    return 4;
                case END:
                    return 5;
            }
            throw new IllegalArgumentException("Not value available for this ShadowGravity: " + this);
        }

        public static ShadowGravity fromValue(int value) {
            switch (value) {
                case 1:
                    return CENTER;
                case 2:
                    return TOP;
                case 3:
                    return BOTTOM;
                case 4:
                    return START;
                case 5:
                    return END;
            }
            throw new IllegalArgumentException("This value is not supported for ShadowGravity: " + value);
        }

    }
}
