package fi.jamk.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseServiceActivity
{
    TextView txtPairedWith;
    Cursor cursor;
    String pairedDeviceName;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtPairedWith = findViewById(R.id.txtPairedWith);
    }


    // MAIN MENU BUTTONS CLICK
    public void onConnectClick(View v){
        if(!btService.isBtEnabled()) // start bluetooth
            return;

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                btService.getListDevices());

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select device:")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pairedDeviceName = adapter.getItem(which);
                        btService.setRemoteDevice(pairedDeviceName);
                        txtPairedWith.setText("Paired with: " + pairedDeviceName);
                    }
                })
                .create();

        builder.show();
    }

    public void onStartGameClick(View v){
        //db.execSQL("delete from SCORES");

        cursor = db.rawQuery("SELECT name FROM SCORES WHERE name = '" + pairedDeviceName + "';", null);

        if(cursor.getCount() == 0){
            String comm = "INSERT INTO SCORES (name, wins, looses, ties) VALUES('"+ pairedDeviceName+"', 0, 0, 0);";
            db.execSQL(comm);
            Log.d("DB:", "new device name inserted");
        }


        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("opponentName", pairedDeviceName);
        startActivity(i);


    }

    public void onHighScoresClick(View v){
        Intent i = new Intent(this, ScoresActivity.class);
        startActivity(i);
    }


}
