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
import android.database.DataSetObserver;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import antistatic.widget.R;
import antistatic.widget.wheel.adapters.WheelViewAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.LinkedList;
import java.util.List;

/**
 * Spinner wheel horizontal view.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public class WheelNewHorizontalView extends View {


    //----------------------------------
    //  Default properties values
    //----------------------------------

    protected static final int DEF_ITEMS_DIMMED_ALPHA = 50; // 60 in ICS

    protected static final int DEF_SELECTION_DIVIDER_ACTIVE_ALPHA = 70;

    protected static final int DEF_SELECTION_DIVIDER_DIMMED_ALPHA = 70;

    protected static final int DEF_ITEM_OFFSET_PERCENT = 10;

    protected static final int DEF_ITEM_PADDING = 0;

    protected static final int DEF_SELECTION_DIVIDER_SIZE = 2;

    //----------------------------------
    //  Class properties
    //----------------------------------

    // configurable properties

    /** The alpha of the selector widget when it is dimmed. */
    protected int mItemsDimmedAlpha;

    /** The alpha of separators widget when they are shown. */
    protected int mSelectionDividerActiveAlpha;

    /** The alpha of separators when they are is dimmed. */
    protected int mSelectionDividerDimmedAlpha;

    /** Top and bottom items offset */
    protected int mItemOffsetPercent;

    /** Left and right padding value */
    protected int mItemPadding;

    /** Divider for showing item to be selected while scrolling */
    protected Drawable mSelectionDivider;

    // the rest

    /**
     * The {@link android.graphics.Paint} for drawing the selector.
     */
    protected Paint mSelectorWheelPaint;

    /**
     * The {@link android.graphics.Paint} for drawing the separators.
     */
    protected Paint mSeparatorsPaint;

    /**
     * {@link com.nineoldandroids.animation.Animator} for dimming the selector widget.
     */
    protected Animator mDimSelectorWheelAnimator;

    /**
     * {@link com.nineoldandroids.animation.Animator} for dimming the selector widget.
     */
    protected Animator mDimSeparatorsAnimator;

    /**
     * The property for setting the selector paint.
     */
    protected static final String PROPERTY_SELECTOR_PAINT_COEFF = "selectorPaintCoeff";

    /**
     * The property for setting the separators paint.
     */
    protected static final String PROPERTY_SEPARATORS_PAINT_ALPHA = "separatorsPaintAlpha";


    protected Bitmap mSpinBitmap;
    protected Bitmap mSeparatorsBitmap;

    private static int itemID = -1;

    @SuppressWarnings("unused")
    private final String LOG_TAG = WheelNewHorizontalView.class.getName() + " #" + (++itemID);

    /**
     * The width of the selection divider.
     */
    protected int mSelectionDividerWidth;

    // Item width
    private int itemWidth = 0;

    /** Default count of visible items */
    private static final int DEF_VISIBLE_ITEMS = 4;
    private static final boolean DEF_IS_CYCLIC = false;

    //----------------------------------
    //  Class properties
    //----------------------------------

    protected int mCurrentItemIdx = 0;

    // Count of visible items
    protected int mVisibleItems;

    protected boolean mIsCyclic;

    // Scrolling
    protected WheelScroller mScroller;
    protected boolean mIsScrollingPerformed;
    protected int mScrollingOffset;

    // Items layout
    protected LinearLayout mItemsLayout;

    // The number of first item in layout
    protected int mFirstItemIdx;

    // View adapter
    protected WheelViewAdapter mViewAdapter;

    protected int mLayoutHeight;
    protected int mLayoutWidth;

    // Recycle
    private WheelNewRecycler mRecycler = new WheelNewRecycler(this);

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

    //XXX: I don't like listeners the way as they are now. -df

    // Adapter listener
    private DataSetObserver mDataObserver;


    //--------------------------------------------------------------------------
    //
    //  Constructors
    //
    //--------------------------------------------------------------------------

    /**
     * Create a new wheel horizontal view.
     *
     * @param context The application environment.
     */
    public WheelNewHorizontalView(Context context) {
        this(context, null);
    }

    /**
     * Create a new wheel horizontal view.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public WheelNewHorizontalView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.abstractWheelViewStyle);
    }

    /**
     * Create a new wheel horizontal view.
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public WheelNewHorizontalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initAttributes(attrs, defStyle);
        initData(context);
    }

    //--------------------------------------------------------------------------
    //
    //  Initiating assets and setter for selector paint
    //
    //--------------------------------------------------------------------------
    protected void initData(Context context) {

        mDataObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                invalidateItemsLayout(false);
            }

            @Override
            public void onInvalidated() {
                invalidateItemsLayout(true);
            }
        };

        // creating new scroller
        mScroller = createScroller(new WheelScroller.ScrollingListener() {

            public void onStarted() {
                mIsScrollingPerformed = true;
                // notifyScrollingListenersAboutStart();
                //onScrollStarted();
            }

            public void onTouch() {
                //onScrollTouched();
            }

            public void onTouchUp() {
                /*
                if (!mIsScrollingPerformed)
                    onScrollTouchedUp(); // if scrolling IS performed, whe should use onFinished instead
                */
            }

            public void onScroll(int distance) {
                doScroll(distance);

                int dimension = getBaseDimension();
                if (mScrollingOffset >  dimension) {
                    mScrollingOffset =  dimension;
                    mScroller.stopScrolling();
                } else if (mScrollingOffset < - dimension) {
                    mScrollingOffset = - dimension;
                    mScroller.stopScrolling();
                }
            }

            public void onFinished() {
                if (mIsScrollingPerformed) {
                    //notifyScrollingListenersAboutEnd();
                    mIsScrollingPerformed = false;
                    //onScrollFinished();
                }

                mScrollingOffset = 0;
                invalidate();
            }

            public void onJustify() {
                if (Math.abs(mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                    mScroller.scroll(mScrollingOffset, 0);
                }
            }
        });

        // creating animators
        mDimSelectorWheelAnimator = ObjectAnimator.ofFloat(this, PROPERTY_SELECTOR_PAINT_COEFF, 1, 0);

        mDimSeparatorsAnimator = ObjectAnimator.ofInt(this, PROPERTY_SEPARATORS_PAINT_ALPHA,
                mSelectionDividerActiveAlpha, mSelectionDividerDimmedAlpha
        );

        // creating paints
        mSeparatorsPaint = new Paint();
        mSeparatorsPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mSeparatorsPaint.setAlpha(mSelectionDividerDimmedAlpha);

        mSelectorWheelPaint = new Paint();
        mSelectorWheelPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }


    protected void initAttributes(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AbstractWheelView, defStyle, 0);
        mItemsDimmedAlpha = a.getInt(R.styleable.AbstractWheelView_itemsDimmedAlpha, DEF_ITEMS_DIMMED_ALPHA);
        mSelectionDividerActiveAlpha = a.getInt(R.styleable.AbstractWheelView_selectionDividerActiveAlpha, DEF_SELECTION_DIVIDER_ACTIVE_ALPHA);
        mSelectionDividerDimmedAlpha = a.getInt(R.styleable.AbstractWheelView_selectionDividerDimmedAlpha, DEF_SELECTION_DIVIDER_DIMMED_ALPHA);
        mItemOffsetPercent = a.getInt(R.styleable.AbstractWheelView_itemOffsetPercent, DEF_ITEM_OFFSET_PERCENT);
        mItemPadding = a.getDimensionPixelSize(R.styleable.AbstractWheelView_itemPadding, DEF_ITEM_PADDING);
        a.recycle();
    }

    public void setSelectorPaintCoeff(float coeff) {
        LinearGradient shader;

        int w = getMeasuredWidth();
        int iw = getItemDimension();
        float p1 = (1 - iw/(float) w)/2;
        float p2 = (1 + iw/(float) w)/2;
        float z = mItemsDimmedAlpha * (1 - coeff);
        float c1f = z + 255 * coeff;

        if (mVisibleItems == 2) {
            int c1 = Math.round( c1f ) << 24;
            int c2 = Math.round( z ) << 24;
            int[] colors =      {c2, c1, 0xff000000, 0xff000000, c1, c2};
            float[] positions = { 0, p1,     p1,         p2,     p2,  1};
            shader = new LinearGradient(0, 0, w, 0, colors, positions, Shader.TileMode.CLAMP);
        } else {
            float p3 = (1 - iw*3/(float) w)/2;
            float p4 = (1 + iw*3/(float) w)/2;

            float s = 255 * p3/p1;
            float c3f = s * coeff ; // here goes some optimized stuff
            float c2f = z + c3f;

            int c1 = Math.round( c1f ) << 24;
            int c2 = Math.round( c2f ) << 24;
            int c3 = Math.round( c3f ) << 24;

            int[] colors =      {0, c3, c2, c1, 0xff000000, 0xff000000, c1, c2, c3, 0};
            float[] positions = {0, p3, p3, p1,     p1,         p2,     p2, p4, p4, 1};
            shader = new LinearGradient(0, 0, w, 0, colors, positions, Shader.TileMode.CLAMP);
        }
        mSelectorWheelPaint.setShader(shader);
        invalidate();
    }


    //--------------------------------------------------------------------------
    //
    //  Scroller-specific methods
    //
    //--------------------------------------------------------------------------

    /**
     * Scroll the widget
     * @param itemsToScroll items to scroll
     * @param time scrolling duration
     */
    public void scroll(int itemsToScroll, int time) {
        int distance = itemsToScroll * getItemDimension() - mScrollingOffset;
        //onScrollTouched(); // we have to emulate touch when scrolling widget programmatically to light up stuff
        mScroller.scroll(distance, time);
    }

    /**
     * Scrolls the widget
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        mScrollingOffset += delta;

        int itemDimension = getItemDimension();
        int count = mScrollingOffset / itemDimension;

        int pos = mCurrentItemIdx - count;
        int itemCount = mViewAdapter.getItemsCount();

        int fixPos = mScrollingOffset % itemDimension;
        if (Math.abs(fixPos) <= itemDimension / 2) {
            fixPos = 0;
        }
        if (mIsCyclic && itemCount > 0) {
            if (fixPos > 0) {
                pos--;
                count++;
            } else if (fixPos < 0) {
                pos++;
                count--;
            }
            // fix position by rotating
            while (pos < 0) {
                pos += itemCount;
            }
            pos %= itemCount;
        } else {
            if (pos < 0) {
                count = mCurrentItemIdx;
                pos = 0;
            } else if (pos >= itemCount) {
                count = mCurrentItemIdx - itemCount + 1;
                pos = itemCount - 1;
            } else if (pos > 0 && fixPos > 0) {
                pos--;
                count++;
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++;
                count--;
            }
        }

        int offset = mScrollingOffset;
        if (pos != mCurrentItemIdx) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        int baseDimension = getBaseDimension();
        mScrollingOffset = offset - count * itemDimension;
        if (mScrollingOffset > baseDimension) {
            mScrollingOffset = mScrollingOffset % baseDimension + baseDimension;
        }
    }

    protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener) {
        return new WheelHorizontalScroller(getContext(), scrollingListener);
    }


    protected float getMotionEventPosition(MotionEvent event) {
        return event.getY();
    }


    //--------------------------------------------------------------------------
    //
    //  Base measurements
    //
    //--------------------------------------------------------------------------


    protected int getBaseDimension() {
        return getWidth();
    }

    /**
     * Returns height of widget item
     * @return the item height
     */

    protected int getItemDimension() {
        if (itemWidth != 0) {
            return itemWidth;
        }

        if (mItemsLayout != null && mItemsLayout.getChildAt(0) != null) {
            itemWidth = mItemsLayout.getChildAt(0).getMeasuredWidth();
            return itemWidth;
        }

        return getBaseDimension() / mVisibleItems;
    }


    //--------------------------------------------------------------------------
    //
    //  Layout creation and measurement operations
    //
    //--------------------------------------------------------------------------

    /**
     * Creates item layouts if necessary
     */

    protected void createItemsLayout() {
        if (mItemsLayout == null) {
            mItemsLayout = new LinearLayout(getContext());
            mItemsLayout.setOrientation(LinearLayout.HORIZONTAL);
        }
    }


    protected void doItemsLayout() {
        mItemsLayout.layout(0, 0, getMeasuredWidth(), getMeasuredHeight() - 2 * mItemPadding);
    }


    protected void measureLayout() {
        mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mItemsLayout.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(getHeight() - 2 * mItemPadding, MeasureSpec.EXACTLY)
        );
    }

    //XXX: Most likely, measurements of mItemsLayout or/and its children are done inconrrectly.
    // Investigate and fix it

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        rebuildItems(); // rebuilding before measuring

        int height = calculateLayoutHeight(heightSize, heightMode);

        int width;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = Math.max(
                    getItemDimension() * (mVisibleItems - mItemOffsetPercent / 100),
                    getSuggestedMinimumWidth()
            );

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize);
            }
        }
        setMeasuredDimension(width, height);
    }


    /**
     * Calculates control height and creates text layouts
     * @param heightSize the input layout height
     * @param mode the layout mode
     * @return the calculated control height
     */
    private int calculateLayoutHeight(int heightSize, int mode) {
        mItemsLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mItemsLayout.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED)
        );
        int height = mItemsLayout.getMeasuredHeight();

        if (mode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height += 2 * mItemPadding;

            // Check against our minimum width
            height = Math.max(height, getSuggestedMinimumHeight());

            if (mode == MeasureSpec.AT_MOST && heightSize < height) {
                height = heightSize;
            }
        }
        // forcing recalculating
        mItemsLayout.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(height - 2 * mItemPadding, MeasureSpec.EXACTLY)
        );

        return height;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int w = r - l;
            int h = b - t;
            doItemsLayout();
            if (mLayoutWidth != w || mLayoutHeight != h) {
                //mLayoutWidth = w;
                //mLayoutHeight = h;
                mLayoutWidth = 500;
                mLayoutHeight = 65;

                recreateAssets(mLayoutWidth, mLayoutHeight);
            }
        }
    }

    protected void recreateAssets(int width, int height) {
        mSpinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSeparatorsBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        setSelectorPaintCoeff(0);
    }

    /**
     * Invalidates items layout
     *
     * @param clearCaches if true then cached views will be cleared
     */
    public void invalidateItemsLayout(boolean clearCaches) {
        if (clearCaches) {
            mRecycler.clearAll();
            if (mItemsLayout != null) {
                mItemsLayout.removeAllViews();
            }
            mScrollingOffset = 0;
        } else if (mItemsLayout != null) {
            // cache all items
            mRecycler.recycleItems(mItemsLayout, mFirstItemIdx, new ItemsRange());
        }
        invalidate();
    }

    /**
     * Sets view adapter. Usually new adapters contain different views, so
     * it needs to rebuild view by calling measure().
     *
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(WheelViewAdapter viewAdapter) {
        if (this.mViewAdapter != null) {
            this.mViewAdapter.unregisterDataSetObserver(mDataObserver);
        }
        this.mViewAdapter = viewAdapter;
        if (this.mViewAdapter != null) {
            this.mViewAdapter.registerDataSetObserver(mDataObserver);
        }
        invalidateItemsLayout(true);
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return mCurrentItemIdx;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated) {
        if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
            return; // throw?
        }

        int itemCount = mViewAdapter.getItemsCount();
        if (index < 0 || index >= itemCount) {
            if (mIsCyclic) {
                while (index < 0) {
                    index += itemCount;
                }
                index %= itemCount;
            } else{
                return; // throw?
            }
        }
        if (index != mCurrentItemIdx) {
            if (animated) {
                int itemsToScroll = index - mCurrentItemIdx;
                if (mIsCyclic) {
                    int scroll = itemCount + Math.min(index, mCurrentItemIdx) - Math.max(index, mCurrentItemIdx);
                    if (scroll < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                scroll(itemsToScroll, 0);
            } else {
                mScrollingOffset = 0;
                final int old = mCurrentItemIdx;
                mCurrentItemIdx = index;
                //notifyChangingListeners(old, mCurrentItemIdx);
                invalidate();
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     *
     * @param index the item index
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }


    /**
     * Rebuilds widget items if necessary. Caches all unused items.
     *
     * @return true if items are rebuilt
     */
    protected boolean rebuildItems() {
        boolean updated;
        ItemsRange range = getItemsRange();

        if (mItemsLayout != null) {
            int first = mRecycler.recycleItems(mItemsLayout, mFirstItemIdx, range);
            updated = mFirstItemIdx != first;
            mFirstItemIdx = first;
        } else {
            createItemsLayout();
            updated = true;
        }

        if (!updated) {
            updated = mFirstItemIdx != range.getFirst() || mItemsLayout.getChildCount() != range.getCount();
        }

        if (mFirstItemIdx > range.getFirst() && mFirstItemIdx <= range.getLast()) {
            for (int i = mFirstItemIdx - 1; i >= range.getFirst(); i--) {
                if (!addItemView(i, true)) {
                    break;
                }
                mFirstItemIdx = i;
            }
        } else {
            mFirstItemIdx = range.getFirst();
        }

        int first = mFirstItemIdx;
        for (int i = mItemsLayout.getChildCount(); i < range.getCount(); i++) {
            if (!addItemView(mFirstItemIdx + i, false) && mItemsLayout.getChildCount() == 0) {
                first++;
            }
        }
        mFirstItemIdx = first;

        return updated;
    }

    //----------------------------------
    //  ItemsRange operations
    //----------------------------------

    /**
     * Calculates range for widget items
     * @return the items range
     */
    private ItemsRange getItemsRange() {
        int start = mCurrentItemIdx - mVisibleItems / 2;
        int end = start + mVisibleItems - (mVisibleItems % 2 == 0 ? 0 : 1);
        if (mScrollingOffset != 0) {
            if (mScrollingOffset > 0) {
                start--;
            } else {
                end++;
            }
        }
        if (!mIsCyclic) {
            if (start < 0)
                start = 0;
            if (end > mViewAdapter.getItemsCount())
                end = mViewAdapter.getItemsCount();
        }
        return new ItemsRange(start, end - start + 1);
    }

    /**
     * Checks whether item index is valid
     * @param index the item index
     * @return true if item index is not out of bounds or the widget is cyclic
     */
    protected boolean isValidItemIndex(int index) {
        return (mViewAdapter != null) && (mViewAdapter.getItemsCount() > 0) &&
                (mIsCyclic || (index >= 0 && index < mViewAdapter.getItemsCount()));
    }


    //----------------------------------
    //  Operations with item view
    //----------------------------------

    /**
     * Adds view for item to items layout
     * @param index the item index
     * @param first the flag indicates if view should be first
     * @return true if corresponding item exists and is added
     */
    private boolean addItemView(int index, boolean first) {
        View view = getItemView(index);
        if (view != null) {
            if (first) {
                mItemsLayout.addView(view, 0);
            } else {
                mItemsLayout.addView(view);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns view for specified item
     * @param index the item index
     * @return item view or empty view if index is out of bounds
     */
    private View getItemView(int index) {
        if (mViewAdapter == null || mViewAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = mViewAdapter.getItemsCount();
        if (!isValidItemIndex(index)) {
            View recItem = mRecycler.getEmptyItem();
            View v =  mViewAdapter.getEmptyItem(recItem, mItemsLayout);
            return v;
        } else {
            while (index < 0) {
                index = count + index;
            }
        }
        index %= count;
        return mViewAdapter.getItem(index, mRecycler.getItem(), mItemsLayout);
    }




    //--------------------------------------------------------------------------
    //
    //  Drawing items
    //
    //--------------------------------------------------------------------------


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewAdapter != null && mViewAdapter.getItemsCount() > 0) {
            if (rebuildItems()) {
                measureLayout();
                doItemsLayout();
            }
            drawItems(canvas);
        }
    }


    protected void drawItems(Canvas canvas) {
        canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int iw = getItemDimension();

        // resetting intermediate bitmap and recreating canvases
        mSpinBitmap.eraseColor(0);
        Canvas c = new Canvas(mSpinBitmap);
        Canvas cSpin = new Canvas(mSpinBitmap);

        int left = (mCurrentItemIdx - mFirstItemIdx) * iw + (iw - getWidth()) / 2;
        c.translate(- left + mScrollingOffset, mItemPadding);
        mItemsLayout.draw(c);

        mSeparatorsBitmap.eraseColor(0);
        Canvas cSeparators = new Canvas(mSeparatorsBitmap);

        if (mSelectionDivider != null) {
            // draw the top divider
            int leftOfLeftDivider = (getWidth() - iw - mSelectionDividerWidth) / 2;
            int rightOfLeftDivider = leftOfLeftDivider + mSelectionDividerWidth;
            mSelectionDivider.setBounds(leftOfLeftDivider, 0, rightOfLeftDivider, getHeight());
            mSelectionDivider.draw(cSeparators);

            // draw the bottom divider
            int leftOfRightDivider =  leftOfLeftDivider + iw;
            int rightOfRightDivider = rightOfLeftDivider + iw;
            mSelectionDivider.setBounds(leftOfRightDivider, 0, rightOfRightDivider, getHeight());
            mSelectionDivider.draw(cSeparators);
        }

        cSpin.drawRect(0, 0, w, h, mSelectorWheelPaint);
        cSeparators.drawRect(0, 0, w, h, mSeparatorsPaint);

        canvas.drawBitmap(mSpinBitmap, 0, 0, null);
        canvas.drawBitmap(mSeparatorsBitmap, 0, 0, null);
        canvas.restore();
    }
    /**
     * Gets view adapter
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter() {
        return mViewAdapter;
    }
    /**
     * Tests if widget is cyclic. That means before the 1st item there is shown the last one
     * @return true if widget is cyclic
     */
    public boolean isCyclic() {
        return mIsCyclic;
    }

    //--------------------------------------------------------------------------
    //
    //  Intercepting and processing touch event
    //
    //--------------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || getViewAdapter() == null) {
            return true;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!mIsScrollingPerformed) {
                    int distance = (int) getMotionEventPosition(event) - getBaseDimension() / 2;
                    if (distance > 0) {
                        distance += getItemDimension() / 2;
                    } else {
                        distance -= getItemDimension() / 2;
                    }
                    int items = distance / getItemDimension();
                    if (items != 0 && isValidItemIndex(mCurrentItemIdx + items)) {
                        // notifyClickListenersAboutClick(mCurrentItemIdx + items);
                    }
                }
                break;
        }
        return mScroller.onTouchEvent(event);
    }

}
