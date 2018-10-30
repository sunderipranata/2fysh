package id.ac.umn.twofysh;

/**
 * Created by SUNDERI on 9/26/2017.
 */
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;


public class HttpPostReturnJSON{
    static String charset = "UTF-8";
    static HttpURLConnection conn;
    static StringBuilder result;
    static URL urlObj;
    static JSONObject jObj = null;
    static StringBuilder sbParams;
    static String paramsString;

    static public JSONObject makeHttpRequest(String url, HashMap<String, String> params){
        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        // request method is POST
        try {
            urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("POST");
            conn.connect();

            paramsString = sbParams.toString();
            Log.v("PARAMS STRING", paramsString);
            //wr = new DataOutputStream(conn.getOutputStream());
            conn.getOutputStream().write(paramsString.getBytes());
            BufferedReader reader=new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("JSON Parser", "JSON result: " + result.toString());
        }
        catch (ConnectException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            conn.disconnect();
        }


        // try parse the string to a JSON object
        try {
            System.out.println(result.toString());
            jObj = new JSONObject(result.toString());
            return jObj;
        } catch (Exception e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return null;
    }
}
