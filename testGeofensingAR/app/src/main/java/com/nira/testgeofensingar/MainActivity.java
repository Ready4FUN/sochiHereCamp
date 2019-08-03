package com.nira.testgeofensingar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapPolygon;
import com.here.android.mpa.mapping.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE };


    private Map map = null;
    private SupportMapFragment mapFragment = null;

    private PositioningManager positioningManager = null;
    private PositioningManager.OnPositionChangedListener positionListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, parkingAR.class);
            startActivity(intent);
        });

        fab.hide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (positioningManager != null) {
            positioningManager.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (positioningManager != null) {
            positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }


    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }


    private void initialize(){


        // Search for the map fragment to finish setup by calling init().
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        // Set up disk cache path for the map service for this application
        // It is recommended to use a path under your application folder for storing the disk cache
        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.nira.testgeofensingar.MapService"); /* ATTENTION! Do not forget to update {YOUR_INTENT_NAME} */

        if (!success) {
            Toast.makeText(getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG);
        } else {
            mapFragment.init(new OnEngineInitListener() {
                @Override
                public void onEngineInitializationCompleted(Error error) {
                    final GeoCoordinate[] currentPosition = {new GeoCoordinate(55.86, 37.48)};
                    final GeoCoordinate[] oldPosition = {new GeoCoordinate(55.86, 37.48)};

                    if (error == Error.NONE) {
                        // retrieve a reference of the map from the map fragment
                        map = mapFragment.getMap();
                        // Set the map center to the Vancouver region (no animation)
                        map.setCenter(new GeoCoordinate(55.861274569586, 37.48462811297, 0.0),
                                Map.Animation.NONE);
                        // Set the zoom level to the average between min and max
                        map.setZoomLevel(18);

                        positioningManager = PositioningManager.getInstance();
                        positionListener = new PositioningManager.OnPositionChangedListener() {
                            @Override
                            public void onPositionUpdated(PositioningManager.LocationMethod method, GeoPosition position, boolean isMapMatched) {
                                //map.setCenter(position.getCoordinate(), Map.Animation.NONE);
                                currentPosition[0] = position.getCoordinate();
                                if(!currentPosition[0].equals(oldPosition[0])) {
                                    makeRequest(new GeoCoordinate(position.getCoordinate().getLatitude(), position.getCoordinate().getLongitude()));
                                    map.setCenter(position.getCoordinate(), Map.Animation.NONE);
                                    oldPosition[0] = currentPosition[0];
                                    //Toast.makeText(getApplicationContext(), "positing update", Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onPositionFixChanged(PositioningManager.LocationMethod method, PositioningManager.LocationStatus status) { }
                        };

                        try {
                            positioningManager.addListener(new WeakReference<>(positionListener));
                            if(!positioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK)) {
                                Log.e("HERE", "PositioningManager.start: Failed to start...");
                            }
                        } catch (Exception e) {
                            Log.e("HERE", "Caught: " + e.getMessage());
                        }
                        mapFragment.getPositionIndicator().setVisible(true);

                        List<GeoCoordinate> parkingShape = new ArrayList<GeoCoordinate>();
                        parkingShape.add(new GeoCoordinate(55.86101566541637, 37.484011204899616, 0.0));
                        parkingShape.add(new GeoCoordinate(55.86057612647918, 37.484558375538654, 0.0));
                        parkingShape.add(new GeoCoordinate(55.86082600196441, 37.48519137686617, 0.0));
                        parkingShape.add(new GeoCoordinate(55.861274569586335, 37.48462811297304, 0.0));

                        List<GeoCoordinate> yardShape = new ArrayList<GeoCoordinate>();
                        yardShape.add(new GeoCoordinate(55.860904275882206, 37.48296514338381, 0.0));
                        yardShape.add(new GeoCoordinate(55.86110297050371, 37.483555229367084, 0.0));
                        yardShape.add(new GeoCoordinate(55.86049183077225, 37.484311612309284, 0.0));
                        yardShape.add(new GeoCoordinate(55.860290122445285, 37.483780534924335, 0.0));

                        List<GeoCoordinate> streetShape = new ArrayList<GeoCoordinate>();
                        streetShape.add(new GeoCoordinate(55.861735173902254, 37.48272374457247, 0.0));
                        streetShape.add(new GeoCoordinate(55.86148530426477, 37.483018787564106, 0.0));
                        streetShape.add(new GeoCoordinate(55.86181344598822, 37.48395756071932, 0.0));
                        streetShape.add(new GeoCoordinate(55.86206632395744, 37.48361960238344, 0.0));

                        GeoPolygon geoParkingPolygon = new GeoPolygon(parkingShape);
                        MapPolygon parkingPolygon = new MapPolygon(geoParkingPolygon);
                        parkingPolygon.setFillColor(Color.argb(70, 0, 255, 0));

                        GeoPolygon geoYardPolygon = new GeoPolygon(yardShape);
                        MapPolygon mapYardPolygon = new MapPolygon(geoYardPolygon);
                        mapYardPolygon.setFillColor(Color.argb(70, 0, 0, 255));

                        GeoPolygon geoStreetPolygon = new GeoPolygon(streetShape);
                        MapPolygon mapStreetPolygon = new MapPolygon(geoStreetPolygon);
                        mapStreetPolygon.setFillColor(Color.argb(70, 255, 0, 0));

                        map.addMapObject(parkingPolygon);
                        map.addMapObject(mapYardPolygon);
                        map.addMapObject(mapStreetPolygon);

                    } else {
                        System.out.println("ERROR: Cannot initialize Map Fragment");
                    }
                }
            });
        }
    }


    public void makeRequest(GeoCoordinate coords) {
        FloatingActionButton fab = findViewById(R.id.fab);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,"https://gfe.api.here.com/2/search/proximity.json?layer_ids=4711&app_id=4XeWgoNVvORJz4lkZQhv&app_code=vdYLl9afBxZoVYtRG7jbaQ&proximity=" + coords.getLatitude() + "," + coords.getLongitude() + "&key_attribute=NAME", null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray geometries = response.getJSONArray("geometries");
                    if(geometries.length() > 0) {
                        if(geometries.getJSONObject(0).getJSONObject("attributes").getString("NAME").equals("PARKING")) {
                            fab.show();
                            Log.d("HERE", "You are in parking");
                        } else {
                            fab.show();
                            Log.d("HERE", "Todo");
                        }
                    } else {
                        fab.hide();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, error -> Log.d("HERE", error.getMessage()));
        queue.add(request);
    }


}
