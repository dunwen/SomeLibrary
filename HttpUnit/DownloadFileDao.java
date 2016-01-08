package HttpUnit;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by dun on 2016/1/6.
 */
public class DownloadFileDao {
    private Context mContext;
    DownloadDBHelper helper;
    SQLiteDatabase database;
    private final String tableName = "downloadFile";

    public DownloadFileDao(Context mContext){
        this.mContext = mContext;
        helper = new DownloadDBHelper(mContext);
        database = helper.getWritableDatabase();
    }

    public void insertDownloadFileConfig(DownloadFileConfig file){
        long start = file.getStart();
        long finished = file.getFinished();
        long size = file.getSize();
        String fileName = file.getFileName();
        String path = file.getPath();
        String url = file.getUrl();
        int state = (size==finished?1:0);

        String sqlString = "INSERT INTO "+tableName+" values(\n" +
                "null,\n" +
                ""+finished+",\n" +
                ""+start+",\n" +
                ""+size+",\n" +
                "'"+path+"',\n" +
                "'"+url+"',\n" +
                "'"+fileName+"',\n" +
                ""+state+"\n" +
                ");";
        database.execSQL(sqlString);
    }
    public void deleteDownloadFileConfig(DownloadFileConfig file){
        int _id = file.get_id();
        String sqlString;
        if(_id==-1){
            sqlString = "DELETE FROM "+tableName+" where\n" +
                    "url = '"+file.getUrl()+"'";
        }else{
            sqlString = "DELETE FROM "+tableName+" where\n" +
                    "_id = "+_id+"";
        }

        database.execSQL(sqlString);
    }

    public void updateDownloadFileConfig(DownloadFileConfig file){
        int _id = file.get_id();
        String sqlString;

        long start = file.getStart();
        long finished = file.getFinished();
        long size = file.getSize();
        String fileName = file.getFileName();
        String path = file.getPath();
        String url = file.getUrl();
        int state = (size==finished?1:0);

        if(_id == -1){
            sqlString = "UPDATE "+tableName+" SET\n" +
                    "finished= "+finished+" ,\n" +
                    "state = "+state+" ,\n" +
                    "size = "+size+",\n" +
                    "fileName = '"+fileName+"',\n" +
                    "start = "+start+",\n" +
                    "path = '"+path+"'\n" +
                    "WHERE _id = "+_id+"";
        }else{
            sqlString = "UPDATE "+tableName+" SET\n" +
                    "finished= "+finished+" ,\n" +
                    "state = "+state+" ,\n" +
                    "size = "+size+",\n" +
                    "fileName = '"+fileName+"',\n" +
                    "start = "+start+",\n" +
                    "path = '"+path+"'\n" +
                    "WHERE url = '"+url+"'";
        }

        database.execSQL(sqlString);
    }


    /**
     * @return null if not exist
     * */
    public DownloadFileConfig queryDownloadFile(String url){
        String sqlString = "SELECT * FROM "+tableName+"\n" +
                "WHERE url = '"+url+"'";
        Cursor c = database.rawQuery(sqlString,null);

        DownloadFileConfig file = null;

        while (c.moveToNext()){
            int _id = c.getInt(c.getColumnIndex("_id"));
            long start = c.getLong(c.getColumnIndex("start"));
            long finished = c.getLong(c.getColumnIndex("finished"));
            long size = c.getLong(c.getColumnIndex("size"));
            String path = c.getString(c.getColumnIndex("path"));
            String fileName = c.getString(c.getColumnIndex("fileName"));

            file = new DownloadFileConfig(finished,start,size,path,url,fileName);
            file.set_id(_id);
        }
        c.close();
        return file;
    }

    public void close(){
        database.close();
        helper.close();
    }

}
