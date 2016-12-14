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

package antistatic.spinnerwheel.adapters;

import android.content.Context;
import android.database.DataSetObserver;

/**
 * Numeric Wheel adapter.
 */
public class NumericWheelAdapter extends AbstractWheelTextAdapter {

    public interface IntParamFunction<R> {
        R apply(int i);
    }

    /** The default min value */
    public static final int DEFAULT_MAX_VALUE = 9;

    /** The default max value */
    private static final int DEFAULT_MIN_VALUE = 0;

    // Values
    private int minValue;
    private int maxValue;

    private int mItemCountTemp;

    // format
    private IntParamFunction<String> formatFunction;

    /**
     * Constructor
     * @param context the current context
     */
    public NumericWheelAdapter(Context context) {
        this(context, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the spinnerwheel min value
     * @param maxValue the spinnerwheel max value
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue) {
        this(context, minValue, maxValue, (String) null);
    }

    /**
     * Constructor
     * @param context the current context
     * @param minValue the spinnerwheel min value
     * @param maxValue the spinnerwheel max value
     * @param format the format string
     */
    public NumericWheelAdapter(Context context, int minValue, int maxValue, final String format) {
        this(context, minValue, maxValue, new IntParamFunction<String>() {
            @Override public String apply(int i) {
                if (format == null) {
                    return Integer.toString(i);
                }
                return String.format(format, i);
            }
        });
    }

    public NumericWheelAdapter(Context context, int minValue, int maxValue, IntParamFunction<String> formatFunction) {
        super(context);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.formatFunction = formatFunction;

        registerDataSetObserver(new DataSetObserver() {
            @Override public void onInvalidated() {
                super.onInvalidated();
                mItemCountTemp = -1;
            }
        });
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        notifyDataInvalidatedEvent();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        notifyDataInvalidatedEvent();
    }

    @Override public CharSequence getItemText(int index) {
        if (index >= 0 && index < getItemsCount()) {
            int value = minValue + index;
            return formatFunction != null ? formatFunction.apply(value) : Integer.toString(value);
        }
        return null;
    }

    @Override public int getItemsCount() {
        if (mItemCountTemp > 0) {
            return mItemCountTemp;
        }
        mItemCountTemp = addExact(addExact(maxValue, negateExact(minValue)), 1);
        return mItemCountTemp;
    }

    /**
     * Copied from java.lang.Math.class in JDK 8.
     *
     * Returns the sum of its arguments,
     * throwing an exception if the result overflows an {@code int}.
     *
     * @param x the first value
     * @param y the second value
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int addExact(int x, int y) {
        int r = x + y;
        // HD 2-12 Overflow iff both arguments have the opposite sign of the result
        if (((x ^ r) & (y ^ r)) < 0) {
            throw new ArithmeticException("integer overflow");
        }
        return r;
    }

    /**
     * Returns the negation of the argument, throwing an exception if the
     * result overflows an {@code int}.
     *
     * @param a the value to negate
     * @return the result
     * @throws ArithmeticException if the result overflows an int
     * @since 1.8
     */
    public static int negateExact(int a) {
        if (a == Integer.MIN_VALUE) {
            throw new ArithmeticException("integer overflow");
        }

        return -a;
    }
}
