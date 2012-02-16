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
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Numeric wheel view.
 *
 * @author Yuri Kanivets
 */
public class WheelVerticalView extends WheelView {

    /**
     * The {@link Paint} for drawing the selector.
     */
    private Paint mSelectorWheelPaint;

    /**
     * Divider for showing item to be selected while scrolling
     */
    private Drawable mSelectionDivider;

    /**
     * The height of the selection divider.
     */
    private int mSelectionDividerHeight;

    /**
     * The height of a selector element (text + gap).
     */
    private int mSelectorElementHeight;

    /**
     * The {@link Paint} for drawing the separators.
     */
    private Paint mSeparatorsPaint;

    /**
     * {@link com.nineoldandroids.animation.Animator} for dimming the selector wheel.
     */
    private Animator mDimSelectorWheelAnimator;

    /**
     * {@link com.nineoldandroids.animation.Animator} for dimming the selector wheel.
     */
    private Animator mDimSeparatorsAnimator;

    /**
     * The property for setting the selector paint.
     */
    private static final String PROPERTY_SELECTOR_PAINT_COEFF = "selectorPaintCoeff";

    /**
     * The property for setting the separators paint.
     */
    private static final String PROPERTY_SEPARATORS_PAINT_ALPHA = "separatorsPaintAlpha";

    /**
     * The alpha of the selector wheel when it is dimmed.
     */
    private static final int SELECTOR_WHEEL_DIM_ALPHA = 30; // 60 in ICS

    /**
     * The alpha of separators wheel when they are shown.
     */
    private static final int SEPARATORS_BRIGHT_ALPHA = 70;

    /**
     * The alpha of separators when they are is dimmed.
     */
    private static final int SEPARATORS_DIM_ALPHA = 70;

    // -------------- items above should be moved to WheelView

    /** Top and bottom items offset (to hide that) */
    private static final int ITEM_OFFSET_PERCENT = 10;

    /** Left and right padding value */
    private static final int PADDING = 10;

    // Item height
    private int itemHeight = 0;

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
        mDimSelectorWheelAnimator = ObjectAnimator.ofFloat(this, PROPERTY_SELECTOR_PAINT_COEFF, 1, 0);

        mDimSeparatorsAnimator = ObjectAnimator.ofInt(this, PROPERTY_SEPARATORS_PAINT_ALPHA,
                SEPARATORS_BRIGHT_ALPHA, SEPARATORS_DIM_ALPHA
        );

        int[] dividerColors = new int[] { 0xFF111111, 0xFF222222, 0xFF111111 };
        mSelectionDivider = new  GradientDrawable(Orientation.LEFT_RIGHT, dividerColors);
        /*
        mSelectionDivider = attributesArray.getDrawable(R.styleable.NumberPicker_selectionDivider);
        int defSelectionDividerHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_SELECTION_DIVIDER_HEIGHT,
                getResources().getDisplayMetrics());
        */
        mSelectionDividerHeight = 1;
        //mSelectionDividerHeight = attributesArray.getDimensionPixelSize(
        //        R.styleable.NumberPicker_selectionDividerHeight, defSelectionDividerHeight);
    }

    @Override
    protected void onScrollTouched() {
        mDimSelectorWheelAnimator.cancel();
        mDimSeparatorsAnimator.cancel();
        setSelectorPaintCoeff(1);
        setSeparatorsPaintAlpha(SEPARATORS_BRIGHT_ALPHA);
    }

    @Override
    protected void onScrollTouchedUp() {
        fadeSelectorWheel(500);
        lightSeparators(500);
    }

    @Override
    protected void onScrollFinished() {
        fadeSelectorWheel(500);
        lightSeparators(500);
    }

    /**
     * Sets the <code>coeff</code> of the {@link Paint} for drawing the selector
     * wheel.
     */
    @SuppressWarnings("unused")
    // Called via reflection
    public void setSelectorPaintCoeff(float coeff) {

        int h = getMeasuredHeight();
        int ih = getItemDimension();

        float p1 = (1 - ih/(float) h)/2;
        float p2 = (1 + ih/(float) h)/2;
        float p3 = (1 - ih*3/(float) h)/2;
        float p4 = (1 + ih*3/(float) h)/2;

        float s = 255 * p3/p1;
        float z = SELECTOR_WHEEL_DIM_ALPHA * (1 - coeff);

        float c3f = s * coeff ;
        float c2f = z + c3f;
        float c1f = z + 255 * coeff;

        int c1 = Math.round( c1f ) << 24;
        int c2 = Math.round( c2f ) << 24;
        int c3 = Math.round( c3f ) << 24;

        int[] colors =      {0, c3, c2, c1, 0xff000000, 0xff000000, c1, c2, c3, 0};
        float[] positions = {0, p3, p3, p1,     p1,         p2,     p2, p4, p4, 1};

        LinearGradient shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
        mSelectorWheelPaint.setShader(shader);

        invalidate();
    }

    /**
     * Sets the <code>alpha</code> of the {@link Paint} for drawing separators
     * wheel.
     */
    @SuppressWarnings("unused")
    // Called via reflection
    public void setSeparatorsPaintAlpha(int alpha) {
        mSeparatorsPaint.setAlpha(alpha);
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
     * Fade the selector wheel via an animation.
     *
     * @param animationDuration The duration of the animation.
     */
    private void lightSeparators(long animationDuration) {
        mDimSeparatorsAnimator.setDuration(animationDuration);
        mDimSeparatorsAnimator.start();
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

        // initResourcesIfNecessary();

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

        itemsLayout.measure(
                MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );

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
    protected void layout(int width, int height) {
        itemsLayout.layout(0, 0, width - 2 * PADDING, height);

        mSelectorElementHeight = getItemDimension();

        if (mSelectorWheelPaint == null) { // ugly hack to check stuff. remove it
            mSelectorWheelPaint = new Paint();
            mSelectorWheelPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            setSelectorPaintCoeff(0);
        }

        if (mSeparatorsPaint == null) {
            mSeparatorsPaint = new Paint();
            mSeparatorsPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mSeparatorsPaint.setAlpha(SEPARATORS_DIM_ALPHA);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
            updateView();
            drawItems(canvas);
        }
    }


    /**
     * Draws items
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int ih = getItemDimension();

        // creating intermediate bitmap and canvas
        Bitmap bSpin = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bSpin);
        Canvas cSpin = new Canvas(bSpin);

        int top = (currentItem - firstItem) * ih + (ih - getHeight()) / 2;
        c.translate(PADDING, - top + scrollingOffset);
        itemsLayout.draw(c);

        // ----------------------------

        Bitmap bSeparators = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cSeparators = new Canvas(bSeparators);

        if (mSelectionDivider != null) {
            mSelectionDividerHeight = 2;
            // draw the top divider
            int topOfTopDivider =
                    (getHeight() - mSelectorElementHeight - mSelectionDividerHeight) / 2;
            int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), bottomOfTopDivider);
            mSelectionDivider.draw(cSeparators);

            // draw the bottom divider
            int topOfBottomDivider =  topOfTopDivider + mSelectorElementHeight;
            int bottomOfBottomDivider = bottomOfTopDivider + mSelectorElementHeight;
            mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(), bottomOfBottomDivider);
            mSelectionDivider.draw(cSeparators);
        }
        // ----------------------------

        cSpin.drawRect(0, 0, w, h, mSelectorWheelPaint);
        cSeparators.drawRect(0, 0, w, h, mSeparatorsPaint);

        canvas.drawBitmap(bSpin, 0, 0, null);
        canvas.drawBitmap(bSeparators, 0, 0, null);

        canvas.restore();
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
