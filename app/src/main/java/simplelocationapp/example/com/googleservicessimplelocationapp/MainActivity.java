package simplelocationapp.example.com.googleservicessimplelocationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback  {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 11;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 999;

    TextView lattitudeText;
    TextView longitudeText;

    FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;

    Location mLastLocation;
    private LocationRequest mLocationRequest;

    LocationProvider mLocationProvider;
    LocationCallback mLocationCallback;

    LocationManager mLocationManager;

    Geocoder mGeocoder;
    List<Address> mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Timber.plant(new Timber.DebugTree());

        lattitudeText = findViewById(R.id.lattitude);
        longitudeText = findViewById(R.id.longitude);

        mFusedLocationProviderClient = new FusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Timber.i("Results:" + Double.toString(location.getLatitude()) + Double.toString(location.getLongitude()));
                    Double lattitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    //Do stuff here like save location to preferences
                    mGeocoder = new Geocoder(MainActivity.this,Locale.getDefault());

                    try {
                        mAddress = mGeocoder.getFromLocation(lattitude,longitude,1);

                        String city = mAddress.get(0).getLocality();
                        String state = mAddress.get(0).getAdminArea();

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("CITY_STATE", city + "," + state);
                        editor.putString("LAT", lattitude.toString());
                        editor.putString("LON", longitude.toString());
                        editor.apply();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mLocationProvider = new LocationProvider(this,this, mFusedLocationProviderClient,mLocationCallback, mLocationManager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationProvider.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.disconnect(mFusedLocationProviderClient,mLocationCallback);
    }


    @Override
    public void handleNewLocation(Location location) {
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        lattitudeText.setText(Double.toString(currentLatitude));
        longitudeText.setText(Double.toString(currentLongitude));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_acitvity_action_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.settings:
            launchActivity(SettingsActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchActivity(Class<?> classToLaunch) {
        Intent intent = new Intent(this, classToLaunch);
        this.startActivity(intent);
    }
}
