package HttpUnit;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dun on 2016/1/5.
 */
public class DownLoadUnits {
    public static final int STATE_NEW = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_CANCEL = 2;
    public static final int STATE_ERROE = 3;
    public static final int STATE_PREPARE = 4;
    public static final int STATE_DOWNLOADING = 5;
    public static final int STATE_FINISHI = 6;

    private int downloadState = 0;
    private long progressUpDateTime = 1000L;
    private progressListener mprogressListener;
    private DownloadFileConfig mDownloadFile;
    private Thread mThread;
    private  receiveHandlder mHander = new receiveHandlder(Looper.getMainLooper());
    private boolean isBrokenDownload = false;
    private Context mContext;
    private DownloadFileDao dao;


    public DownLoadUnits(String url,Context mContext,boolean isBrokenDownload) {
        this.isBrokenDownload = isBrokenDownload;
        this.mContext = mContext.getApplicationContext();
        if(!isBrokenDownload){
            this.mDownloadFile = new DownloadFileConfig(url,mContext);
        }else{
            dao = new DownloadFileDao(mContext);
            if((mDownloadFile = dao.queryDownloadFile(url))==null){
                mDownloadFile = new DownloadFileConfig(url,mContext);
            }
        }
        downloadState = STATE_NEW;
    }

    public void startDownlowd(){
        if(downloadState>=4){
            return;
        }

        mThread = new Thread(new downLoadRunnable());
        mThread.start();
    }


    public int getDownloadState() {
        return downloadState;
    }


    /**
     * @return true 如果暂停成功
     * */
    public boolean downloadPause(){
        if(downloadState==STATE_PREPARE||downloadState==STATE_DOWNLOADING){
            this.downloadState = STATE_PAUSE;
            return true;
        }
        return false;
    }
    /**
     * @return true 如果取消成功
     * */
    public boolean downloadCancel(){
        if(downloadState==STATE_PREPARE||downloadState==STATE_DOWNLOADING){
           this.downloadState = STATE_CANCEL;
            removeFile();
            return true;
        }
        return false;
    }

    public void removeFile() {
        mDownloadFile.setFinished(0);
        File file = new File(mDownloadFile.getPath(),mDownloadFile.getFileName());
        if(file.exists()){
            file.delete();
        }
    }

//    private void setDownloadState(int downloadState) {
//        this.downloadState = downloadState;
//        if(downloadState == STATE_CANCEL){
//            mDownloadFile.setFinished(0);
//            File file = new File(mDownloadFile.getPath(),mDownloadFile.getFileName());
//            if(file.exists()){
//                file.delete();
//            }
//        }
//    }


    public boolean isBrokenDownload() {
        return isBrokenDownload;
    }
    public long getProgressUpDateTime() {
        return progressUpDateTime;
    }

    public void setProgressUpDateTime(long progressUpDateTime) {
        this.progressUpDateTime = progressUpDateTime;
    }

    public progressListener getMprogressListener() {
        return mprogressListener;
    }

    public void setMprogressListener(progressListener mprogressListener) {
        this.mprogressListener = mprogressListener;
    }

    public DownloadFileConfig getmDownloadFile() {
        return mDownloadFile;
    }

    public void setmDownloadFile(DownloadFileConfig mDownloadFile) {
        this.mDownloadFile = mDownloadFile;
    }




    private class downLoadRunnable implements Runnable{


        @Override
        public void run() {
            try {
                downloadState = DownLoadUnits.STATE_PREPARE;

                URL mUrl = new URL(mDownloadFile.getUrl());
                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setConnectTimeout(10000);
                long size = conn.getContentLength();

                if(!(size<0)){
                    if(!isBrokenDownload){
                        mDownloadFile.setSize(size);
                    }else if(mDownloadFile.getSize()!=size){
                        mDownloadFile.setSize(size);
                        mDownloadFile.setFinished(0);
                    }
                }else{
                    mHander.sendEmptyMessage(STATE_ERROE);
                    return;
                }

                conn.disconnect();
                conn = (HttpURLConnection) mUrl.openConnection();

                conn.setRequestProperty("Range","bytes="+mDownloadFile.getFinished()+"-"+mDownloadFile.getSize());
                conn.setConnectTimeout(10000);

                long refreshTime = System.currentTimeMillis();

                File file = new File(mDownloadFile.getPath(),mDownloadFile.getFileName());
                File dir = new File(mDownloadFile.getPath());
                if(!dir.exists()){
                    dir.mkdirs();
                }

                if(!file.exists()){
                    mDownloadFile.setFinished(0);
                    file.createNewFile();
                }else if(mDownloadFile.getFinished()==0){
                    file.delete();
                }else if(mDownloadFile.getFinished()==mDownloadFile.getSize()){
                    mHander.sendEmptyMessage(STATE_FINISHI);
                    downloadState = STATE_FINISHI;
                    return;
                }

                RandomAccessFile raf = new RandomAccessFile(file,"rwd");

//                if(conn.getResponseCode() == 200){


                    long currentSize = mDownloadFile.getFinished();
                    InputStream mInputStream = conn.getInputStream();
                    byte[] bytes = new byte[4 * 1024];
                    int len = -1;

                    raf.seek(mDownloadFile.getFinished());
                    downloadState = STATE_DOWNLOADING;
                    while ((len = mInputStream.read(bytes))!=-1){
                        raf.write(bytes,0,len);
                        long finished = mDownloadFile.getFinished();
                        mDownloadFile.setFinished(finished+len);

                        long currentTime = System.currentTimeMillis();


                        if(currentTime - refreshTime >= progressUpDateTime){
                            Message message = mHander.obtainMessage(DownLoadUnits.STATE_DOWNLOADING);
                            int speed = (int)((mDownloadFile.getFinished() - currentSize)/ (1024 * progressUpDateTime/1000));
                            currentSize = mDownloadFile.getFinished();
                            message.arg1 = speed;
                            message.sendToTarget();
                            refreshTime = currentTime;
                        }

                        if(downloadState == DownLoadUnits.STATE_PAUSE){
                            mInputStream.close();
                            conn.disconnect();

                            if(isBrokenDownload){
                                if(dao.queryDownloadFile(mDownloadFile.getUrl())==null){
                                    dao.insertDownloadFileConfig(mDownloadFile);
                                }else{
                                    dao.updateDownloadFileConfig(mDownloadFile);
                                }
                            }

                            mHander.sendEmptyMessage(STATE_PAUSE);
                            return;
                        }else if(downloadState == STATE_CANCEL){
                            downloadState = STATE_CANCEL;
                            downloadCancel();
                            if(isBrokenDownload){
                                if(dao.queryDownloadFile(mDownloadFile.getUrl())!=null){
                                    dao.deleteDownloadFileConfig(mDownloadFile);
                                }
                            }

                            return;
                        }
                    }
                    mInputStream.close();
                    conn.disconnect();

                    if(isBrokenDownload){
                        if(dao.queryDownloadFile(mDownloadFile.getUrl())!=null){
                            dao.updateDownloadFileConfig(mDownloadFile);
                        }else{
                            dao.insertDownloadFileConfig(mDownloadFile);
                        }
                    }

                    downloadState = DownLoadUnits.STATE_FINISHI;
                    mHander.sendEmptyMessage(STATE_FINISHI);
//                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



    public interface progressListener{

        /**
         * @param progress 进度，最高100
         * @param speed 速度，单位kb/s
         * */
        void onDownLoading(float progress,int speed);
        void onFinish();
        void onError();

        /**
         * @param progress 暂停时的进度
         * */
        void onPause(float progress);
    }


    private class receiveHandlder extends Handler{

        public receiveHandlder(Looper l){
            super(l);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            if(mprogressListener == null){
                return;
            }
            if(msg.what == DownLoadUnits.STATE_DOWNLOADING){
                mprogressListener.onDownLoading(mDownloadFile.getFinished()*100/mDownloadFile.getSize(),msg.arg1);
            }else if(msg.what == DownLoadUnits.STATE_FINISHI){
                mprogressListener.onFinish();
            }else if(msg.what==DownLoadUnits.STATE_ERROE){
                mprogressListener.onError();
            }else if(msg.what==DownLoadUnits.STATE_PAUSE){
                mprogressListener.onPause(mDownloadFile.getFinished()*100/mDownloadFile.getSize());
            }


        }
    }

}
