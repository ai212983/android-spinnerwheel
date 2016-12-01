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

package antistatic.spinnerwheel.demo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.widget.TextView;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;
import antistatic.spinnerwheel.demo.R;

/**
 * Numeric Wheel adapter.
 */
public class CustomNumericWheelAdapter extends NumericWheelAdapter {
    public CustomNumericWheelAdapter(Context context) {
        super(context);
        sharedConstructor();
    }

    public CustomNumericWheelAdapter(Context context, int minValue, int maxValue) {
        super(context, minValue, maxValue);
        sharedConstructor();
    }

    public CustomNumericWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context, minValue, maxValue, format);
        sharedConstructor();
    }

    public CustomNumericWheelAdapter(Context context, int minValue, int maxValue, IntParamFunction<String> formatFunction) {
        super(context, minValue, maxValue, formatFunction);
        sharedConstructor();
    }

    private void sharedConstructor() {
        itemResourceId = R.layout.item_custom_text_view;
        itemTextResourceId = R.id.tv;
        emptyItemResourceId = R.layout.item_custom_text_view;
        setTextColor(Color.parseColor("#999999"));
    }

    @Override protected void onConfigureTextView(TextView textView, boolean isSelectedItem) {
        super.onConfigureTextView(textView, isSelectedItem);
        if(isSelectedItem) {
            textView.setTextColor(Color.parseColor("#333333"));
        } else {
            textView.setTextColor(getTextColor());
        }
    }

    @Override protected int getDefaultTextStyle() {
        return Typeface.NORMAL;
    }
}
