package here.com;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


public class AuthActivity extends AppCompatActivity {

    Button btnStart;
    TextView passInput;
    DBHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        checkActiveUser();
        initBtns();
    }

    private void initBtns () {

        btnStart = findViewById(R.id.btn_start);
        passInput = findViewById(R.id.team_code);

        btnStart.setOnClickListener(v -> {
            String query;
            Cursor cursor;

            String team_code = passInput.getText().toString();

            query = "SELECT * FROM " + DBHelper.TABLE_TEAMS + " WHERE team_code = ?";
            cursor = db.rawQuery(query, new String[] {team_code});

            if (cursor.moveToFirst()) {
                ContentValues cv = new ContentValues();
                cv.put(DBHelper.ACTIVE, 1);
                String where = DBHelper.TEAM_CODE + " = ?";
                String[] whereArgs = new String[] {team_code};
                db.update(DBHelper.TABLE_TEAMS, cv, where,whereArgs);

                openMainActivity ();
            } else {
                passInput.setText("");
            }
        });
    }

    private void checkActiveUser(){
        String query;
        Cursor cursorActive;

        // Select from TEAMS table active users
        query = "SELECT * FROM " + DBHelper.TABLE_TEAMS + " WHERE active = ?";
        cursorActive = db.rawQuery(query, new String[] {"1"});

        // Open MainActivity if user is active
        if(cursorActive.moveToFirst()){
            openMainActivity ();
        }
    }
    private void openMainActivity () {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}