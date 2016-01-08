package HttpUnit;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/*
 * Created by dun on 2016/1/5.
 */
public class HttpUrlConnectionUnits {


    /**
     * 不带参数或者带上全参数的GET请求
     * */
    public static String doGet(String reqUrl){
        return doGet(reqUrl,null);
    }


    /**
     * 默认请求和接受的enCode为UTF-8
     * */
    public static String doGet(String reqUrl,Map parameters){
        try {
            return doGet(reqUrl,parameters,"utf-8","utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * <pre>
     * 发送带参数的GET的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @param parameters 参数映射表
     * @return HTTP响应的字符串
     */
    public static String doGet(String reqUrl, Map parameters,
                               String recvEncoding,String requestEncoding) throws Exception {
        HttpURLConnection url_con;
        String responseContent;

        if(!isNullOrEmpty(parameters)){
            reqUrl = getUrlWhitParms(reqUrl,parameters,requestEncoding);
        }

        URL url = new URL(reqUrl);
        url_con = (HttpURLConnection) url.openConnection();
        url_con.setRequestMethod("GET");
        url_con.setReadTimeout(10000);//（单位：毫秒）jdk 1.5换成这个,读操作超时
        url_con.setDoInput(true);

        InputStream in = url_con.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(in,
                recvEncoding));
        String tempLine = rd.readLine();
        StringBuffer temp = new StringBuffer();
        String crlf=System.getProperty("line.separator");
        while (tempLine != null)
        {
            temp.append(tempLine);
            temp.append(crlf);
            tempLine = rd.readLine();
        }
        responseContent = temp.toString();
        rd.close();
        in.close();


        url_con.disconnect();

        return responseContent;
    }

    private static boolean isNullOrEmpty(Map c){
        if(c==null){
            return true;
        }else if(c.size()==0){
            return true;
        }else{
            return false;
        }

    }

    public static String getUrlWhitParms(String reqUrl,Map parms,String requestEncoding) throws UnsupportedEncodingException {
        StringBuffer params = new StringBuffer();
        for (Iterator iter = parms.entrySet().iterator(); iter
                .hasNext();)
        {
            Map.Entry element = (Map.Entry) iter.next();
            params.append(element.getKey().toString());
            params.append("=");
            params.append(URLEncoder.encode(element.getValue().toString(),requestEncoding));
            params.append("&");
        }

        if (params.length() > 0)
        {
            params = params.deleteCharAt(params.length() - 1);
        }
        reqUrl+="?"+params;

        return reqUrl;
    }


    /**
     * <pre>
     * 发送带参数的POST的HTTP请求
     * </pre>
     *
     * @param reqUrl HTTP请求URL
     * @param parameters 参数映射表
     * @return HTTP响应的字符串
     */
    public static String doPost(String reqUrl,@NonNull Map parameters,
                                String recvEncoding,String requestEncoding) throws Exception {
        HttpURLConnection url_con = null;
        String responseContent = null;

        StringBuffer params = new StringBuffer();
        for (Iterator iter = parameters.entrySet().iterator(); iter
                .hasNext();)
        {
            Map.Entry element = (Map.Entry) iter.next();
            params.append(element.getKey().toString());
            params.append("=");
            params.append(URLEncoder.encode(element.getValue().toString(),
                    requestEncoding));
            params.append("&");
        }

        if (params.length() > 0)
        {
            params = params.deleteCharAt(params.length() - 1);
        }

        URL url = new URL(reqUrl);
        url_con = (HttpURLConnection) url.openConnection();
        url_con.setRequestMethod("POST");
         url_con.setReadTimeout(10000);//（单位：毫秒）jdk 1.5换成这个,读操作超时
        url_con.setDoOutput(true);
        byte[] b = params.toString().getBytes();
        url_con.getOutputStream().write(b, 0, b.length);
        url_con.getOutputStream().flush();
        url_con.getOutputStream().close();

        InputStream in = url_con.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(in,
                recvEncoding));
        String tempLine = rd.readLine();
        StringBuffer tempStr = new StringBuffer();
        String crlf=System.getProperty("line.separator");
        while (tempLine != null)
        {
            tempStr.append(tempLine);
            tempStr.append(crlf);
            tempLine = rd.readLine();
        }
        responseContent = tempStr.toString();
        rd.close();
        in.close();


        if (url_con != null)
        {
            url_con.disconnect();
        }

        return responseContent;
    }





}
