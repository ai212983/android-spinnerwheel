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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import antistatic.widget.R;

/**
 * Numeric widget view.
 *
 * @author Yuri Kanivets
 */
public class WheelHorizontalView extends AbstractWheelView {

    /** Top and bottom items offset (to hide that) */
    private static final int ITEM_OFFSET_PERCENT = 10;

    /** Left and right padding value */
    private static final int PADDING = 10;

    // Item height
    private int itemWidth = 0;

    // Center Line
    private Drawable centerDrawable;

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

    @SuppressWarnings("unused") // Called via reflection
    public void setSelectorPaintCoeff(float coeff) {}

    @Override
    protected void measureLayout() { }

    /**
     * Initializes resources
     */
    private void initializeResources(AttributeSet attrs) {
        int backgroundResourceID = -1;
        if (attrs != null) {

            // TODO: Move styling to AbstractWheel class
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

        // setBackgroundResource(backgroundResourceID); // there's no background in ICS spinner
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
     * Returns height of widget item
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
    protected void doItemsLayout(int width, int height) {
        itemsLayout.layout(0, 0, width, height - 2 * PADDING);
    }


    /**
     * Draws items
     * @param canvas the canvas for drawing
     */
    @Override
    protected void drawItems(Canvas canvas) {
        canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        // creating intermediate bitmap and canvas
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        int position = (currentItem - firstItem) * getItemDimension() + (getItemDimension() - getBaseDimension()) / 2;
        c.translate(- position + scrollingOffset, PADDING);
        itemsLayout.draw(c);

        //Create a shader that is a linear gradient that covers the reflection
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, 0, w, 0, 0x70ff0000, 0x000000ff, Shader.TileMode.CLAMP);
        //Set the paint to use this shader (linear gradient)
        paint.setShader(shader);
        //Set the Transfer mode to be porter duff and destination in
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        //Draw a rectangle using the paint with our linear gradient
        c.drawRect(0, 0, w, h, paint);

        canvas.drawBitmap(b, 0, 0, null);
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
