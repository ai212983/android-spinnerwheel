package antistatic.spinnerwheel.demo;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class ProgrammaticSwitchingActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.password);
        initWheel(R.id.passw_1);
        initWheel(R.id.passw_2);
        initWheel(R.id.passw_3);
        initWheel(R.id.passw_4);
        
        Button mix = (Button)findViewById(R.id.btn_mix);
        mix.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mixWheel(R.id.passw_1);
                mixWheel(R.id.passw_2);
                mixWheel(R.id.passw_3);
                mixWheel(R.id.passw_4);
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
     * Updates entered PIN status
     */
    private void updateStatus() {
        TextView text = (TextView) findViewById(R.id.pwd_status);
        if (testPin(2, 4, 6, 1)) {
            text.setText("PIN accepted!");
        } else {
            text.setText("Invalid PIN");
        }
    }

    /**
     * Initializes spinnerwheel
     * @param id the spinnerwheel wheel Id
     */
    private void initWheel(int id) {
        AbstractWheel wheel = getWheel(id);
        wheel.setViewAdapter(new NumericWheelAdapter(this, 0, 9));
        wheel.setCurrentItem((int) (Math.random() * 10));
        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setInterpolator(new AnticipateOvershootInterpolator());
    }
    
    /**
     * Returns spinnerwheel by Id
     * @param id the spinnerwheel Id
     * @return the spinnerwheel with passed Id
     */
    private AbstractWheel getWheel(int id) {
        return (AbstractWheel) findViewById(id);
    }
    
    /**
     * Tests entered PIN
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     * @return true 
     */
    private boolean testPin(int v1, int v2, int v3, int v4) {
        return testWheelValue(R.id.passw_1, v1) && testWheelValue(R.id.passw_2, v2) &&
                testWheelValue(R.id.passw_3, v3) && testWheelValue(R.id.passw_4, v4);
    }
    
    /**
     * Tests spinnerwheel value
     * @param id the spinnerwheel Id
     * @param value the value to test
     * @return true if spinnerwheel value is equal to passed value
     */
    private boolean testWheelValue(int id, int value) {
        return getWheel(id).getCurrentItem() == value;
    }
    
    /**
     * Mixes spinnerwheel
     * @param id the spinnerwheel id
     */
    private void mixWheel(int id) {
        AbstractWheel wheel = getWheel(id);
        wheel.scroll(-25 + (int)(Math.random() * 50), 2000);
    }
}
