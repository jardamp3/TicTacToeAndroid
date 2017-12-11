package fi.jamk.tictactoe;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by arne on 10.12.17.
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TicTacToe";
    private final String DATABASE_TABLE = "Scores";
    private final String NAME = "name";
    private final String WINS = "wins";
    private final String LOOSES = "looses";
    private final String TIES = "ties";

    public DatabaseOpenHelper(Context context) {
        // Context, database name, optional cursor factory, database version
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create a new table
        //db.execSQL("DROP TABLE "+DATABASE_TABLE);
        db.execSQL("CREATE TABLE "+DATABASE_TABLE+" (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+NAME+" TEXT, "+WINS+" REAL, "+LOOSES+" REAL,"+TIES+" REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
        onCreate(db);
    }
}
