package antistatic.spinnerwheel.demo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.AbstractWheelView;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.OnWheelClickedListener;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.WheelVerticalView;
import antistatic.spinnerwheel.adapters.NumericWheelAdapter;
import antistatic.spinnerwheel.demo.adapter.CustomNumericWheelAdapter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Xavier.S
 * @date 2016.11.30 17:57
 */

public class DateTimePickerActivity extends Activity {

    // Time changed flag
    private boolean timeChanged = false;

    // Time scrolled flag
    private boolean timeScrolled = false;
    private TextView          tv;
    public  WheelVerticalView wheel_hour;
    public  WheelVerticalView wheel_min;
    private WheelVerticalView wheel_date;
    public  TimePicker        mPicker;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gradient_size_and_color_picker);

        initReferences();
        initListeners();
        initViews();
        bind();
    }

    private void bind() {
        wheel_date.setViewAdapter(new CustomNumericWheelAdapter(this,
          0,
          Integer.MAX_VALUE - 2,// in fact, the largest accept max value is MAX_VALUE - 2, but it is enough for finite scroll.
          new NumericWheelAdapter.IntParamFunction<String>() {
              Date mDate = new Date();
              SimpleDateFormat mSimpleDateFormatHalf = new SimpleDateFormat("MM.dd", Locale.US);
              SimpleDateFormat mSimpleDateFormatFull =
                new SimpleDateFormat("yyyy.MM.dd", Locale.US);

              @Override public String apply(int i) {
                  // set current time
                  mDate.setTime(TimeUnit.DAYS.toMillis(i));
                  if (mDate.getYear() == 2016) {
                      return mSimpleDateFormatHalf.format(mDate);
                  }
                  return mSimpleDateFormatFull.format(mDate);
              }
          }));
        wheel_hour.setViewAdapter(new CustomNumericWheelAdapter(this, 0, 23));
        wheel_min.setViewAdapter(new CustomNumericWheelAdapter(this, 0, 59, "%02d"));

        // set current time
        Calendar c = Calendar.getInstance();
        int curHours = c.get(Calendar.HOUR_OF_DAY);
        int curMinutes = c.get(Calendar.MINUTE);

        wheel_date.setCurrentItem((int) TimeUnit.MILLISECONDS.toDays(c.getTimeInMillis()));
        wheel_hour.setCurrentItem(curHours);
        wheel_min.setCurrentItem(curMinutes);

        mPicker.setCurrentHour(curHours);
        mPicker.setCurrentMinute(curMinutes);
    }

    private void initReferences() {
        tv = (TextView) findViewById(R.id.tv);

        wheel_date = (WheelVerticalView) findViewById(R.id.wheel_date);

        wheel_hour = (WheelVerticalView) findViewById(R.id.wheel_hour);

        wheel_min = (WheelVerticalView) findViewById(R.id.wheel_min);

        mPicker = (TimePicker) findViewById(R.id.time);
    }

    private void initViews() {
        initWheelsStyle();

        mPicker.setIs24HourView(true);
    }

    private void initWheelsStyle() {
        initWheelStyle(wheel_date);
        wheel_date.setCyclic(false);

        initWheelStyle(wheel_hour);
        initWheelStyle(wheel_min);
    }

    private void initWheelStyle(AbstractWheelView wheel) {
        wheel.setCyclic(true);
        wheel.setVisibleItems(10);
        wheel.setActiveCoeff(0.8f);
        wheel.setPassiveCoeff(0.6f);

        // test
        wheel.setSelectionDivider(new ColorDrawable(Color.BLACK));
    }

    private void initListeners() {
        // add listeners
        addChangingListener(wheel_min, "min");
        addChangingListener(wheel_hour, "hour");

        OnWheelChangedListener wheelListener = new OnWheelChangedListener() {
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!timeScrolled) {
                    timeChanged = true;
                    mPicker.setCurrentHour(wheel_hour.getCurrentItem());
                    mPicker.setCurrentMinute(wheel_min.getCurrentItem());
                    timeChanged = false;
                }
            }
        };
        wheel_hour.addChangingListener(wheelListener);
        wheel_min.addChangingListener(wheelListener);

        OnWheelClickedListener click = new OnWheelClickedListener() {
            public void onItemClicked(AbstractWheel wheel, int itemIndex) {
                wheel.setCurrentItem(itemIndex, true);
            }
        };
        wheel_date.addClickingListener(click);
        wheel_hour.addClickingListener(click);
        wheel_min.addClickingListener(click);

        OnWheelScrollListener scrollListener = new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {
                timeScrolled = true;
            }

            public void onScrollingFinished(AbstractWheel wheel) {
                timeScrolled = false;
                timeChanged = true;
                mPicker.setCurrentHour(wheel_hour.getCurrentItem());
                mPicker.setCurrentMinute(wheel_min.getCurrentItem());
                timeChanged = false;
            }
        };

        wheel_hour.addScrollingListener(scrollListener);
        wheel_min.addScrollingListener(scrollListener);

        mPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (!timeChanged) {
                    wheel_hour.setCurrentItem(hourOfDay, true);
                    wheel_min.setCurrentItem(minute, true);
                }
            }
        });

        tv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // test edge
                wheel_date.setCurrentItem(0);
                wheel_date.invalidateItemsLayout(false);
                //wheel_date.setCurrentItem(Integer.MAX_VALUE - 2);
            }
        });
    }

    /**
     * Adds changing listener for spinnerwheel that updates the spinnerwheel label
     * @param wheel the spinnerwheel
     * @param label the spinnerwheel label
     */
    private void addChangingListener(final AbstractWheel wheel, final String label) {
        wheel.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                //spinnerwheel.setLabel(newValue != 1 ? label + "s" : label);
            }
        });
    }
}
