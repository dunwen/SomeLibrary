package HttpUnit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dun on 2016/1/6.
 */
public class DownloadDBHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "DownloadUnits.db";
    private static final int VERSION = 1;


    public DownloadDBHelper(Context context){
        this(context,DB_NAME,null,VERSION);
    }

    public DownloadDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlString = "CREATE TABLE downloadFile(\n" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "finished LONG,\n" +
                "start LONG,\n" +
                "size LONG,\n" +
                "path STRING,\n" +
                "url STRING,\n" +
                "fileName STRING,\n" +
                "state INTEGER\n" +
                ");";
        db.execSQL(sqlString);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
