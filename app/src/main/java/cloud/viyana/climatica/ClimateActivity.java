package cloud.viyana.climatica;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.ResponseHandler;

public class ClimateActivity extends AppCompatActivity {
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    final String APP_ID = "2f99f8ce1c5fc6db6e9e4d2414b6c7b2";
    final int REQUEST_CODE = 101;
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    final private String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
    final private String CLIMATICA_LOGCAT = "Climatica";

    private TextView mCityLabel;
    private ImageView mWeatherImage;
    private TextView mTemperatureLabel;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate);

        mCityLabel = findViewById(R.id.city_label);
        mTemperatureLabel = findViewById(R.id.temp_label);
        mWeatherImage = findViewById(R.id.main_weather_icon);
        Button displayCity = findViewById(R.id.city_page_btn);

        displayCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(ClimateActivity.this, CityWeatherActivity.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(CLIMATICA_LOGCAT, "onResume Called");
        Intent myIntent = getIntent();
        String city = myIntent.getStringExtra("city");
        if(city != null) {
            getWeatherForNewCity(city);
        } else {
            Log.d(CLIMATICA_LOGCAT, "Getting Current Weather data");
            getWeatherForCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE) {

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(CLIMATICA_LOGCAT, "Permission Granted");
                getWeatherForCurrentLocation();
            } else {
                Log.d(CLIMATICA_LOGCAT, "Permission Denied");
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        getWeatherFromOpenWeather(params);
    }


    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(CLIMATICA_LOGCAT, "onLocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d(CLIMATICA_LOGCAT, longitude);
                Log.d(CLIMATICA_LOGCAT, latitude);


                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                getWeatherFromOpenWeather(params);


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(CLIMATICA_LOGCAT, "onProviderDisabled called, callback received");
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String []{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
     }

     private void getWeatherFromOpenWeather(RequestParams params) {
         AsyncHttpClient client = new AsyncHttpClient();
         client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {
             @Override
             public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                 GsonBuilder mGsonBuilder = new GsonBuilder();
                 Gson gson = mGsonBuilder.create();
                 ClimateDataModel climate = gson.fromJson(String.valueOf(response), ClimateDataModel.class);

                 Main summery = climate.getMain();
                 double temp = (Double) summery.getTemp();
                 List<Weather> weather = climate.getWeather();

                 updateUi(summery, weather, temp, climate);

                 Log.e(CLIMATICA_LOGCAT, "Success "+ response.toString());
                 Log.e(CLIMATICA_LOGCAT, "Temp "+ String.format("%1.2f", (temp - 273.15)));
                 Log.e(CLIMATICA_LOGCAT, "Name "+ climate.getName());
                 Log.e(CLIMATICA_LOGCAT, "Id "+ weather.get(0).getId());
                 Log.e(CLIMATICA_LOGCAT, "Icon "+ ClimateDataModel.updateWeatherIcon(weather.get(0).getId()));
             }

             @Override
             public void onFailure(int statusCode, Header[] headers, String responseString, Throwable e) {
                 super.onFailure(statusCode, headers, responseString, e);
                 Log.e(CLIMATICA_LOGCAT, "Fail "+ e.toString());
                 Log.d(CLIMATICA_LOGCAT, "Response code "+ statusCode);
                 Toast.makeText(ClimateActivity.this, "Request Failed", Toast.LENGTH_SHORT).show();
             }
         });
     }

     private void updateUi(Main main, List<Weather> weather, double temp, ClimateDataModel climate){
        mCityLabel.setText(climate.getName());
        mTemperatureLabel.setText(String.format("%1.2fᵒ", (temp - 273.15)));
        int resourceID = getResources().getIdentifier(ClimateDataModel.updateWeatherIcon(weather.get(0).getId()), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
     }

 }
