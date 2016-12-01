package antistatic.spinnerwheel.demo;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.OnWheelScrollListener;
import antistatic.spinnerwheel.adapters.AbstractWheelTextAdapter;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class CitiesActivity extends Activity {
    // Scrolling flag
    private boolean scrolling = false;

    private int mActiveCities[] = new int[] {
            1, 1, 1, 1
    };
    
    private int mActiveCountry;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.repopulating_data);
                
        final AbstractWheel country = (AbstractWheel) findViewById(R.id.country);
        country.setVisibleItems(3);
        country.setViewAdapter(new CountryAdapter(this));

        final String cities[][] = new String[][] {
                new String[] {"New York", "Washington", "Chicago", "Atlanta", "Orlando", "Los Angeles", "Houston", "New Orleans"},
                new String[] {"Ottawa", "Vancouver", "Toronto", "Windsor", "Montreal", "Calgary", "Winnipeg", "Edmonton"},
                new String[] {"Kyiv", "Simferopol", "Lviv", "Kharkiv", "Odessa", "Mariupol", "Lugansk", "Sevastopol"},
                new String[] {"Paris", "Bordeaux", "Le Mans", "Orleans", "Valence", "Amiens", "Rouen", "Touluse", "La Rochelle"},
        };


        final AbstractWheel city = (AbstractWheel) findViewById(R.id.city);
        city.setVisibleItems(5);

        city.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    mActiveCities[mActiveCountry] = newValue;
                }
            }
        });

        country.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    updateCities(city, cities, newValue);
                }
            }
        });
        
        country.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(AbstractWheel wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(AbstractWheel wheel) {
                scrolling = false;
                updateCities(city, cities, country.getCurrentItem());
            }
        });

        country.setCurrentItem(1);
    }
    
    /**
     * Updates the city spinnerwheel
     */
    private void updateCities(AbstractWheel city, String cities[][], int index) {
        mActiveCountry = index;
        ArrayWheelAdapter<String> adapter =
            new ArrayWheelAdapter<String>(this, cities[index]);
        adapter.setTextSize(18);
        city.setViewAdapter(adapter);
        city.setCurrentItem(mActiveCities[index]);
    }
    
    /**
     * Adapter for countries
     */
    private class CountryAdapter extends AbstractWheelTextAdapter {
        // Countries names
        private String countries[] =
            new String[] {"USA", "Canada", "Ukraine", "France"};
        // Countries flags
        private int flags[] =
            new int[] {R.drawable.usa, R.drawable.canada, R.drawable.ukraine, R.drawable.france};
        
        /**
         * Constructor
         */
        protected CountryAdapter(Context context) {
            super(context, R.layout.country_item, NO_RESOURCE);
            
            setItemTextResource(R.id.country_name);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent, int currentItemIdx) {
            View view = super.getItem(index, cachedView, parent, currentItemIdx);
            ImageView img = (ImageView) view.findViewById(R.id.flag);
            img.setImageResource(flags[index]);
            return view;
        }
        
        @Override
        public int getItemsCount() {
            return countries.length;
        }
        
        @Override
        protected CharSequence getItemText(int index) {
            return countries[index];
        }
    }
}
