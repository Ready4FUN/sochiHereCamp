package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

public class unsuportedArCore extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private ModelRenderable andyRenderable;

    private int DefRenderable;

    TaskManager taskManager;

    SceneView mSceneView;
    Scene mScene;


    private String lastTaskIndex;
    private boolean taskDone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsuported_ar_core);
        mSceneView = findViewById(R.id.sceneView);


        //по хорошему определить отдельную функцию и вызвать из разных мест...
        //но я просто скопирую тот же самый код. ИБО БЛЯТЬ МОГУ
        lastTaskIndex = (String)getIntent().getSerializableExtra("CURRENT_ZONE_NUMBER");
        taskDone = (boolean)getIntent().getSerializableExtra("TASK_DONE");
        //Log.e("intent",  lastTaskIndex);

        TextView arText = (TextView)findViewById(R.id.morphArText);

        Button arButton = (Button)findViewById(R.id.morphArButton);

        taskManager = new TaskManager(this, null);

        //оно не работатет -_-
        //HashMap<String, String> questData = taskManager.getCurrentTaskDesctiption();
        //questData.get("language")

        if(taskDone){


            //TODO вставить получение задания
            arText.setText("ЗАДАНИЕ МНЕ ЗАПИЛИ");
            arButton.setText(getResources().getString(R.string.backToMap));
        } else {
            arText.setText(getResources().getString(R.string.done));
            arButton.setText(getResources().getString(R.string.stringSelfieButton));
        }

        Toast toast = Toast.makeText(getApplicationContext(),
                lastTaskIndex, Toast.LENGTH_SHORT);
        toast.show();

        switch (lastTaskIndex){
            case ("1"):
                DefRenderable =  R.raw.pickle_rick;
                break;
            case ("2"):
                DefRenderable =  R.raw.model;
                break;
            case ("3"):
                DefRenderable =  R.raw.butthole;
                break;
            case ("4"):
                DefRenderable =  R.raw.butterrobot;
                break;
            case ("5"):
                DefRenderable =  R.raw.pickle_rick;
                break;
            default:
                DefRenderable = R.raw.pickle_rick;
                break;
        }

        mScene = new Scene(mSceneView);
        ModelRenderable.builder()
                .setSource(this, DefRenderable)
                .build()
                .thenAccept(renderable->onRenderableLoaded(renderable))
                .exceptionally(throwable -> {
                    Log.i("Sceneform", "failed to load model");
                    return null;
                });
    }


    private void onRenderableLoaded(Renderable renderable) {
        Node parentNode = new Node();
        parentNode.setRenderable(renderable);
        parentNode.setParent(mSceneView.getScene());
        parentNode.setLocalPosition(new Vector3(0f, -0.5f, -1f));
        parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), 180));

        //Я вот смотрю на этот код
        // И понимаю, что его писал как минимум сверхразум
        //ДЕЙСТВИТЕЛЬ НО БЛЯТЬ, НАХУЯ НАМ ПЕРЕОПРЕДЕЛЯТЬ ПЕРЕМЕННУЮ И ОДИН РАЗ ВЫЗВАТЬ ФУНКЦИЮ
        //Мы лучше дохуя раз вызовем функции.
        //Переписывать это я конечно же не буду
        switch (lastTaskIndex){
            case ("1"):
                parentNode.setLocalPosition(new Vector3(0f, -0.5f, -1f));
                parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), 180));
                break;
            case ("2"):
                parentNode.setLocalPosition(new Vector3(0f, -0.7f, -1.5f));
                parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), 35));
                break;
            case ("3"):
                parentNode.setLocalPosition(new Vector3(0f, -0.5f, -1f));
                parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), 140));
                break;
            case ("4"):
                parentNode.setLocalPosition(new Vector3(0f, -0.4f, -1f));
                parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), -90));
                break;
            case ("5"):
                parentNode.setLocalPosition(new Vector3(0f, -0.5f, -1f));
                parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), 180));
                break;
            default:
                parentNode.setLocalPosition(new Vector3(0f, -0.5f, -1f));
                parentNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0f), 180));
                break;
        }


        mSceneView.getScene().addChild(parentNode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mSceneView.resume();
        }
        catch (CameraNotAvailableException e){}
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSceneView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSceneView.destroy();
    }


    //как ты поняд, это просто ctrl+c ctrl+v
    public void goSelfie(View target) {
        //Да, мы таскаем lastTaskIndex по активностям... что бы не было ошибок ;3
        //Ахуенно, не правда ли?

        //6:20 на часах
        //Зацени какой костль придумал. Сам в ахуе
        //Я щас такой думал запилить переключение через switch. Булевой переменной.
        //КАРЛ, БУЛЕВАЯ ПЕРЕМЕННАЯ В ОПЕРАТОРЕ SWITCH
        //А когда он начал ругаться на это, думал bool в int перевести
        if(taskDone){
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        } else {
            Intent intent = new Intent(this, cameraActivity.class);
            intent.putExtra("CURRENT_ZONE_NUMBER", lastTaskIndex);
            intent.putExtra("TRUE_AR", false);
            this.startActivity(intent);
        }

    }

}
