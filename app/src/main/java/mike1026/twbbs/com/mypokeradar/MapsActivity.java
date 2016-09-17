package mike1026.twbbs.com.mypokeradar;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient = null;
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private double current_latitude = 25.048187;
    private double current_longtitude = 121.517172;
    static double minLatitude = 0.0;
    static double maxLatitude = 0.0;
    static double minLongtitude = 0.0;
    static double maxLongtitude = 0.0;
    private ArrayList<Marker> markerList = new ArrayList<Marker>();

    public Handler UiHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(DEBUG, "handleMessage");
            ArrayList<PokeMon> data = (ArrayList<PokeMon>)msg.getData().getSerializable(QueryDataThread.QUERY_RESULT);
            for(Marker m: markerList)
            {
                m.remove();
            }
            for(PokeMon p: data)
            {
                Marker tmp = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(p.latitude, p.longtitude))
                .title("PokeMon ID" + p.id));
                markerList.add(tmp);
            }
        }
    };

    static String DEBUG="__DEBUG__";

    private synchronized void configGoogleApiClient()
    {
        if(googleApiClient==null)
        {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void processLocation()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasPermission = checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
            Log.d(DEBUG, "hasPermission "+ hasPermission + " " + PackageManager.PERMISSION_GRANTED);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION
                );
            }
        }
        getlocation();
    }

    private void getlocation()
    {
        Log.d(DEBUG, Build.VERSION.SDK_INT + " " + Build.VERSION_CODES.M);
        Location lastLocation = null;
        try
        {
            Log.d(DEBUG, "start getLastLocation");
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Log.d(DEBUG, "Location" + lastLocation.toString());
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if(lastLocation != null)
        {
            current_longtitude = lastLocation.getLongitude();
            current_latitude = lastLocation.getLatitude();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(DEBUG, "Get location");
        configGoogleApiClient();
        googleApiClient.connect();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Log.d(DEBUG, "show Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(DEBUG, "onMapReady");

        mMap.setOnCameraIdleListener(
        new GoogleMap.OnCameraIdleListener()
        {
            @Override
            public void onCameraIdle()
            {
                VisibleRegion mapVisibleRegion  = mMap.getProjection().getVisibleRegion();
                MapsActivity.minLatitude = mapVisibleRegion.latLngBounds.southwest.latitude;
                MapsActivity.maxLatitude = mapVisibleRegion.latLngBounds.northeast.latitude;
                MapsActivity.minLongtitude = mapVisibleRegion.latLngBounds.southwest.longitude;
                MapsActivity.maxLongtitude = mapVisibleRegion.latLngBounds.northeast.longitude;
                new QueryDataThread(MapsActivity.this).start();

            }
        }
        );
    }

    private void setLocation()
    {
        Log.d(DEBUG, current_latitude+ " " + current_longtitude);
        LatLng place = new LatLng(current_latitude, current_longtitude);
        mMap.addMarker(new MarkerOptions().position(place).title("Current position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 17.0f));
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(DEBUG, "onConnected");
        processLocation();
        setLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(DEBUG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(DEBUG, "onConnectionFailed");
    }
}
