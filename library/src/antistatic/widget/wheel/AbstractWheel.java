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

import java.util.LinkedList;
import java.util.List;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import antistatic.widget.R;
import antistatic.widget.wheel.adapters.WheelViewAdapter;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

/**
 * Abstract spinner widget view.
 * This class should be subclassed.
 *
 * @author Yuri Kanivets
 * @author Dimitri Fedorov
 */
public abstract class AbstractWheel extends View {

    private static int itemID = -1;

    @SuppressWarnings("unused")
    private final String LOG_TAG = AbstractWheel.class.getName() + " #" + (++itemID);

    //----------------------------------
    //  Default properties values
    //----------------------------------

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
    private WheelRecycler mRecycler = new WheelRecycler(this);

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

    //XXX: I don't like listeners the way as they are now. -df

    // Adapter listener
    private DataSetObserver mDataObserver;


    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Create a new AbstractWheel instance
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public AbstractWheel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initAttributes(attrs, defStyle);
        initData(context);
    }

    //--------------------------------------------------------------------------
    //
    //  Initiating data and assets at start up
    //
    //--------------------------------------------------------------------------

    /**
     * Initiates data and parameters from styles
     *
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    protected void initAttributes(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AbstractWheelView, defStyle, 0);
        mVisibleItems = a.getInt(R.styleable.AbstractWheelView_visibleItems, DEF_VISIBLE_ITEMS);
        mIsCyclic = a.getBoolean(R.styleable.AbstractWheelView_isCyclic, DEF_IS_CYCLIC);

        a.recycle();
    }

    /**
     * Initiates data
     *
     * @param context the context
     */
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
                notifyScrollingListenersAboutStart();
                onScrollStarted();
            }

            public void onTouch() {
                onScrollTouched();
            }

            public void onTouchUp() {
                if (!mIsScrollingPerformed)
                    onScrollTouchedUp(); // if scrolling IS performed, whe should use onFinished instead
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
                    notifyScrollingListenersAboutEnd();
                    mIsScrollingPerformed = false;
                    //onScrollFinished(); XXX: Fixing bug
                }

                //mScrollingOffset = 0; XXX: Fixing bug
                invalidate();
            }

            public void onJustify() {
                if (Math.abs(mScrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                    mScroller.scroll(mScrollingOffset, 0);
                }
            }
        });
    }


    abstract protected void recreateAssets(int width, int height);


    //--------------------------------------------------------------------------
    //
    //  Scroller operations
    //
    //--------------------------------------------------------------------------

    /**
     * Creates scroller appropriate for specific wheel implementation.
     *
     * @param scrollingListener listener to be passed to the scroller
     * @return Initialized scroller to be used
     */
    abstract protected WheelScroller createScroller(WheelScroller.ScrollingListener scrollingListener);

    /* These methods are not abstract, as we may want to override only some of them */
    protected void onScrollStarted() {}
    protected void onScrollTouched() {}
    protected void onScrollTouchedUp() {
        //XXX: Finding bug
        int cnt = mItemsLayout.getChildCount();
        Log.e(LOG_TAG, "We have " + cnt + " items");
        for (int i = 0; i < cnt; i++ ) {
            View v = mItemsLayout.getChildAt(i);
            String dT = (String) v.getTag();

            Log.e(LOG_TAG, "Item #" + i + ": " + dT + " at " + v.getTop() + ", " + v.getLeft());
        }
        mItemsLayout.requestLayout();
    }
    protected void onScrollFinished() {}

    /**
     * Stops scrolling
     */
    public void stopScrolling() {
        mScroller.stopScrolling();
    }

    /**
     * Set the the specified scrolling interpolator
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mScroller.setInterpolator(interpolator);
    }

    /**
     * Scroll the widget
     * @param itemsToScroll items to scroll
     * @param time scrolling duration
     */
    public void scroll(int itemsToScroll, int time) {
        int distance = itemsToScroll * getItemDimension() - mScrollingOffset;
        onScrollTouched(); // we have to emulate touch when scrolling widget programmatically to light up stuff
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

    //--------------------------------------------------------------------------
    //
    //  Base measurements
    //
    //--------------------------------------------------------------------------

    /**
     * Returns base dimension of the widget — width for horizontal widget, height for vertical
     *
     * @return width or height of the widget
     */
    abstract protected int getBaseDimension();

    /**
     * Returns base dimension of base item — width for horizontal widget, height for vertical
     *
     * @return width or height of base item
     */
    abstract protected int getItemDimension();

    /**
     * Processes MotionEvent and returns relevant position — x for horizontal widget, y for vertical
     *
     * @param event MotionEvent to be processed
     * @return relevant position of the MotionEvent
     */
    abstract protected float getMotionEventPosition(MotionEvent event);


    //--------------------------------------------------------------------------
    //
    //  Layout creation and measurement operations
    //
    //--------------------------------------------------------------------------

    /**
     * Creates item layouts if necessary
     */
    abstract protected void createItemsLayout();

    /**
     * Sets layout width and height
     */
    abstract protected void doItemsLayout();


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int w = r - l;
            int h = b - t;
            doItemsLayout();
            if (mLayoutWidth != w || mLayoutHeight != h) {
                recreateAssets(getMeasuredWidth(), getMeasuredHeight());
            }
            mLayoutWidth = w;
            mLayoutHeight = h;
        }
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


    //--------------------------------------------------------------------------
    //
    //  Getters and setters
    //
    //--------------------------------------------------------------------------

    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    public int getVisibleItems() {
        return mVisibleItems;
    }

    /**
     * Sets the desired count of visible items.
     * Actual amount of visible items depends on widget layout parameters.
     * To apply changes and rebuild view call measure().
     *
     * @param count the desired count for visible items
     */
    public void setVisibleItems(int count) {
        mVisibleItems = count;
    }

    /**
     * Gets view adapter
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter() {
        return mViewAdapter;
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
                notifyChangingListeners(old, mCurrentItemIdx);
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
     * Tests if widget is cyclic. That means before the 1st item there is shown the last one
     * @return true if widget is cyclic
     */
    public boolean isCyclic() {
        return mIsCyclic;
    }

    /**
     * Set widget cyclic flag
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.mIsCyclic = isCyclic;
        invalidateItemsLayout(false);
    }


    //--------------------------------------------------------------------------
    //
    //  Listener operations
    //
    //--------------------------------------------------------------------------

    /**
     * Adds widget changing listener
     * @param listener the listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        changingListeners.add(listener);
    }

    /**
     * Removes widget changing listener
     * @param listener the listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        changingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     * @param oldValue the old widget value
     * @param newValue the new widget value
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : changingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds widget scrolling listener
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.add(listener);
    }

    /**
     * Removes widget scrolling listener
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        scrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : scrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Adds widget clicking listener
     * @param listener the listener
     */
    public void addClickingListener(OnWheelClickedListener listener) {
        clickingListeners.add(listener);
    }

    /**
     * Removes widget clicking listener
     * @param listener the listener
     */
    public void removeClickingListener(OnWheelClickedListener listener) {
        clickingListeners.remove(listener);
    }

    /**
     * Notifies listeners about clicking
     * @param item clicked item
     */
    protected void notifyClickListenersAboutClick(int item) {
        for (OnWheelClickedListener listener : clickingListeners) {
            listener.onItemClicked(this, item);
        }
    }


    //--------------------------------------------------------------------------
    //
    //  Rebuilding items
    //
    //--------------------------------------------------------------------------

    /**
     * Rebuilds widget items if necessary. Caches all unused items.
     *
     * @return true if items are rebuilt
     */
    protected boolean rebuildItems() {
        boolean updated;
        ItemsRange range = getItemsRange();

        Log.e(LOG_TAG, "FirstItemIdx 01: " + mFirstItemIdx);

        if (mItemsLayout != null) {
            int first = mRecycler.recycleItems(mItemsLayout, mFirstItemIdx, range);
            updated = mFirstItemIdx != first;
            mFirstItemIdx = first;
        } else {
            createItemsLayout();
            updated = true;
        }
        Log.e(LOG_TAG, "FirstItemIdx 02: " + mFirstItemIdx);
        if (mItemsLayout.getChildCount() == 0) { // if mItemsLayout was cleaned up, update it
            updated = true;
        }

        int first;
        int rangeCount = range.getCount();
        if (!updated) {
            first = range.getFirst();
            if (first < 0)
                first = 0;
            if (rangeCount > mItemsLayout.getChildCount())
                rangeCount = mItemsLayout.getChildCount();
            updated = mFirstItemIdx != first || mItemsLayout.getChildCount() != rangeCount;
        }
        Log.e(LOG_TAG, "Range count: " + rangeCount);
        if (mFirstItemIdx > range.getFirst() && mFirstItemIdx <= range.getLast()) {
            for (int i = mFirstItemIdx - 1; i >= range.getFirst(); i--) {

                if (!addItemView(i, true)) {
                    break;
                }
                mFirstItemIdx = i;
                Log.e(LOG_TAG, "FirstItemIdx 02a: " + mFirstItemIdx);
            }
        } else {
            mFirstItemIdx = range.getFirst();
            Log.e(LOG_TAG, "FirstItemIdx 02b: " + mFirstItemIdx);
        }

        Log.e(LOG_TAG, "FirstItemIdx 03: " + mFirstItemIdx);
        first = mFirstItemIdx;
        int count = mItemsLayout.getChildCount();
        Log.e(LOG_TAG, "populating items");
        for (int i = count; i < range.getCount(); i++) {
            Log.e(LOG_TAG, " adding item #" + i);
            if (!addItemView(mFirstItemIdx + i, false) && mItemsLayout.getChildCount() == 0) {
                first++;
            }
        }
        Log.e(LOG_TAG, mItemsLayout.getChildCount() + " items populated");
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
        int itemSize = getItemDimension();
        if (itemSize == 0 || true) {
            Log.e(LOG_TAG, mScrollingOffset + " getItemsRange(), current item index: " + mCurrentItemIdx + ", visible items: " + mVisibleItems);
            int start = mCurrentItemIdx - (int)Math.floor(mVisibleItems / 2);
            int end = start + mVisibleItems - 1;
            Log.e(LOG_TAG, "getItemsRange(), supposed start: " + start + ", supposed end: " + end);
            if (!isCyclic()) {
                if (start < 0)
                    start = 0;
                if (end > mViewAdapter.getItemsCount())
                    end = mViewAdapter.getItemsCount();
            }
            Log.e(LOG_TAG, "getItemsRange(), range calculated, start: " + start + ", end: " + end);

            return new ItemsRange(start, end - start + 1);

        }

        int first = mCurrentItemIdx;
        int count = 1;

        while (count * itemSize < getBaseDimension()) {
            first--;
            count += 2; // starting and ending items
        }

        if (mScrollingOffset != 0) {
            if (mScrollingOffset > 0) {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            int emptyItems = mScrollingOffset / itemSize;
            first -= emptyItems;
            count += Math.ceil(emptyItems); // wtf is asin doing here? -df
        }


        int end = count - first;
        if (!isCyclic()) {
            if (first < 0)
                first = 0;
            if (end > mViewAdapter.getItemsCount())
                end = mViewAdapter.getItemsCount();
        }
        count = end - first;
        Log.e(LOG_TAG, "getItemsRange(), range calculated*, start: " + first + ", end: " + end );
        return new ItemsRange(first, count);
    }

    /**
     * Checks whether item index is valid
     * @param index the item index
     * @return true if item index is not out of bounds or the widget is cyclic
     */
    protected boolean isValidItemIndex(int index) {
        return (mViewAdapter != null) && (mViewAdapter.getItemsCount() > 0) &&
                ((mIsCyclic || index >= 0) && (index < mViewAdapter.getItemsCount()));
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
                Log.e(LOG_TAG, "  adding item #" + index + " / " + view.getTag() +" at first position");
            } else {
                mItemsLayout.addView(view);
                Log.e(LOG_TAG, "  adding item #" + index + " / " + view.getTag());
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
            return mViewAdapter.getEmptyItem(mRecycler.getEmptyItem(), mItemsLayout);
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
                        notifyClickListenersAboutClick(mCurrentItemIdx + items);
                    }
                }
                break;
        }
        return mScroller.onTouchEvent(event);
    }

}
