/*
 * android-spinnerwheel
 * https://github.com/ai212983/android-spinnerwheel
 *
 * based on
 *
 * Android Wheel Control.
 * https://code.google.com/p/android-wheel/
 *
 * Copyright 2011 Yuri Kanivets
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kankan.wheel.widget;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import com.jakewharton.nineoldandroids.Animator;
import com.jakewharton.nineoldandroids.AnimatorSet;
import com.jakewharton.nineoldandroids.ObjectAnimator;
import kankan.wheel.R;

/**
 * Numeric wheel view.
 *
 * @author Yuri Kanivets
 */
public class WheelVerticalView extends WheelView {

    /**
     * The {@link Paint} for drawing active value.
     */
    private Paint mActiveValuePaint;
    
    /**
     * The {@link Paint} for drawing the selector.
     */
    private Paint mSelectorWheelPaint;

    /**
     * {@link com.jakewharton.nineoldandroids.Animator} for showing the up/down arrows.
     */
    private AnimatorSet mShowInputControlsAnimator;

    /**
     * {@link com.jakewharton.nineoldandroids.Animator} for dimming the selector wheel.
     */
    private Animator mDimSelectorWheelAnimator;

    /**
     * The property for setting the selector paint.
     */
    private static final String PROPERTY_SELECTOR_PAINT_ALPHA = "selectorPaintAlpha";

    /**
     * The alpha of the selector wheel when it is bright.
     */
    private static final int SELECTOR_WHEEL_BRIGHT_ALPHA = 255;

    /**
     * The alpha of the selector wheel when it is dimmed.
     */
    private static final int SELECTOR_WHEEL_DIM_ALPHA = 60;


    // -------------- items above should be moved to WheelView

    /** Top and bottom shadows colors */
    private static final int[] SHADOWS_COLORS = new int[] { 0xFFffffff, 0x00ffffff, 0x00ffffff };

    /** Top and bottom items offset (to hide that) */
    private static final int ITEM_OFFSET_PERCENT = 10;

    /** Left and right padding value */
    private static final int PADDING = 10;

    // Item height
    private int itemHeight = 0;

    // Center Line
    private Drawable centerDrawable;

    // Shadows drawables
    private GradientDrawable topShadow;
    private GradientDrawable bottomShadow;

    /**
     * Constructor
     */
    public WheelVerticalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Constructor
     */
    public WheelVerticalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     */
    public WheelVerticalView(Context context) {
        super(context);
    }
    
    protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener) {
        return new WheelVerticalScroller(getContext(), scrollingListener);
    }

    protected void initData(Context context) {
        super.initData(context);
        // create the animator for showing the input controls
        mDimSelectorWheelAnimator = ObjectAnimator.ofInt(this, PROPERTY_SELECTOR_PAINT_ALPHA,
                SELECTOR_WHEEL_BRIGHT_ALPHA, SELECTOR_WHEEL_DIM_ALPHA);

        mShowInputControlsAnimator = new AnimatorSet();
        mShowInputControlsAnimator.playTogether(mDimSelectorWheelAnimator);
    }

    @Override
    protected void onScrollStarted() {
        mDimSelectorWheelAnimator.cancel();
    }

    @Override
    protected void onScrollFinished() {
        fadeSelectorWheel(500);
        Log.e("WheelVerticalView", "Dimming selector wheel");
    }

    /**
     * Sets the <code>alpha</code> of the {@link Paint} for drawing the selector
     * wheel.
     */
    @SuppressWarnings("unused")
    // Called via reflection
    public void setSelectorPaintAlpha(int alpha) {
        mSelectorWheelPaint.setAlpha(alpha);
        invalidate();
    }

    /**
     * Fade the selector wheel via an animation.
     *
     * @param animationDuration The duration of the animation.
     */
    private void fadeSelectorWheel(long animationDuration) {
        mDimSelectorWheelAnimator.setDuration(animationDuration);
        mDimSelectorWheelAnimator.start();
    }
    
    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (centerDrawable == null) {
            centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
        }

        if (topShadow == null) {
            topShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (bottomShadow == null) {
            bottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(100);
        //setBackgroundResource(R.drawable.wheel_bg_ver);
    }

    /**
     * Calculates desired height for layout
     *
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(LinearLayout layout) {
        if (layout != null && layout.getChildAt(0) != null) {
            itemHeight = layout.getChildAt(0).getMeasuredHeight();
        }

        int desired = itemHeight * visibleItems - itemHeight * ITEM_OFFSET_PERCENT / 50;
        return Math.max(desired, getSuggestedMinimumHeight());
    }

    /**
     * Returns height of wheel item
     * @return the item height
     */
    @Override
    protected int getItemDimension() {
        if (itemHeight != 0) {
            return itemHeight;
        }

        if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
            itemHeight = itemsLayout.getChildAt(0).getHeight();
            return itemHeight;
        }

        return getBaseDimension() / visibleItems;
    }

    /**
     * Calculates control width and creates text layouts
     * @param widthSize the input layout width
     * @param mode the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        initResourcesIfNecessary();

        // TODO: make it static
        itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        int width = itemsLayout.getMeasuredWidth();

        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width += 2 * PADDING;

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
            }
        }

        itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        return width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        buildViewForMeasuring();

        int width = calculateLayoutWidth(widthSize, widthMode);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(itemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    /**
     * Sets layouts width and height
     * @param width the layout width
     * @param height the layout height
     */
    @Override
    protected void layout(int width, int height) { //TODO: Something wrong with layout, its called too often. Unoptimized parent?
        itemsLayout.layout(0, 0, width - 2 * PADDING, height);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int ih = getItemDimension();
        Log.e("WheelVerticalView", "Layout invoked for " + this  + ": " + w + "x" + h + ",  " + ih);

        if (mSelectorWheelPaint == null) { // ugly hack to check stuff. remove it, see TO DO item for this method
        mSelectorWheelPaint = new Paint();
        float p1 = (1 - ih/(float) h)/2;
        float p2 = (1 + ih/(float) h)/2;
        int[] colors = {0x00000000, 0xff000000, 0x00000000, 0x00000000, 0xff000000, 0x00000000};
        float[] positions = {0, p1, p1, p2, p2, 1};

        LinearGradient shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
        //Set the paint to use this shader (linear gradient)
        mSelectorWheelPaint.setShader(shader);
        //Set the Transfer mode to be porter duff and destination in
        mSelectorWheelPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        mActiveValuePaint= new Paint();
        int[] colorsV = {0x00000000, 0x00000000, 0xff00ff00, 0xff0000ff, 0x00000000, 0x00000000};
        float[] positionsV = {0, p1, p1, p2, p2, 1};

        LinearGradient shaderV = new LinearGradient(0, 0, 0, h, colorsV, positionsV, Shader.TileMode.CLAMP);
        mActiveValuePaint.setShader(shaderV);
        mActiveValuePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
            updateView();

            drawItems(canvas);
            // drawCenterRect(canvas);
        }

        // drawShadows(canvas);
    }

    /**
     * Draws shadows on top and bottom of control
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        int height = (int)(1.5 * getItemDimension());
        topShadow.setBounds(0, 0, getWidth(), height);
        topShadow.draw(canvas);

        bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
        bottomShadow.draw(canvas);
    }

    /**
     * Draws items
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        drawNew(canvas);
    }

    private void drawNew(Canvas canvas) {
        canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int ih = getItemDimension();

        // creating intermediate bitmap and canvas
        Bitmap bSpin = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bSpin);
        Canvas cSpin = new Canvas(bSpin);

        int top = (currentItem - firstItem) * getItemDimension() + (getItemDimension() - getHeight()) / 2;
        c.translate(PADDING, - top + scrollingOffset);
        itemsLayout.draw(c);

        Bitmap bValue = bSpin.copy(Bitmap.Config.ARGB_8888, true);
        Canvas cValue = new Canvas(bValue); //TODO: We have to create canvas and bitmaps only once, do we?

        cSpin.drawRect(0, 0, w, h, mSelectorWheelPaint);
        cValue.drawRect(0, 0, w, h, mActiveValuePaint);

        canvas.drawBitmap(bValue, 0, 0, null);
        canvas.drawBitmap(bSpin, 0, 0, null);
        canvas.restore();
    }

    private void drawLegacy(Canvas canvas) {
        canvas.save();

        int top = (currentItem - firstItem) * getItemDimension() + (getItemDimension() - getHeight()) / 2;
        canvas.translate(PADDING, - top + scrollingOffset);

        int h = getDesiredHeight(itemsLayout);
        Paint paint = new Paint();
        int[] colors = {0x70ff0000, 0x600000ff};
        float[] positions = {0, 1};
        LinearGradient shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
        //Set the paint to use this shader (linear gradient)
        paint.setShader(shader);
        //Set the Transfer mode to be porter duff and destination in
        // paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //Draw a rectangle using the paint with our linear gradient
        canvas.drawRect(0, 0, getMeasuredWidth(), h, paint);

        itemsLayout.draw(canvas);

        canvas.restore();
    }
    
    
    /**
     * Draws rect for current value
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = (int) (getItemDimension() / 2 * 1.2);
        centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        centerDrawable.draw(canvas);
    }


    @Override
    protected int getBaseDimension() {
        return getHeight();
    }

    /**
     * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
     */
    @Override
    protected void updateView() {
        if (rebuildItems()) {
            calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            layout(getWidth(), getHeight());
        }
    }

    /**
     * Creates item layouts if necessary
     */
    @Override
    protected void createItemsLayout() {
        if (itemsLayout == null) {
            itemsLayout = new LinearLayout(getContext());
            itemsLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }
    
    @Override
    protected float getMotionEventPosition(MotionEvent event) {
        return event.getY();
    }

}
