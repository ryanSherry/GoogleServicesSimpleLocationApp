package simplelocationapp.example.com.googleservicessimplelocationapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

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
}
