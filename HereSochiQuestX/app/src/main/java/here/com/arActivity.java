package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.ar.sceneform.ux.ArFragment;

public class arActivity extends AppCompatActivity {

  private ArFragment fragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ar);

    fragment = (ArFragment)
            getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
  }
}