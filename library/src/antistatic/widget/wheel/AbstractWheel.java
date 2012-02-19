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

    /** Default count of visible items */
    private static final int DEF_VISIBLE_ITEMS = 4;

    protected int currentItem = 0;

    // Count of visible items
    protected int visibleItems = DEF_VISIBLE_ITEMS;

    // Scrolling
    protected WheelScroller mScroller;
    protected boolean isScrollingPerformed;
    protected int scrollingOffset;

    boolean isCyclic = false;

    // Items layout
    protected LinearLayout itemsLayout;

    // The number of first item in layout
    protected int firstItem;

    // View adapter
    protected WheelViewAdapter viewAdapter;
    
    protected int mLayoutHeight;
    protected int mLayoutWidth;

    // Recycle
    private WheelRecycle recycle = new WheelRecycle(this);

    // Listeners
    private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
    private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
    private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

    //TODO: I don't like listeners the way as they are now. -df

    // Adapter listener
    private DataSetObserver mDataObserver;


    //--------------------------------------------------------------------------
    //
    //  Constructors
    //
    //--------------------------------------------------------------------------

    public AbstractWheel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    public AbstractWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    /**
     * Constructor
     * @param context Context for creation
     */
    public AbstractWheel(Context context) {
        super(context);
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
                isScrollingPerformed = true;
                notifyScrollingListenersAboutStart();
                onScrollStarted();
            }

            public void onTouch() {
                onScrollTouched();
            }

            public void onTouchUp() {
                if (!isScrollingPerformed)
                    onScrollTouchedUp(); // if scrolling IS performed, whe should use onFinished instead
            }

            public void onScroll(int distance) {
                doScroll(distance);

                int dimension = getBaseDimension();
                if (scrollingOffset >  dimension) {
                    scrollingOffset =  dimension;
                    mScroller.stopScrolling();
                } else if (scrollingOffset < - dimension) {
                    scrollingOffset = - dimension;
                    mScroller.stopScrolling();
                }
            }

            public void onFinished() {
                if (isScrollingPerformed) {
                    notifyScrollingListenersAboutEnd();
                    isScrollingPerformed = false;
                    onScrollFinished();
                }

                scrollingOffset = 0;
                invalidate();
            }

            public void onJustify() {
                if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
                    mScroller.scroll(scrollingOffset, 0);
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
    protected void onScrollTouchedUp() {}
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
        int distance = itemsToScroll * getItemDimension() - scrollingOffset;
        onScrollTouched(); // we have to emulate touch when scrolling widget programmatically to light up stuff
        mScroller.scroll(distance, time);
    }

    /**
     * Scrolls the widget
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        scrollingOffset += delta;

        int itemDimension = getItemDimension();
        int count = scrollingOffset / itemDimension;

        int pos = currentItem - count;
        int itemCount = viewAdapter.getItemsCount();

        int fixPos = scrollingOffset % itemDimension;
        if (Math.abs(fixPos) <= itemDimension / 2) {
            fixPos = 0;
        }
        if (isCyclic && itemCount > 0) {
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
                count = currentItem;
                pos = 0;
            } else if (pos >= itemCount) {
                count = currentItem - itemCount + 1;
                pos = itemCount - 1;
            } else if (pos > 0 && fixPos > 0) {
                pos--;
                count++;
            } else if (pos < itemCount - 1 && fixPos < 0) {
                pos++;
                count--;
            }
        }

        int offset = scrollingOffset;
        if (pos != currentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        int baseDimension = getBaseDimension();
        scrollingOffset = offset - count * itemDimension;
        if (scrollingOffset > baseDimension) {
            scrollingOffset = scrollingOffset % baseDimension + baseDimension;
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
            recycle.clearAll();
            if (itemsLayout != null) {
                itemsLayout.removeAllViews();
            }
            scrollingOffset = 0;
        } else if (itemsLayout != null) {
            // cache all items
            recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
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
        return visibleItems;
    }

    /**
     * Sets the desired count of visible items.
     * Actual amount of visible items depends on widget layout parameters.
     * To apply changes and rebuild view call measure().
     *
     * @param count the desired count for visible items
     */
    public void setVisibleItems(int count) {
        visibleItems = count;
    }

    /**
     * Gets view adapter
     * @return the view adapter
     */
    public WheelViewAdapter getViewAdapter() {
        return viewAdapter;
    }


    /**
     * Sets view adapter. Usually new adapters contain different views, so
     * it needs to rebuild view by calling measure().
     *
     * @param viewAdapter the view adapter
     */
    public void setViewAdapter(WheelViewAdapter viewAdapter) {
        if (this.viewAdapter != null) {
            this.viewAdapter.unregisterDataSetObserver(mDataObserver);
        }
        this.viewAdapter = viewAdapter;
        if (this.viewAdapter != null) {
            this.viewAdapter.registerDataSetObserver(mDataObserver);
        }
        invalidateItemsLayout(true);
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated) {
        if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
            return; // throw?
        }

        int itemCount = viewAdapter.getItemsCount();
        if (index < 0 || index >= itemCount) {
            if (isCyclic) {
                while (index < 0) {
                    index += itemCount;
                }
                index %= itemCount;
            } else{
                return; // throw?
            }
        }
        if (index != currentItem) {
            if (animated) {
                int itemsToScroll = index - currentItem;
                if (isCyclic) {
                    int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
                    if (scroll < Math.abs(itemsToScroll)) {
                        itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
                    }
                }
                scroll(itemsToScroll, 0);
            } else {
                scrollingOffset = 0;
                final int old = currentItem;
                currentItem = index;
                notifyChangingListeners(old, currentItem);
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
        return isCyclic;
    }

    /**
     * Set widget cyclic flag
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;
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

        if (itemsLayout != null) {
            int first = recycle.recycleItems(itemsLayout, firstItem, range);
            updated = firstItem != first;
            firstItem = first;
        } else {
            createItemsLayout();
            updated = true;
        }
        
        if (itemsLayout.getChildCount() == 0) { // if itemsLayout was cleaned up, update it
            updated = true;
        }

        int first;
        if (!updated) {
            first = range.getFirst();
            int cnt = range.getCount();
            if (first < 0)
                first = 0;
            if (cnt > itemsLayout.getChildCount())
                cnt = itemsLayout.getChildCount();
            updated = firstItem != first || itemsLayout.getChildCount() != cnt;
        }
        
        if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
            for (int i = firstItem - 1; i >= range.getFirst(); i--) {
                if (!addItemView(i, true)) {
                    break;
                }
                firstItem = i;
            }
        } else {
            firstItem = range.getFirst();
        }

        first = firstItem;
        for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
            if (!addItemView(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
                first++;
            }
        }
        firstItem = first;
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
        int id = getItemDimension();
        if (id == 0) {
            return new ItemsRange(currentItem - visibleItems / 2, visibleItems);
        }

        int first = currentItem;
        int count = 1;

        while (count * id < getBaseDimension()) {
            first--;
            count += 2; // starting and ending items
        }

        if (scrollingOffset != 0) {
            if (scrollingOffset > 0) {
                first--;
            }
            count++;

            // process empty items above the first or below the second
            int emptyItems = scrollingOffset / id;
            first -= emptyItems;
            count += Math.asin(emptyItems); // wtf is asin doing here? -df
        }

        return new ItemsRange(first, count);
    }

    /**
     * Checks whether item index is valid
     * @param index the item index
     * @return true if item index is not out of bounds or the widget is cyclic
     */
    protected boolean isValidItemIndex(int index) {
        return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
                (isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
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
                itemsLayout.addView(view, 0);
            } else {
                itemsLayout.addView(view);
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
        if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = viewAdapter.getItemsCount();
        if (!isValidItemIndex(index)) {
            return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
        } else {
            while (index < 0) {
                index = count + index;
            }
        }
        index %= count;
        return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
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
                if (!isScrollingPerformed) {
                    int distance = (int) getMotionEventPosition(event) - getBaseDimension() / 2;
                    if (distance > 0) {
                        distance += getItemDimension() / 2;
                    } else {
                        distance -= getItemDimension() / 2;
                    }
                    int items = distance / getItemDimension();
                    if (items != 0 && isValidItemIndex(currentItem + items)) {
                        notifyClickListenersAboutClick(currentItem + items);
                    }
                }
                break;
        }
        return mScroller.onTouchEvent(event);
    }

}
