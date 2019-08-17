package here.com;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPolygon;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.SupportMapFragment;

import java.io.File;
import java.lang.ref.WeakReference;


public class MapFragmentView extends AppCompatActivity {

    public AppCompatActivity m_activity;

    public SupportMapFragment m_mapFragment;
    private Map m_map;

    private FloatingActionButton geolocationBtn;
    private FloatingActionButton zoomInBtn;
    private FloatingActionButton zoomOutBtn;
    private FloatingActionButton taskInfoBtn;

    private PositioningManager posManager = null;
    private Boolean paused = false;
    private Boolean initialized = false;

    TaskManager taskManager;

    SQLiteDatabase db;
    DBHelper dbHelper;

    private PositioningManager.OnPositionChangedListener positionListener = new PositioningManager.OnPositionChangedListener() {


        public void onPositionUpdated(PositioningManager.LocationMethod method,
                                      GeoPosition position, boolean isMapMatched) {

            if (!paused) {

            }

            if (!initialized) {

                initialized = true;

                m_map.setCenter(posManager.getPosition().getCoordinate(),
                        Map.Animation.BOW);
            }

            try{
                GeoPolygon checkPolygon = taskManager.getCurrentGeozone();
                GeoCoordinate currentPosition = new GeoCoordinate(position.getCoordinate());

                if(checkPolygon.contains(currentPosition)){
                    taskManager.completeCurrentTask();
                    taskManager.openCurrentTaskDescription();
                }

            }catch(NullPointerException err){
                System.out.print(err);
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

        initDatabase();
        initMapFragment ();
    }

    private SupportMapFragment getMapFragment () {
        return (SupportMapFragment) m_activity.getSupportFragmentManager().findFragmentById(R.id.mapfragment);
    }

    private void initDatabase () {
        dbHelper = new DBHelper(m_activity);
        db = dbHelper.getReadableDatabase();
    }

    private void initMapFragment () {
        m_mapFragment = getMapFragment();

        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                m_activity.getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps",
                "MapService");


        if(!success) {
            Toast.makeText(m_activity.getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG);
        } else m_mapFragment.init(error -> {
            if (error == OnEngineInitListener.Error.NONE) {
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

                taskManager = new TaskManager(m_activity, m_map);

                openDialogWindow();
                initControlBtns();

            } else {
                System.out.println("ERROR: Cannot initialize Map Fragment");
            }
        });

    }

    private void initControlBtns() {
        geolocationBtn = m_activity.findViewById(R.id.geolocationBtn);
        zoomInBtn = m_activity.findViewById(R.id.zoomInBtn);
        zoomOutBtn = m_activity.findViewById(R.id.zoomOutBtn);
        taskInfoBtn = m_activity.findViewById(R.id.task_info);


        taskInfoBtn.setOnClickListener(v -> {
            taskManager.openCurrentTaskDescription();
        });

        geolocationBtn.setOnClickListener(v -> {
            try{

                if(posManager.hasValidPosition()){
                    m_map.setCenter(posManager.getPosition().getCoordinate(),
                            Map.Animation.LINEAR);
                    m_map.setZoomLevel(17, Map.Animation.BOW);
                    m_map.setTilt(0, Map.Animation.BOW);
                    m_map.setOrientation(0, Map.Animation.BOW);
                };
            }catch(NullPointerException error) {
                Toast.makeText(getApplicationContext(), "Идет поиск местоположения", Toast.LENGTH_SHORT).show();
                System.out.print("Search in process");
            }
        });

        zoomInBtn.setOnClickListener(v ->{
            double zoomLevel = m_map.getZoomLevel();
            m_map.setCenter(posManager.getPosition().getCoordinate(),
                    Map.Animation.BOW);
            m_map.setZoomLevel( zoomLevel + 1, Map.Animation.LINEAR);
        });

        zoomOutBtn.setOnClickListener(v -> {
            double zoomLevel = m_map.getZoomLevel();
            m_map.setCenter(posManager.getPosition().getCoordinate(),
                    Map.Animation.BOW);
            m_map.setZoomLevel( zoomLevel - 1, Map.Animation.LINEAR);
        });

    }

    private void openDialogWindow() {
        Cursor cursor;

        cursor = taskManager.getActiveUser();

        if(!cursor.moveToFirst()){
            LayoutInflater inflater = m_activity.getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_start_task, null);

            new MaterialAlertDialogBuilder(m_activity)
                    .setCancelable(false)
                    .setView(v)
                    .setPositiveButton("Начать", (dialogInterface, i) -> {
                    })
                    .show();
        }
    }

}
