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

package antistatic.widget.wheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import antistatic.widget.R;

/**
 * Spinner wheel vertical view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public class WheelVerticalView extends AbstractWheelView {

    private static int itemID = -1;

    @SuppressWarnings("unused")
    private final String LOG_TAG = WheelVerticalView.class.getName() + " #" + (++itemID);

    /**
     * The height of the selection divider.
     */
    protected int mSelectionDividerHeight;

    // Cached item height
    private int mItemHeight = 0;

    //--------------------------------------------------------------------------
    //
    //  Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Create a new wheel vertical view.
     *
     * @param context The application environment.
     */
    public WheelVerticalView(Context context) {
        this(context, null);
    }

    /**
     * Create a new wheel vertical view.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public WheelVerticalView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.abstractWheelViewStyle);
    }

    /**
     * Create a new wheel vertical view.
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public WheelVerticalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    //--------------------------------------------------------------------------
    //
    //  Initiating assets and setter for selector paint
    //
    //--------------------------------------------------------------------------

    @Override
    protected void initAttributes(AttributeSet attrs, int defStyle) {
        super.initAttributes(attrs, defStyle);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.WheelVerticalView, defStyle, 0);
        mSelectionDividerHeight = a.getDimensionPixelSize(R.styleable.WheelVerticalView_selectionDividerHeight, DEF_SELECTION_DIVIDER_SIZE);
        a.recycle();
    }

    @Override
    public void setSelectorPaintCoeff(float coeff) {
        LinearGradient shader;

        int h = getMeasuredHeight();
        int ih = getItemDimension();
        float p1 = (1 - ih/(float) h)/2;
        float p2 = (1 + ih/(float) h)/2;
        float z = mItemsDimmedAlpha * (1 - coeff);
        float c1f = z + 255 * coeff;

        if (mVisibleItems == 2) {
            int c1 = Math.round( c1f ) << 24;
            int c2 = Math.round( z ) << 24;
            int[] colors =      {c2, c1, 0xff000000, 0xff000000, c1, c2};
            float[] positions = { 0, p1,     p1,         p2,     p2,  1};
            shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
        } else {
            float p3 = (1 - ih*3/(float) h)/2;
            float p4 = (1 + ih*3/(float) h)/2;

            float s = 255 * p3/p1;
            float c3f = s * coeff ; // here goes some optimized stuff
            float c2f = z + c3f;

            int c1 = Math.round( c1f ) << 24;
            int c2 = Math.round( c2f ) << 24;
            int c3 = Math.round( c3f ) << 24;

            int[] colors =      {0, c3, c2, c1, 0xff000000, 0xff000000, c1, c2, c3, 0};
            float[] positions = {0, p3, p3, p1,     p1,         p2,     p2, p4, p4, 1};
            shader = new LinearGradient(0, 0, 0, h, colors, positions, Shader.TileMode.CLAMP);
        }
        mSelectorWheelPaint.setShader(shader);
        invalidate();
    }


    //--------------------------------------------------------------------------
    //
    //  Scroller-specific methods
    //
    //--------------------------------------------------------------------------

    @Override
    protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener) {
        return new WheelVerticalScroller(getContext(), scrollingListener);
    }

    @Override
    protected float getMotionEventPosition(MotionEvent event) {
        return event.getY();
    }

    //--------------------------------------------------------------------------
    //
    //  Base measurements
    //
    //--------------------------------------------------------------------------

    @Override
    protected int getBaseDimension() {
        return getHeight();
    }

    /**
     * Returns height of the widget
     * @return the item height
     */
    @Override
    protected int getItemDimension() {
        if (mItemHeight != 0) {
            return mItemHeight;
        }

        if (this.getChildAt(0) != null) {
            mItemHeight = this.getChildAt(0).getMeasuredHeight();
            return mItemHeight;
        }

        return getBaseDimension() / mVisibleItems;
    }


    //--------------------------------------------------------------------------
    //
    //  Layout creation and measurement operations
    //
    //--------------------------------------------------------------------------

    /**
     * Creates item layout if necessary
     */
    @Override
    protected void initData(Context context) {
        super.initData(context);
        this.setOrientation(LinearLayout.VERTICAL);
    }

/*s
    @Override
    protected void measureLayout() {
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        this.measure(
                MeasureSpec.makeMeasureSpec(getWidth() - 2 * mItemPadding, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
    }*/

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        rebuildItems(); // rebuilding before measuring

        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, 75);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, 300);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        /*int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
*/

/*
        // int width = calculateLayoutWidth(widthSize, widthMode); XXX: This is causing stack overflow
        int width = 100;

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = Math.max(
                    getItemDimension() * (mVisibleItems - mItemOffsetPercent / 100),
                    getSuggestedMinimumHeight()
            );

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }*/
        int w = getMeasuredWidth(); 
        int h = getMeasuredHeight();
        w = 75; h = 300;
        setMeasuredDimension(w, h);
        Log.e(LOG_TAG, "onMeasure is invoked, resulting: "+getMeasuredWidth()+"x"+getMeasuredHeight());
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == -1) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }

    /**
     * Calculates control width
     * @param widthSize the input layout width
     * @param mode the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.measure(
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        int width = this.getMeasuredWidth();

        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width += 2 * mItemPadding;

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
            }
        }
        // forcing recalculating
        this.measure(
                MeasureSpec.makeMeasureSpec(width - 2 * mItemPadding, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );

        return width;
    }


    //--------------------------------------------------------------------------
    //
    //  Drawing items
    //
    //--------------------------------------------------------------------------

    @Override
    protected void drawItems(Canvas canvas) {

        final int restoreCount = canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int ih = getItemDimension();

        // resetting intermediate bitmap and recreating canvases
        mSpinBitmap.eraseColor(0);
        Canvas c = new Canvas(mSpinBitmap);
        Canvas cSpin = new Canvas(mSpinBitmap);

        int top = (mCurrentItemIdx - mFirstItemIdx) * ih + (ih - getHeight()) / 2;
        //c.translate(mItemPadding, - top + mScrollingOffset);

        Log.e(LOG_TAG, "--------- drawingItems: "+ getChildCount());
        long drawTime = getDrawingTime();
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            //if (!child.isShown()) {
            //    continue;
            //}
            Log.e(LOG_TAG, "Item #" + i + ", " + child.getMeasuredWidth() + "x" + child.getMeasuredHeight() + " // "  + child.getLeft() + ", " + child.getTop() + ", " + child.getRight() + ", " + child.getBottom());
            drawChild(c, getChildAt(i), drawTime);
        }

        mSeparatorsBitmap.eraseColor(0);
        Canvas cSeparators = new Canvas(mSeparatorsBitmap);

        if (mSelectionDivider != null) {
            // draw the top divider
            int topOfTopDivider = (getHeight() - ih - mSelectionDividerHeight) / 2;
            int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
            mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), bottomOfTopDivider);
            mSelectionDivider.draw(cSeparators);

            // draw the bottom divider
            int topOfBottomDivider =  topOfTopDivider + ih;
            int bottomOfBottomDivider = bottomOfTopDivider + ih;
            mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(), bottomOfBottomDivider);
            mSelectionDivider.draw(cSeparators);
        }

        cSpin.drawRect(0, 0, w, h, mSelectorWheelPaint);
        cSeparators.drawRect(0, 0, w, h, mSeparatorsPaint);

        canvas.drawBitmap(mSpinBitmap, 0, 0, null);
        canvas.drawBitmap(mSeparatorsBitmap, 0, 0, null);

        canvas.restoreToCount(restoreCount);

    }

}
