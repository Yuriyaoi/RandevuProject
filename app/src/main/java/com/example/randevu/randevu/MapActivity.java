package com.example.randevu.randevu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {
    private static final String TAG = "MapsActivity";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int OVERVIEW = 0;
    private static double lat;
    private static double lon;
    private static String eta;
    private static int etaMin;
    private static String distance;
    private static boolean isTime = true;
    private static boolean isAnnouced = false;
    private static boolean isClicked = false;

    private GoogleMap mGoogleMap;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private DirectionsResult results;
    private Marker mCurrLocationMarker;
    private SupportMapFragment mapFragment;
    private TextView showInfo;
    private Button request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mFusedLocationClient = getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        showInfo = (TextView) findViewById(R.id.tv_info);
        request = (Button) findViewById(R.id.btn_request);
        request.setText("SEND REQUEST");

        request.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                checkTime(eta);
                if(isTime && !isAnnouced){
                    request.setText("ANNOUCED");
                    isAnnouced = true;
                    Log.i("MapsActivity", "Time to annouce");
                } else if(isAnnouced){
                    request.setText("ANNOUCED ALREADY");
                    Log.i("MapsActivity", "announce already");
                } else if(!isTime && !isAnnouced){
                    request.setText("PENDING TO ANNOUCE");
                    isClicked = true;
                    Log.i("MapsActivity", "Waiting to aanouce");
                }else{
                    Log.i("MapsActivity", "WHAT THE CASE ????");
                }
            }
        });
    }

    public void checkTime(String eta){
        if(eta.equals("5 mins")||eta.equals("4 mins")||eta.equals("3 mins")||eta.equals("2 mins")||eta.equals("1 min")){
            isTime = true;
        } else{
            isTime = false;
        }
    }

    private DirectionsResult getDirectionsDetails(String origin, String destination, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        setupGoogleMapScreenSettings(mGoogleMap);
        setCheckPermission(mGoogleMap);
        //getLastLocation();
    }

    private void makeDirectionPath(GoogleMap mGoogleMap){
        Toast.makeText(this, "Current location:\n" + "Latitude: " + lat + "\nLongitude: " + lon, Toast.LENGTH_LONG).show();
        //Log.i("MapsActivity", "Location in MAKE DIRECT BEFORE RESULT " + this.lat + " " + this.lon);
        results = getDirectionsDetails(lat+"," + lon,"13.651712,100.495386",TravelMode.DRIVING);
        if (results != null) {
            addPolyline(results, mGoogleMap);
            positionCamera(results.routes[OVERVIEW], mGoogleMap);
            addMarkersToMap(results, mGoogleMap);
        }
    }


/*    private void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLastLocation = task.getResult();
                                lat = mLastLocation.getLatitude();
                                lon = mLastLocation.getLongitude();
                                Log.i("MapsActivity", "Location in getLASTLOCATION METHOD: " + lat + " " + lon);
                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }*/

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location in  CallBack: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                lat = mLastLocation.getLatitude();
                lon = mLastLocation.getLongitude();
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }
            }
            makeDirectionPath(mGoogleMap);
            if(isClicked) {
                checkTime(eta);
                changeState();
            }
        };
    };

    public void changeState(){
        if(isTime && !isAnnouced){
            request.setText("ANNOUCED");
            isAnnouced = true;
        }
    }
    private void setCheckPermission(GoogleMap mGoogleMap){
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000); // two minute interval
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                getLocationPermission();
            }
        }else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[OVERVIEW].legs[OVERVIEW].endLocation.lat,results.routes[OVERVIEW].legs[OVERVIEW].endLocation.lng)).title(results.routes[OVERVIEW].legs[OVERVIEW].startAddress).snippet(getEndLocationTitle(results)));
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.legs[OVERVIEW].startLocation.lat, route.legs[OVERVIEW].startLocation.lng), 12));
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[OVERVIEW].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
    }

    private String getEndLocationTitle(DirectionsResult results){
        eta = results.routes[OVERVIEW].legs[OVERVIEW].duration.humanReadable;
        distance = results.routes[OVERVIEW].legs[OVERVIEW].distance.humanReadable;
        updateInfo(eta+"");
        return  "Time :"+ eta + " Distance :" + distance;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(getString(R.string.directionsApiKey))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public void updateInfo(String eta){
        showInfo.setText("Time: " + eta);
    }


}
