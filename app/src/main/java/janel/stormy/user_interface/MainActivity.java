package janel.stormy.user_interface;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import janel.stormy.R;
import janel.stormy.weather_pkg.Current;
import janel.stormy.weather_pkg.Day;
import janel.stormy.weather_pkg.Forecast;
import janel.stormy.weather_pkg.Hour;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class MainActivity extends ActionBarActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";
    public static final String CURRENT_LOCATION = "CURRENT_LOCATION";
    public String USER_LOCATION = "Current Location";

    private double mLatitude;
    private double mLongitude;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    LocationAvailability mLocationAvailability;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    Geocoder mGeocoder;
    List<Address> mAddress;
    String mCity;
    private Forecast mForecast;

    /*Butterknife API injects the appropriate information from variables in the code into the
    * corresponding xml id.  This removes the need to associate ids with variables after they
    * have been instantiated.*/
    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidVal;
    @InjectView(R.id.precipValue) TextView mPrecipVal;
    @InjectView(R.id.iconImageView)ImageView mIconImageView;
    @InjectView(R.id.summaryTextView) TextView mSummary;
    @InjectView(R.id.refreshImageView) ImageView mRefresh;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;
    @InjectView(R.id.locationLabel) TextView mLocationLabel;

    /*Called from onCreate(). This function creates a new request with information on speed
    * and accuracy of the results.*/
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    /*The onCreate() method checks to see if GooglePlay services is available,
    * calls method to build Google API Client object, calls method to create a
    * location request, instantiates the Geocoder object (which performs reverse
    * geocoding to get a String place name from lat/long coords), associates this
    * activity with main page, starts the Butterknife injection process, makes the
    * progress bar invisible and refresh button visible, and puts an onClickListener
    * on the refresh button.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        buildGoogleApiClient();

        createLocationRequest();

        mGeocoder = new Geocoder(this);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);
        mRefresh.setVisibility(View.VISIBLE);

        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(mLatitude, mLongitude);
            }
        });
    }

    @OnClick (R.id.weeklyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this, DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        intent.putExtra(CURRENT_LOCATION, USER_LOCATION);
        startActivity(intent);
    }

    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this, HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        startActivity(intent);
    }

    /*Checks if network is available, makes progress bar visible/refresh invisible, uses
    * OkHttp for asynchronous request, and either gets JSON object from the request or
    * prints a Toast error.*/
    private void getForecast(double lat, double lon) {
        String apiKey = "6753ec3c6f64895b42b354adc1c7653b";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + lat + "," + lon;

        if (isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.INVISIBLE);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "IO Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, getString(R.string.error_toast_no_network), Toast.LENGTH_LONG).show();
        }
    }

    /*Sets appropriate variables used by XML to the correct values to be displayed.*/
    private void updateDisplay() {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRefresh.setVisibility(View.VISIBLE);
        mTemperatureLabel.setText(mForecast.getCurrent().getTemp() + "");
        mTimeLabel.setText("At " + mForecast.getCurrent().getFormattedTime() + " it is");
        mHumidVal.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipVal.setText(mForecast.getCurrent().getPrecipChance() + "%");
        mSummary.setText(mForecast.getCurrent().getSummary() + "");
        Drawable drawable = getResources().getDrawable(mForecast.getCurrent().getIconId());
        mIconImageView.setImageDrawable(drawable);
        mLocationLabel.setText(mCity);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");
        Day[] days = new Day[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            Log.d(TAG, "Day temperature max = " + day.getTemperatureMax());
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);
            day.setIcon(jsonDay.getString("icon"));

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");
        Hour[] hours = new Hour[data.length()];

        for (int i = 0; i < data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            Log.d(TAG, "Hour temperature = " + hour.getTemperature());
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);
            hour.setIcon(jsonHour.getString("icon"));

            hours[i] = hour;
        }

        return hours;
    }

    /*Called from getForecast().  Saves needed details from JSON object into a
    * Current object using a rather large constructor.*/
    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currentForecast = forecast.getJSONObject("currently");
        Current current = new Current();
        current.setIcon(currentForecast.getString("icon"));
        current.setTime(currentForecast.getLong("time"));
        current.setHumidity(currentForecast.getDouble("humidity"));
        current.setTemp(currentForecast.getDouble("temperature"));
        current.setPrecipChance(currentForecast.getDouble("precipProbability"));
        current.setSummary(currentForecast.getString("summary"));
        current.setTimeZone(forecast.getString("timezone"));

        Log.d(TAG, "NEW TIME: " + current.getFormattedTime());
        return current;
    }

    /*Checks if network is availaable and returns boolean.*/
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        Boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    /*Creates a dialog to inform user of error.*/
    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    /*Creates a GoogleApiClient object */
    protected void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /*On start, checks if GooglePlay servies are available, and if so, connects the
    * GoogleApiClient.*/
    @Override
    public void onStart() {

        Log.d(TAG, "In onStart..................");

        super.onStart();

        if(isGooglePlayServicesAvailable()) {
            mGoogleApiClient.connect();
        }
    }

    /*On stop, disconnects GoogleApiClient.*/
    @Override
    protected void onStop() {
        Log.d(TAG, "In onStop ..................");

        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /*On connecting, uses FusedLocationApi to see if location is available. If so, saves it
    * and calls updateLocation().  It then does reverse geocoding on the address and saves
    * the city and state as a variable string and calls getForecast().*/
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "In onConnected ..................");
        mLocationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);

        if(mLocationAvailability != null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        updateLocation();

        try {
            mAddress = mGeocoder.getFromLocation(mLatitude, mLongitude, 1);
            Log.d(TAG, "Address: " + mAddress.get(0).getAddressLine(1) + "................");
            mCity = mAddress.get(0).getAddressLine(1);
        } catch (IOException e) {
            Log.d(TAG, "Geocoder did not get a location: " + e + "................");
        }

        if (mCity != null) {
            String delims = "[ ]";
            String[] tokens = mCity.split(delims);
            mCity = tokens[0] + " " + tokens[1];
        } else {
            mCity = "Current weather";
        }

        USER_LOCATION = mCity;

        getForecast(mLatitude, mLongitude);
    }

    /*On resuming, GoogleApiClient is connected.*/
    @Override
    protected void onResume() {
        Log.d(TAG, "In onResume ..................");
        super.onResume();
        mGoogleApiClient.connect();
    }

    /*On pausing, GoogleApiClient is disconnected if it was connected.*/
    @Override
    protected void onPause() {
        Log.d(TAG, "In onPause ..................");
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /*On connection suspension, log message.*/
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "In onConnectionSuspended ..................");
    }

    /*On location change, call update location.*/
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "In onLocationChanged ..................");
        updateLocation();
    }

    /*On connectin fail, log message.*/
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "In onConnectionFailed ..................");
    }

    /*Checks GooglePlay service's status.  If success, return true, else return false.*/
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    /*If we have a location, assign variables for lat/long.*/
    private void updateLocation() {

        if (null != mCurrentLocation) {
            mLatitude = mCurrentLocation.getLatitude();
            mLongitude = mCurrentLocation.getLongitude();
            Log.d(TAG, "GOT NEW INFO: new latitude: " + mLatitude);
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }
}
