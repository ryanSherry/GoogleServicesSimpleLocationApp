package simplelocationapp.example.com.googleservicessimplelocationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity implements LocationProvider.LocationCallback{

    LocationProviderRefresh mLocationProviderRefresh;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationManager locationManager;

    TextView locationView;
    TextView latitudeResultView;
    TextView longitudeResultView;
    Button refreshLocationBtn;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout rootSettingsLayout = findViewById(R.id.settings_activity_root_view);
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar,rootSettingsLayout, false);
        rootSettingsLayout.addView(bar, 0);

        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        final LocationCallback locationCallback = new LocationCallback() {
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

                    Geocoder mGeocoder = new Geocoder(SettingsActivity.this,Locale.getDefault());

                    try {
                        List<Address> address = mGeocoder.getFromLocation(lattitude,longitude,1);

                        String city = address.get(0).getLocality();
                        String state = address.get(0).getAdminArea();

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("CITY_STATE", city + "," + state);
                        editor.putString("LAT", lattitude.toString());
                        editor.putString("LON", longitude.toString());
                        editor.apply();

                        String locationResultString = sharedPreferences.getString("CITY_STATE", "San Diego, CA");
                        String latitudeResult = sharedPreferences.getString("LAT", "999");
                        String longitudeResult = sharedPreferences.getString("LON", "999");

                        locationView.setText(locationResultString);
                        latitudeResultView.setText(latitudeResult);
                        longitudeResultView.setText(longitudeResult);

                        mLocationProviderRefresh.disconnect(fusedLocationProviderClient, this);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };

        locationView = findViewById(R.id.location_result);
        latitudeResultView = findViewById(R.id.latitude_result);
        longitudeResultView = findViewById(R.id.longitude_result);
        refreshLocationBtn = findViewById(R.id.updateLocation);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        String locationResult = sharedPref.getString("CITY_STATE", "San Diego, CA");
        String latitudeResult = sharedPref.getString("LAT", "999");
        String longitudeResult = sharedPref.getString("LON", "999");

        locationView.setText(locationResult);
        latitudeResultView.setText(latitudeResult);
        longitudeResultView.setText(longitudeResult);

        mLocationProviderRefresh = new LocationProviderRefresh(this,this, fusedLocationProviderClient,locationCallback, locationManager);


        refreshLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLocationProviderRefresh.connect();
            }
        });

    }

    @Override
    public void handleNewLocation(Location location) {
        Timber.i("Results:" + Double.toString(location.getLatitude()) + Double.toString(location.getLongitude()));
        Double lattitude = location.getLatitude();
        Double longitude = location.getLongitude();
        //Do stuff here like save location to preferences

    }
}
