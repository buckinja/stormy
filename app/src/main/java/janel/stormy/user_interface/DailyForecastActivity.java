package janel.stormy.user_interface;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import janel.stormy.R;
import janel.stormy.weather_pkg.Day;
import janel.stormy.adapters.DayAdapter;

public class DailyForecastActivity extends ListActivity {

    public static final String TAG = DailyForecastActivity.class.getSimpleName();
    private Day[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        final String userLocation = intent.getStringExtra(MainActivity.CURRENT_LOCATION);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapter adapter = new DayAdapter(this, mDays);
        setListAdapter(adapter);

        final TextView mLocationLabelDaily =(TextView) findViewById(R.id.locationLabelDaily);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLocationLabelDaily.setText(userLocation); //MainActivity.USER_LOCATION
            }
        });
    }
}
