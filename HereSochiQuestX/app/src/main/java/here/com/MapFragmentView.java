package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.SupportMapFragment;

import java.io.File;
import java.lang.ref.WeakReference;

public class MapFragmentView extends AppCompatActivity {

    public static SupportMapFragment m_mapFragment;
    private Map m_map;

    AppCompatActivity m_activity;

    private PositioningManager posManager = null;

    private Boolean paused = false;
    private Boolean initialized = false;

    private PositioningManager.OnPositionChangedListener positionListener = new PositioningManager.OnPositionChangedListener() {


        public void onPositionUpdated(PositioningManager.LocationMethod method,
                                      GeoPosition position, boolean isMapMatched) {

            if (!paused) {

            }

            if (!initialized) {

                initialized = true;

                m_map.setCenter(posManager.getPosition().getCoordinate(),
                        Map.Animation.BOW);
                m_map.setZoomLevel(17);
            }
        }

        public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                         PositioningManager.LocationStatus status) {
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if (posManager != null) {
            posManager.start(
                    PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }

    @Override
    public void onPause() {
        if (posManager != null) {
            posManager.stop();
        }
        super.onPause();
        paused = true;
    }

    @Override
    public void onDestroy() {
        if (posManager != null) {
            posManager.removeListener(
                    positionListener);
        }
        m_map = null;
        super.onDestroy();
    }


    public MapFragmentView(AppCompatActivity activity) {
        m_activity = activity;
        initMapFragment ();
    }

    private SupportMapFragment getMapFragment () {
        return (SupportMapFragment) m_activity.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    private void initMapFragment () {
        m_mapFragment = getMapFragment();

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                m_activity.getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "MapService");


        if(!success) {
            Toast.makeText(m_activity.getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG);
        } else m_mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (error == Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    m_map = m_mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    m_map.setCenter(new GeoCoordinate(55.750907459297785, 37.61693000793457, 0.0),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max
                    m_map.setZoomLevel((m_map.getMaxZoomLevel() + m_map.getMinZoomLevel()) / 2);

                    posManager = PositioningManager.getInstance();
                    posManager.addListener(new WeakReference<>(positionListener));
                    posManager.start(
                            PositioningManager.LocationMethod.GPS_NETWORK);
                    m_map.getPositionIndicator().setVisible(true);

                    m_mapFragment.getMapGesture().setAllGesturesEnabled(false);

                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });

    }
}
