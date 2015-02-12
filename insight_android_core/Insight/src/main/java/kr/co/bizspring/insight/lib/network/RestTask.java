package kr.co.bizspring.insight.lib.network;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

import kr.co.bizspring.insight.lib.util.BSDebugger;

/**
 * Created by caspar on 14. 1. 14.
 */
public class RestTask extends AsyncTask<Void, Void, Void> {

    public enum RestType {
        GET,POST,PUT,DELETE
    };
    String target_URI;
    CallBackInterface callback;
    RestType restType;
    HashMap<String, String> keyValue;
    String requestBody;
    SharedPreferences pref;
    Boolean authFlag;

    int statusCode = 0;
    String statusString;

    Activity activity;

    byte[] compressedJsonData;


    public RestTask(String _target_URI, Object body, RestType _restType, CallBackInterface _callback) {
        this.target_URI = _target_URI;
        if(body instanceof HashMap){
            this.keyValue = (HashMap)body;
            this.requestBody = null;
        }else if(body instanceof byte[]){
            this.compressedJsonData = (byte[]) body;
        }else{
            this.requestBody = (String)body;
            this.keyValue = null;
        }
        this.callback = _callback;
        this.restType = _restType;
        this.authFlag = false;
        this.pref = null;
        this.activity = null;
        if(authFlag==true){
            if(pref!=null){
                this.id = pref.getString("AQ_REST_ID","_");
                if(this.id.equalsIgnoreCase("_")){
                    this.authFlag = false;
                }
                this.passwd = pref.getString("AQ_REST_PASSWD","_");
                if(this.passwd.equalsIgnoreCase("_")){
                    this.authFlag = false;
                }
            }else{
                this.pref=null;
                this.authFlag = false;
            }
        }
    }
    public RestTask(Activity _activity, String _target_URI, Object body, RestType _restType, Boolean _authFlag, SharedPreferences _pref, CallBackInterface _callback) {
        this.target_URI = _target_URI;
        if(body instanceof HashMap){
            this.keyValue = (HashMap)body;
            this.requestBody = null;
        }else{
            this.requestBody = (String)body;
            this.keyValue = null;
        }
        this.callback = _callback;
        this.restType = _restType;
        this.authFlag = _authFlag;
        this.pref = _pref;
        this.activity = _activity;
        if(authFlag==true){
            if(pref!=null){
                this.id = pref.getString("AQ_REST_ID","_");
                if(this.id.equalsIgnoreCase("_")){
                    this.authFlag = false;
                }
                this.passwd = pref.getString("AQ_REST_PASSWD","_");
                if(this.passwd.equalsIgnoreCase("_")){
                    this.authFlag = false;
                }
            }else{
                this.pref=null;
                this.authFlag = false;
            }
        }
    }
    String id;
    String passwd;
    public RestTask(Activity _activity, String _target_URI, Object body, RestType _restType, Boolean _authFlag, SharedPreferences _pref, String _id, String _passwd, CallBackInterface _callback) {
        this.target_URI = _target_URI;
        if(body instanceof HashMap){
            this.keyValue = (HashMap)body;
            this.requestBody = null;
        }else{
            this.requestBody = (String)body;
            this.keyValue = null;
        }
        this.callback = _callback;
        this.restType = _restType;
        this.authFlag = _authFlag;
        this.pref = _pref;
        this.id = _id;
        this.passwd = _passwd;
        this.activity = _activity;
        if(this.authFlag==true){
            this.id = _id;
            this.passwd = _passwd;
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        JSONObject obj = null;
        try {
            obj = connectToJson(this.restType);
        } catch (IOException e) {
            if(response!=null){
                response.getFirstHeader("URI");
                URL url  = null;
                try {
                    url = new URL(target_URI);
                } catch (MalformedURLException e1) {
                    BSDebugger.log(e1, RestTask.this);
                }
                target_URI = url.getHost()+response.getFirstHeader("URI").getValue();
                try {
                    obj = connectToJson(this.restType);
                } catch (IOException e1) {
                    BSDebugger.log(e1, RestTask.this);
                } catch (JSONException e1) {
                    BSDebugger.log(e1, RestTask.this);
                }
                BSDebugger.log(e, RestTask.this);
            }else{
                BSDebugger.log(e,RestTask.this);
            }
        } catch (JSONException e) {
            BSDebugger.log(e,RestTask.this);
        }
        try {
            if(statusCode>=200&&statusCode<400){
                //정상적으로 인증했고, 로그인해야 접속해야 하는경우 아이디 패스워드를 다시 저장한다.
                if(this.pref!=null&&authFlag==true){
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("AQ_REST_ID",this.id);
                    editor.putString("AQ_REST_PASSWD",this.passwd);
                    editor.commit();
                }
                callback.toDoInBackground(obj);
            }else{
                if(statusCode==401){
                    if(this.pref!=null) {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.clear();
                        editor.commit();
                    }
                }else{
                    callback.onErrorCodefind(statusCode,statusString);
                }
            }
        } catch (JSONException e) {
            BSDebugger.log(e,RestTask.this);
        }
        return null;
    }
    private JSONObject connectToJson(RestType _restType) throws IOException, JSONException {
        JSONObject obj = new JSONObject();
        if(compressedJsonData!=null){
          obj = getCompressedJsonRequest(this.restType);
        }else if(_restType== RestType.GET||_restType== RestType.DELETE){
                obj = getRestURI(_restType);
        }else if(_restType== RestType.POST||_restType== RestType.PUT){
                obj = getRestBodyURI(_restType);
        }
        return obj;
    }

    private JSONObject getCompressedJsonRequest(RestType restType) {
        Log.d("BS_REQUEST_DATA",String.valueOf(compressedJsonData));
        JSONObject obj;
        boolean flag = false;
        URL trackerURL = null;
        URLConnection urlConn = null;
        HttpURLConnection httpConn = null;
        // Send post request
        OutputStream stream = null;
        try {
            // Url Connection
            trackerURL = new URL(this.target_URI);
            urlConn = trackerURL.openConnection();
            httpConn = (HttpURLConnection)urlConn;
            httpConn.setRequestProperty("Content-Type", "application/octet-stream");
            httpConn.setRequestProperty("Content-Encoding", "gzip");
            httpConn.setRequestMethod(restType.toString());
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            httpConn.setDefaultUseCaches(false);
            httpConn.setChunkedStreamingMode(1);
            stream =  httpConn.getOutputStream();
            stream.write(compressedJsonData);
            stream.flush();
            statusCode = httpConn.getResponseCode();
            statusString = httpConn.getResponseMessage();
            stream.close();
            flag = true;
            if(statusCode>=200&&statusCode<400){
                //정상 호출인 경우
                InputStreamReader tmp = null;
                tmp = new InputStreamReader(httpConn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuilder builder = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                    builder.append(str + "\n");                     // View에 표시하기 위해 라인 구분자 추가
                }
                String myResult = builder.toString();                       // 전송결과를 전역 변수에 저장
                myResult.replace("\r\n", "\\r\\n");
                Log.d("BS_RESPONSE_DATA",myResult);
                try{
                    obj = new JSONObject(myResult);
                }catch (Exception e){
                    //파싱오류
                    BSDebugger.log(e,this);
                    obj = new JSONObject();
                }
                return obj;
            }else{
                return new JSONObject();
            }
        } catch (Exception e) {
            statusCode = 800;
            BSDebugger.log(e,this);
        }finally{
            try {
                if( stream != null ){
                    stream.close();
                }
                httpConn = null;
                urlConn = null;
                trackerURL = null;
            } catch (IOException e) {
                BSDebugger.log(e, this);
            }
        }
        try {
            return new JSONObject("{response:"+String.valueOf(flag)+"}");
        } catch (JSONException e) {
            BSDebugger.log(e,this);
        }
        return new JSONObject();
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private JSONObject getRestBodyURI(RestType restType) throws IOException, JSONException {
        JSONObject obj = new JSONObject();
        HttpURLConnection http = null;
        URL url  = null;
        url = new URL(target_URI);
        String headertyp=null;
        String headerString = null;
        if(authFlag){
            headertyp ="Authorization";
            String basic = id+":"+passwd;
            byte[] basic64 = basic.getBytes();
            headerString = "Basic "+ Base64.encodeToString(basic64, Base64.NO_WRAP);
        }
        http = (HttpURLConnection) url.openConnection();
        http.setDefaultUseCaches(false);
        http.setDoInput(true);                         // 서버에서 읽기 모드 지정
        http.setDoOutput(true);                       // 서버로 쓰기 모드 지정

        if(restType== RestType.POST){
            http.setRequestMethod("POST");
        }else if(restType== RestType.PUT){
            http.setRequestMethod("PUT");
        }
        StringBuffer buffer = new StringBuffer();
        if(keyValue!=null){
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");
            if(authFlag==true&&headertyp!=null&&headerString!=null){
                http.setRequestProperty(headertyp,  headerString);
            }
            Iterator<String> itr = keyValue.keySet().iterator();
            String params = "";
            params +=getParams(keyValue);
            requestBody = params;
        }
        if(requestBody==null){
            requestBody = "";
        }
        OutputStreamWriter outStream = null;
        outStream = new OutputStreamWriter(http.getOutputStream(), "UTF-8");
        PrintWriter writer = new PrintWriter(outStream);
        writer.write(requestBody);
        writer.flush();
        statusCode = http.getResponseCode();
        statusString = http.getResponseMessage();
        if(statusCode == 302){
            target_URI = response.getFirstHeader("Location").getValue();
            return getRestBodyURI(restType);
        }

        if(statusCode>=200&&statusCode<400){
            //정상 호출인 경우
            InputStreamReader tmp = null;
            tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(tmp);
            StringBuilder builder = new StringBuilder();
            String str;
            while ((str = reader.readLine()) != null) {       // 서버에서 라인단위로 보내줄 것이므로 라인단위로 읽는다
                builder.append(str + "\n");                     // View에 표시하기 위해 라인 구분자 추가
            }
            String myResult = builder.toString();                       // 전송결과를 전역 변수에 저장
            myResult.replace("\r\n", "\\r\\n");
            try {
                obj = new JSONObject(myResult);
            }catch (Exception e){
                BSDebugger.log(e,RestTask.this);
                obj = new JSONObject();
            }
            return obj;
        }else{
            return new JSONObject();
        }
    }

    private String getParams(HashMap<String,String> keyValue) {
        String key;
        String params = "";
        boolean findFlag = false;
        Iterator<String> itr = keyValue.keySet().iterator();
        int count = 0;
        while (itr.hasNext()){
            key = itr.next();
            if(count!=0){
                params +="&";
            }
            params+=key+"="+keyValue.get(key);
            count++;
        }
        return params;
    }

    HttpResponse response;
    @TargetApi(Build.VERSION_CODES.FROYO)
    private JSONObject getRestURI(RestType restType) throws IOException, JSONException {
        JSONObject obj = new JSONObject();
        URL url  = null;
        if(keyValue!=null){
            String params = "?";
            params +=getParams(keyValue);
            if(!keyValue.isEmpty()){
                target_URI += params;
            }
        }
        url = new URL(target_URI);
        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
//        HttpResponse response = null;
        response = null;
        String headertyp=null;
        String headerString = null;
        if(authFlag){
            headertyp ="Authorization";
            String basic = id+":"+passwd;
            byte[] basic64 = basic.getBytes();
            headerString = "Basic "+ Base64.encodeToString(basic64, Base64.NO_WRAP);
        }
        if(restType== RestType.GET){
            HttpGet httpGet = new HttpGet(target_URI);
            if(authFlag==true&&headertyp!=null&&headerString!=null){
                httpGet.setHeader(headertyp,  headerString);
            }
            response = httpClient.execute(httpGet, localContext);
        }else if(restType== RestType.DELETE){
            HttpDelete httpDelete = new HttpDelete(target_URI);
            if(authFlag==true&&headertyp!=null&&headerString!=null){
                httpDelete.setHeader(headertyp,  headerString);
            }
            response = httpClient.execute(httpDelete, localContext);
        }
        statusCode = response.getStatusLine().getStatusCode();
        statusString = response.getStatusLine().getReasonPhrase();

        if(statusCode == 302){
            target_URI = response.getFirstHeader("Location").getValue();
            return getRestURI(restType);
        }

        if(statusCode>=200&&statusCode<400){
            //정상호출인 경우
            String result = "";
            BufferedReader reader = null;
            reader = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()
                    )
            );
            String line = null;
            while ((line = reader.readLine()) != null){
                result += line + "\n";
            }
            String myResult = result;                       // 전송결과를 전역 변수에 저장
            obj = new JSONObject(myResult);
            return obj;
        }else{
            return new JSONObject();
        }
    }
}
