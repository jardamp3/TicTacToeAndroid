package fi.jamk.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MainActivity extends BaseServiceActivity
{
    TextView txtPairedWith;
    SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtPairedWith = findViewById(R.id.txtPairedWith);

        db = (new DatabaseOpenHelper(this)).getWritableDatabase();
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
                        String name = adapter.getItem(which);
                        btService.setRemoteDevice(name);
                        txtPairedWith.setText("Paired with: " + name);
                    }
                })
                .create();

        builder.show();
    }

    public void onStartGameClick(View v){
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }

    public void onHighScoresClick(View v){
        Intent i = new Intent(this, Scores.class);
        startActivity(i);
    }


}
