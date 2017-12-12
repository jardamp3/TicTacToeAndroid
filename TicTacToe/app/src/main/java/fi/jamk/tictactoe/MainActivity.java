package fi.jamk.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends BaseServiceActivity
{
    TextView txtPairedWith;
    Cursor cursor;
    String pairedDeviceName;
    Button btnStartGame;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //disabeling the StartGame Button
        btnStartGame = findViewById(R.id.btnStartGame);
        btnStartGame.setClickable(false);

        txtPairedWith = findViewById(R.id.txtPairedWith);
    }


    // MAIN MENU BUTTONS CLICK
    public void onConnectClick(View v){
        if(!btService.isBtEnabled()) // start bluetooth
            return;

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                btService.getListDevices());
        //connect the devices via Bluetooth
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select device:")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pairedDeviceName = adapter.getItem(which);
                        btService.setRemoteDevice(pairedDeviceName);
                        //Show who you connect with
                        txtPairedWith.setText("Paired with: " + pairedDeviceName);
                        //Enable StartGame Button and make it green
                        btnStartGame.getBackground().setColorFilter(Color.parseColor("#88f92a"), PorterDuff.Mode.MULTIPLY);
                        btnStartGame.setClickable(true);
                    }
                })
                .create();

        builder.show();

    }

    public void onStartGameClick(View v){
        //db.execSQL("delete from SCORES");
        //first add the player to the database if it is not in there yet
        cursor = db.rawQuery("SELECT name FROM SCORES WHERE name = '" + pairedDeviceName + "';", null);

        if(cursor.getCount() == 0){
            String comm = "INSERT INTO SCORES (name, wins, looses, ties) VALUES('"+ pairedDeviceName+"', 0, 0, 0);";
            db.execSQL(comm);
            Log.d("DB:", "new device name inserted");
        }

        //start the game activity
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("opponentName", pairedDeviceName);
        startActivity(i);


    }

    public void onHighScoresClick(View v){
        //start the Scores activity
        Intent i = new Intent(this, ScoresActivity.class);
        startActivity(i);
    }


}
