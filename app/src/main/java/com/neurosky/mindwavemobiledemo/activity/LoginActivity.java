package com.neurosky.mindwavemobiledemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.neurosky.mindwavemobiledemo.R;
import com.neurosky.mindwavemobiledemo.helper.Constants;
import com.neurosky.mindwavemobiledemo.helper.Utils;
import com.neurosky.mindwavemobiledemo.helper.WebRequestHelper;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends AppCompatActivity {

    Button enterBtn;
    Intent enterIntent;
    EditText id;
    String failureMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        id = (EditText) findViewById(R.id.login_idEdit);
        enterBtn = (Button)findViewById(R.id.enter);

        id.setText("");

         /*button listeners*/
        /*listener for enter button*/
        enterBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                final ValidateIDTask validateIDtask = new ValidateIDTask(LoginActivity.this);
                try {
                    validateIDtask.execute(id.getText().toString(), Constants.LOGIN_INTENT);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }


    private class ValidateIDTask extends AsyncTask<String, Integer, String> {


        private Context context;
        private boolean doLogin = false;

        public ValidateIDTask(Context context) {
            this.context = context;
        }


        @Override
        protected String doInBackground(String... param) {

            if(doLogin==true){

                final String id = param[0];
                String intent = param[1];

                JSONObject jsonObj = new JSONObject();
                try{
                    jsonObj.put("ID", id);
                    jsonObj.put(Constants.INTENT_KEY, intent);
                }catch(Exception ex){
                    ex.printStackTrace();
                }

                //JSONObject resultJSON = webRequestHelper.doGet(jsonObject.toString());
                WebRequestHelper.get("/validateID/ " +jsonObj.toString(), null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // If the response is JSONObject instead of expected JSONArray
                        Log.d(Constants.CUSTOM_LOG_TYPE, response.toString());

                        //open HomeScreen Activity
                        String status = "";
                        try {
                            status = response.getString("status");
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                        if(status.equals("success")){

                            String sessionID = id + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                            Log.d(Constants.CUSTOM_LOG_TYPE, sessionID);

                            String isAdmin = "";
                            try {
                                isAdmin = response.getString("is_admin");
                                Log.d(Constants.CUSTOM_LOG_TYPE, "is admin-->"+ isAdmin);
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                            enterIntent = new Intent(LoginActivity.this, DemoActivity.class);
                            enterIntent.putExtra(Constants.INTENT_KEY, Constants.LOGIN_INTENT);
                            enterIntent.putExtra("ISADMIN", isAdmin);
                            enterIntent.putExtra("ID", id);
                            enterIntent.putExtra("SESSIONID", sessionID);
                            startActivity(enterIntent);
                        }else if(status.equals("exception")){
                            try{
                                failureMsg = response.getString("message");
                            }catch(Exception ex){
                                ex.printStackTrace();
                            }
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(LoginActivity.this.getBaseContext(), failureMsg, Toast.LENGTH_LONG).show();
                                }
                            });
                        }else{
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(LoginActivity.this.getBaseContext(), "Invalid ID", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, final String responseString, Throwable throwable) {
                        //super.onFailure(statusCode, headers, responseString, throwable);
                        Log.d(Constants.CUSTOM_LOG_TYPE, "ON failure response-->" + responseString);
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoginActivity.this.getBaseContext(), "Failure Message-->" + responseString, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onRetry(int requestNum) {
                        super.onRetry(requestNum);

                        Log.d(Constants.CUSTOM_LOG_TYPE, "on retry->");
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(LoginActivity.this.getBaseContext(), "Couldn't connect with the server. Retrying...", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                });

            }

            return "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //get value of id
            String idVal = id.getText().toString();
            Log.d(Constants.CUSTOM_LOG_TYPE, "id entered-->" + idVal);
            if(idVal==null || idVal.isEmpty()){
                Log.d(Constants.CUSTOM_LOG_TYPE, "Empty ID" + idVal);
                Toast.makeText(LoginActivity.this, "Enter ID first", Toast.LENGTH_SHORT).show();

            }else{
                doLogin = true;
            }

        }

    }

}