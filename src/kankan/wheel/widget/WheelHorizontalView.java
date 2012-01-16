/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package kankan.wheel.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import kankan.wheel.R;

/**
 * Numeric wheel view.
 *
 * @author Yuri Kanivets
 */
public class WheelHorizontalView extends WheelView {

    /** Top and bottom shadows colors */
    private static final int[] SHADOWS_COLORS = new int[] { 0xFFffffff, 0x00ffffff, 0x00ffffff };

    /** Top and bottom items offset (to hide that) */
    private static final int ITEM_OFFSET_PERCENT = 10;

    /** Left and right padding value */
    private static final int PADDING = 10;

    // Item height
    private int itemWidth = 0;

    // Center Line
    private Drawable centerDrawable;

    // Shadows drawables
    private GradientDrawable leftShadow;
    private GradientDrawable rightShadow;

    /**
     * Constructor
     */
    public WheelHorizontalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeResources(attrs);
    }

    /**
     * Constructor
     */
    public WheelHorizontalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeResources(attrs);
    }

    /**
     * Constructor
     */
    public WheelHorizontalView(Context context) {
        super(context);
        initializeResources(null);
    }
    
    protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener) {
        return new WheelHorizontalScroller(getContext(), scrollingListener);
    }

    /**
     * Initializes resources
     */
    private void initializeResources(AttributeSet attrs) {
        int backgroundResourceID = -1;
        if (attrs != null) {
            TypedArray attr = getContext().obtainStyledAttributes(attrs, R.styleable.WheelHorizontalView);
            centerDrawable = attr.getDrawable(R.styleable.WheelHorizontalView_valueSelector);
            backgroundResourceID = attr.getResourceId(R.styleable.WheelHorizontalView_background, -1);
            attr.recycle();

        }

        if (centerDrawable == null) {
            centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_val);
        }
        
        if (backgroundResourceID == -1) {
            backgroundResourceID = R.drawable.wheel_bg_hor;
        }

        leftShadow = new GradientDrawable(Orientation.LEFT_RIGHT, SHADOWS_COLORS);
        rightShadow = new GradientDrawable(Orientation.RIGHT_LEFT, SHADOWS_COLORS);

        setBackgroundResource(backgroundResourceID);
    }

    /**
     * Calculates desired width for layout
     *
     * @param layout the source layout
     * @return the desired layout width
     */
    private int getDesiredWidth(LinearLayout layout) {
        if (layout != null && layout.getChildAt(0) != null) {
            itemWidth = layout.getChildAt(0).getMeasuredWidth();
        }

        int desired = itemWidth * visibleItems - itemWidth * ITEM_OFFSET_PERCENT / 50;
        return Math.max(desired, getSuggestedMinimumWidth());
    }

    /**
     * Returns height of wheel item
     * @return the item height
     */
    @Override
    protected int getItemDimension() {
        if (itemWidth != 0) {
            return itemWidth;
        }

        if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
            itemWidth = itemsLayout.getChildAt(0).getWidth();
            return itemWidth;
        }

        return getBaseDimension() / visibleItems;
    }

    /**
     * Calculates control height and creates text layouts
     * @param heightSize the input layout height
     * @param mode the layout mode
     * @return the calculated control height
     */
    private int calculateLayoutHeight(int heightSize, int mode) {

        // TODO: make it static
        itemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        itemsLayout.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED)
        );
        int height = itemsLayout.getMeasuredHeight();

        if (mode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height += 2 * PADDING;

            // Check against our minimum width
            height = Math.max(height, getSuggestedMinimumHeight());

            if (mode == MeasureSpec.AT_MOST && heightSize < height) {
                height = heightSize;
            }
        }

        itemsLayout.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(height - 2 * PADDING, MeasureSpec.EXACTLY)
        );

        return height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        buildViewForMeasuring();

        int height = calculateLayoutHeight(heightSize, heightMode);

        int width;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = getDesiredWidth(itemsLayout);

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
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
        itemsLayout.layout(0, 0, width, height - 2 * PADDING);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
            updateView();

            drawItems(canvas);
            drawCenterRect(canvas);
        }

        drawShadows(canvas);
    }

    /**
     * Draws shadows on top and bottom of control
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        int width = (int)(1.5 * getItemDimension());
        leftShadow.setBounds(0, 0, width, getHeight());
        leftShadow.draw(canvas);

        rightShadow.setBounds(getWidth() - width, 0, getWidth(), getHeight());
        rightShadow.draw(canvas);
    }

    /**
     * Draws items
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        canvas.save();

        int position = (currentItem - firstItem) * getItemDimension() + (getItemDimension() - getBaseDimension()) / 2;
        canvas.translate(- position + scrollingOffset, PADDING);

        itemsLayout.draw(canvas);

        canvas.restore();
    }

    /**
     * Draws rect for current value
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getWidth() / 2;
        int offset = (int) (getItemDimension() / 2 * 1.2);
        centerDrawable.setBounds(center - offset, 0, center + offset, getHeight());
        centerDrawable.draw(canvas);
    }


    @Override
    protected int getBaseDimension() {
        return getWidth();
    }

    /**
     * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
     */
    @Override
    protected void updateView() {
        if (rebuildItems()) {
            calculateLayoutHeight(getHeight(), MeasureSpec.EXACTLY);
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
            itemsLayout.setOrientation(LinearLayout.HORIZONTAL);
        }
    }
    
    @Override
    protected float getMotionEventPosition(MotionEvent event) {
        return event.getX();
    }

}
