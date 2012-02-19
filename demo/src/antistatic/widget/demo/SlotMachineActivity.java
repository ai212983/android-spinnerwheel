package antistatic.widget.demo;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import antistatic.widget.wheel.AbstractWheel;
import antistatic.widget.wheel.OnWheelChangedListener;
import antistatic.widget.wheel.OnWheelScrollListener;
import antistatic.widget.wheel.adapters.AbstractWheelAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SlotMachineActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.slot_machine_layout);
        initWheel(R.id.slot_1);
        initWheel(R.id.slot_2);
        initWheel(R.id.slot_3);
        
        Button mix = (Button)findViewById(R.id.btn_mix);
        mix.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mixWheel(R.id.slot_1);
                mixWheel(R.id.slot_2);
                mixWheel(R.id.slot_3);
            }
        });
        
        updateStatus();
    }
    
    // Wheel scrolled flag
    private boolean wheelScrolled = false;
    
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(AbstractWheel wheel) {
            wheelScrolled = true;
        }
        public void onScrollingFinished(AbstractWheel wheel) {
            wheelScrolled = false;
            updateStatus();
        }
    };
    
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
            if (!wheelScrolled) {
                updateStatus();
            }
        }
    };
    
    /**
     * Updates status
     */
    private void updateStatus() {
        TextView text = (TextView) findViewById(R.id.pwd_status);
        if (test()) {
            text.setText("Congratulation!");
        } else {
            text.setText("");
        }
    }

    /**
     * Initializes widget
     * @param id the widget wheel Id
     */
    private void initWheel(int id) {
        AbstractWheel wheel = getWheel(id);
        wheel.setViewAdapter(new SlotMachineAdapter(this));
        wheel.setCurrentItem((int)(Math.random() * 10));
        
        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(false);
    }
    
    /**
     * Returns widget by Id
     * @param id the widget Id
     * @return the widget with passed Id
     */
    private AbstractWheel getWheel(int id) {
        return (AbstractWheel) findViewById(id);
    }
    
    /**
     * Tests wheels
     * @return true 
     */
    private boolean test() {
        int value = getWheel(R.id.slot_1).getCurrentItem();
        return testWheelValue(R.id.slot_2, value) && testWheelValue(R.id.slot_3, value);
    }
    
    /**
     * Tests widget value
     * @param id the widget Id
     * @param value the value to test
     * @return true if widget value is equal to passed value
     */
    private boolean testWheelValue(int id, int value) {
        return getWheel(id).getCurrentItem() == value;
    }
    
    /**
     * Mixes widget
     * @param id the widget id
     */
    private void mixWheel(int id) {
        AbstractWheel wheel = getWheel(id);
        wheel.scroll(-350 + (int)(Math.random() * 50), 2000);
    }
    
    /**
     * Slot machine adapter
     */
    private class SlotMachineAdapter extends AbstractWheelAdapter {
        // Image size
        final int IMAGE_WIDTH = 60;
        final int IMAGE_HEIGHT = 36;
        
        // Slot machine symbols
        private final int items[] = new int[] {
                android.R.drawable.star_big_on,
                android.R.drawable.stat_sys_warning,
                android.R.drawable.radiobutton_on_background,
                android.R.drawable.ic_delete
        };
        
        // Cached images
        private List<SoftReference<Bitmap>> images;
        
        // Layout inflater
        private Context context;
        
        /**
         * Constructor
         */
        public SlotMachineAdapter(Context context) {
            this.context = context;
            images = new ArrayList<SoftReference<Bitmap>>(items.length);
            for (int id : items) {
                images.add(new SoftReference<Bitmap>(loadImage(id)));
            }
        }
        
        /**
         * Loads image from resources
         */
        private Bitmap loadImage(int id) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, true);
            bitmap.recycle();
            return scaled;
        }

        @Override
        public int getItemsCount() {
            return items.length;
        }

        // Layout params for image view
        final LayoutParams params = new LayoutParams(IMAGE_WIDTH, IMAGE_HEIGHT);
        
        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            ImageView img;
            if (cachedView != null) {
                img = (ImageView) cachedView;
            } else {
                img = new ImageView(context);
            }
            img.setLayoutParams(params);
            SoftReference<Bitmap> bitmapRef = images.get(index);
            Bitmap bitmap = bitmapRef.get();
            if (bitmap == null) {
                bitmap = loadImage(items[index]);
                images.set(index, new SoftReference<Bitmap>(bitmap));
            }
            img.setImageBitmap(bitmap);
            
            return img;
        }
    }
}
