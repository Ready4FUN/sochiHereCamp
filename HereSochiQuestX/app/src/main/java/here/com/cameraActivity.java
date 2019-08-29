package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class cameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // do something on back.
            //возможно понадобиться потом. Но это не точно.
            //тебе наверно интересно почему половина комментов на русском, а половина на английском?
            //ИБО МНЕ БЛЯТЬ ЛЕНЬ ПЕРЕВОДИТЬ ТЕКСТ В 6 УТРА БЛЯТЬ
            Toast toast = Toast.makeText(getApplicationContext(),
                    "BACK KEY", Toast.LENGTH_SHORT);
            //toast.show();
            return true;
        }

        return onKeyDown(keyCode, event);
    }
}
