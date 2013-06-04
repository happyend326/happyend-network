package demo.network;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import mobi.happyend.framework.network.CustomMultipartEntity;
import mobi.happyend.framework.network.HdHttpClient;
import mobi.happyend.framework.network.HdHttpRequestException;
import mobi.happyend.framework.network.HdHttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class BasicAuthActivity extends BaseActivity {

    Button btn1;
    TextView text;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.demo3);

        this.btn1 = (Button)this.findViewById(R.id.basicauth);
        this.text = (TextView)this.findViewById(R.id.dataview);

        this.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PostDataTask().execute();
            }
        });

    }

    private class PostDataTask extends AsyncTask<Void, Long, String> {
        @Override
        protected String doInBackground(Void... voids) {
            HdHttpClient httpClient = new HdHttpClient().supportBasicAuth(new AuthScope("testapi.zank.mobi", 80), "48661038@qq.com", md5("111111")).supportGzip();
            httpClient.setHeader("Accept-Encoding","gzip,deflate,sdch");

            String url = "baisc auth url";
            try {
                HdHttpResponse res = httpClient.post(url);
                return res.asString();
            } catch (HdHttpRequestException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == null) {
                text.setText("加载错误");
            } else {
                text.setText(result);
            }

            super.onPostExecute(result);

        }

    }

    public static String md5(String text) {
        if (text == null) {
            return "";
        }

        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());

            byte[] digest = md.digest();

            for (byte aDigest : digest) {
                text = Integer.toHexString(0xFF & aDigest);
                if (text.length() < 2) {
                    text = "0" + text;
                }
                hexString.append(text);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hexString.toString();
    }
}