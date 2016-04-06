package antistatic.spinnerwheel.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.adapters.AbstractWheelCustomAdapter;

/**
 * Created by maytree on 2016. 4. 6..
 */
public class ColorPickerActivity extends Activity {

    int[] colors = {R.color.pink_500, R.color.deepOrange_500, R.color.amber_500, R.color.lightBlue_500, R.color.deepPurple_500};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.color_picker_custom);

        final AbstractWheel colorPicker = (AbstractWheel) findViewById(R.id.color_picker);
        colorPicker.setViewAdapter(new ColorAdapter(this, colors));
    }

    /**
     * Color adapter
     *
     */
    public class ColorAdapter extends AbstractWheelCustomAdapter {
        private Context context;
        private int data[];

        public ColorAdapter(Context context, int[] data) {
            super(context, R.layout.color_picker_custom_item);

            this.context = context;
            this.data = data;
        }

        @Override
        protected void configureItemView(View view, int index) {
            View colorBarView = view.findViewById(R.id.color_bar);

            colorBarView.setBackgroundColor(context.getResources().getColor(data[index]));
        }

        @Override
        public int getItemsCount() {
            return data.length;
        }
    }

}
