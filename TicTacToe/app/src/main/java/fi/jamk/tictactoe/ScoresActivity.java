package fi.jamk.tictactoe;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Created by arne on 10.12.17.
 */

public class ScoresActivity extends BaseServiceActivity {

    private ListView scores;
    private Cursor cursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        //find List View
        scores = (ListView) findViewById(R.id.scores);
        registerForContextMenu(scores);

        queryData();
    }

    public void queryData() {

        // get data with query
        String[] resultColumns = new String[]{"_id", "name", "wins", "looses","ties"};
        cursor = db.query("Scores", resultColumns, null, null, null, null, "_id DESC", null);

        // add data to adapter
        ListAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_item, cursor,
                new String[]{"name", "wins", "looses", "ties"},      // from
                new int[]{R.id.name, R.id.wins, R.id.looses, R.id.ties}    // to
                , 0);  // flags

        // show data in listView
        scores.setAdapter(adapter);
    }
}
