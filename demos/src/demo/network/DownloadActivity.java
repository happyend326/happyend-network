package demo.network;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import mobi.happyend.framework.network.HdHttpClient;
import mobi.happyend.framework.network.HdHttpRequestException;
import mobi.happyend.framework.network.HdHttpResponse;
import mobi.happyend.framework.network.utils.HdHttpUtil;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity {

    Button btn1;
    Button btn2;
    TextView text;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.demo2);

        this.btn1 = (Button)this.findViewById(R.id.getgzipdata);
        this.btn2 = (Button)this.findViewById(R.id.postgzipdata);
        this.text = (TextView)this.findViewById(R.id.dataview);

        this.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetDataTask().execute();
            }
        });

        this.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new PostDataTask().execute();
            }
        });
    }

    private class GetDataTask extends AsyncTask<Void, Long, String> {
        @Override
        protected String doInBackground(Void... voids) {
            HdHttpClient httpClient = new HdHttpClient().supportGzip().
                    supportMainThreadCheck().supportTimeoutAndRetry();
            httpClient.setHeader("Accept-Encoding","gzip,deflate,sdch");

            String url = "http://testapi.zank.mobi/snowball/api/message/msg/getTalks.json?cv=1.0.4&ct=android-zank&token=24b4614e29790937935525d6c32959fd";
            try {
                HdHttpResponse res = httpClient.get(url);
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

    private class PostDataTask extends AsyncTask<Void, Long, String> {
        @Override
        protected String doInBackground(Void... voids) {
            HdHttpClient httpClient = new HdHttpClient().supportGzip().
                    supportMainThreadCheck().supportTimeoutAndRetry();
            httpClient.setHeader("Accept-Encoding","gzip,deflate,sdch");

            String url = "http://happyend.me/demos/framework/network/postgzip.php";
            try {
                String postdata = "hello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gziphello gzip";
                byte[] b = HdHttpUtil.compress(postdata.toString().getBytes());
                HdHttpClient.UploadFile file = new HdHttpClient.UploadFile("bytes", b);

                List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
                HdHttpResponse res = httpClient.post(url,file);
                return res.asString();
            } catch (HdHttpRequestException e) {
                e.printStackTrace();
                String s = e.getMessage();
            } catch (Exception e) {

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

}