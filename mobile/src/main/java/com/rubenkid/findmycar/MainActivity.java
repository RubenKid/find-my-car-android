package com.rubenkid.findmycar;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, android.location.LocationListener, GoogleMap.OnMapLoadedCallback {

    private final static int PERMISSIONS_REQUEST = 1234;

    private GoogleMap mMap;
    private MarkerOptions mMeMarker;
    private LocationManager mLocationManager;
    private Location lastLocation;
    private String selectedProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Initialize the location manager that will be the one in charge of retrieving user current location
     */
    private void initLocationManager() throws SecurityException{
        if (hasLocationPermissions() && isGPSIsEnabled()) {
            Criteria criteria = new Criteria();
            selectedProvider = mLocationManager.getBestProvider(criteria, false);
            mLocationManager.requestLocationUpdates(selectedProvider, 400, 1, this);
            lastLocation = mLocationManager.getLastKnownLocation(selectedProvider);
            onLocationChanged(lastLocation);
        }
    }

    /**
     * Check if the user has GPS enabled. If not, it will prompt a dialog asking him/her to enable it via settings
     * @return Boolean indicating whether the user has GPS enabled or not
     */
    private Boolean isGPSIsEnabled() {
        boolean enabled = mLocationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            new AlertDialog.Builder(this)
                    .setTitle("GPS is not enabled")
                    .setMessage("We need you to enable GPS to make this work. Please go to Settings and enable it")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        return enabled;
    }

    /**
     * Check if user has location permissions enabled. If not, we will show a dialog enforces him to accept them
     * @return Boolean indicating whether the user has permissions accepted or not
     */
    private boolean hasLocationPermissions() {
        boolean granted = true;
        String[] dangerousPermissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ActivityCompat.checkSelfPermission(this, dangerousPermissions[0]) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, dangerousPermissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    dangerousPermissions,
                    PERMISSIONS_REQUEST);
            granted = false;
        } else {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        return granted;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if(mMap != null) {
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            if(mMeMarker == null) {
                mMeMarker = new MarkerOptions().position(latlng);
                mMap.addMarker(mMeMarker);
            } else {
                mMeMarker.position(latlng);
            }

            //Move camera to fit all markers
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mMeMarker.getPosition());
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
            mMap.moveCamera(cu);
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                initLocationManager();
                // If request is cancelled, the result arrays are empty.
                /*if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }*/
                return;
            }
        }
    }

    @Override
    public void onMapLoaded() {
        Log.d("kk", "Map Loaded");
        initLocationManager();
    }
}
