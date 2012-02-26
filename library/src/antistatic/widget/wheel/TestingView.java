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
public class TestingView extends View {


    //----------------------------------
    //  Class properties
    //----------------------------------

    private LinearLayout mLayout;
    private Bitmap mBitmap;

    @SuppressWarnings("unused")
    private final String LOG_TAG = TestingView.class.getName();

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
    public TestingView(Context context) {
        this(context, null);
    }

    public TestingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayout = new LinearLayout(getContext());
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
    }

    /**
     * Create a new wheel horizontal view.
     *
     * @param context the application environment.
     * @param attrs a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public TestingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

       // this.setOrientation(LinearLayout.HORIZONTAL);
       // setWillNotDraw(false);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /*
        if (mViewAdapter != null && mViewAdapter.getItemsCount() > 0) {
            if (rebuildItems()) {
                measureLayout();
                //doItemsLayout();
            }
           // drawItems(canvas);
        }*/
    }


}
