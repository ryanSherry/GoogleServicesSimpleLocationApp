package simplelocationapp.example.com.googleservicessimplelocationapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import timber.log.Timber;

public class LocationProvider implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 11;

    public abstract interface LocationCallback {
        public void handleNewLocation(Location location);
    }

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private LocationCallback mLocationCallback;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Location mLocation;
    private com.google.android.gms.location.LocationCallback gmsLocationCallback;
    private LocationManager mLocationManager;

    public LocationProvider(LocationCallback locationCallback, Context context, FusedLocationProviderClient fusedLocationProviderClient, com.google.android.gms.location.LocationCallback gmsLocationCallback, LocationManager locationManager) {
        mGoogleApiClient =  new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationCallback = locationCallback;
        mContext = context;
        mFusedLocationProviderClient = fusedLocationProviderClient;
        this.gmsLocationCallback = gmsLocationCallback;
        mLocationManager = locationManager;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocationCallback.handleNewLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (checkPermissions((Activity)mContext)) {

            mFusedLocationProviderClient.flushLocations();
            getLastLocation(mFusedLocationProviderClient);

            if (mLocation == null) {
                requestLocationUpdates(mFusedLocationProviderClient, gmsLocationCallback,mLocationManager,LocationProvider.this);
            }
//            getLastLocation(mFusedLocationProviderClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution() && mContext instanceof Activity) {
            try {
                Activity activity = (Activity) mContext;
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Timber.i("Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect(FusedLocationProviderClient fusedLocationProviderClient, com.google.android.gms.location.LocationCallback locationCallback) {
        if (mGoogleApiClient.isConnected()) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            mGoogleApiClient.disconnect();
        }
    }

    public boolean checkPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(activity);
            return false;
        }
    }

    public void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(final FusedLocationProviderClient fusedLocationProviderClient) {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLocation = location;
                            onLocationChanged(location);
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates(FusedLocationProviderClient fusedLocationProviderClient, com.google.android.gms.location.LocationCallback locationCallback, LocationManager locationManager, LocationListener listener) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);

        if(checkPermissions((Activity)mContext)) {
//            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback , Looper.myLooper());
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }
    }


}
