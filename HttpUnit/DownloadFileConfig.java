package HttpUnit;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by dun on 2016/1/5.
 */
public class DownloadFileConfig {


    private int _id = -1;
    private long finished = 0;
    private long start = 0;
    private long size = 0;
    private String path = "";
    private String url = "";
    private String fileName = "";
    private Context mContext;

    //是否断点下载
    private boolean isBreakBrokenDownload = false;


    /**
     * @param url 下载地址
     * @param mContext
     * */
    public DownloadFileConfig(String url,Context mContext) {
        this.mContext = mContext.getApplicationContext();
        this.url = url;
        this.path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+mContext.getResources().
                getString(mContext.getApplicationInfo().labelRes)+"/download";

        this.fileName = getFileName(url);
    }


    public boolean isBreakBrokenDownload() {
        return isBreakBrokenDownload;
    }

    public void setBreakBrokenDownload(boolean breakBrokenDownload) {
        isBreakBrokenDownload = breakBrokenDownload;
    }

    public DownloadFileConfig(long finished, long start, long size, String path, String url, String fileName) {
        this.finished = finished;
        this.start = start;
        this.size = size;
        this.path = path;
        this.url = url;
        this.fileName = fileName;

    }


    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    private String getFileName(String url){
        String[] arr = url.split("/");
        return arr[arr.length-1];
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.fileName = getFileName(url);
    }
}
