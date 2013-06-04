package demo.network;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;
import mobi.happyend.framework.network.CustomMultipartEntity;
import mobi.happyend.framework.network.HdHttpClient;
import mobi.happyend.framework.network.HdHttpRequestException;
import mobi.happyend.framework.network.HdHttpResponse;
import org.apache.http.HttpResponse;

import java.io.File;

public class UploadActivity extends BaseActivity {
    ProgressBar progressBar;
    HdHttpClient httpClient;
    String SDCardRoot;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.demo1);
        httpClient = new HdHttpClient();

        SDCardRoot = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;

        this.progressBar = (ProgressBar)this.findViewById(R.id.progressbar);

        progressBar.setMax(100);

        Button btn = (Button)this.findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload();
            }
        });
    }

    private void upload(){
        new UploadTask().execute();
    }

    private class UploadTask extends AsyncTask<Void, Long, String> {
        @Override
        protected String doInBackground(Void... voids) {
            File file = new File(SDCardRoot+"DCIM/Camera/1364112813723.jpg");
            final long filesize = file.length();
            Log.d("benben", "filesize:"+filesize);
            HdHttpClient.UploadFile uploadFile = new HdHttpClient.UploadFile("file", file, new CustomMultipartEntity.ProgressListener(){

                @Override
                public void transferred(long num) {
                    Log.d("benben", "num:"+num);
                    publishProgress(num, filesize);
                }
            });
            try {
                HdHttpResponse res =  httpClient.post("http://happyend.me/upload.php", uploadFile);
                return res.asString();
            } catch (HdHttpRequestException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Long... progress) {
            long now = progress[0];
            long length = progress[1];
            progressBar.setProgress((int)(now*100/length));
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(UploadActivity.this, result, Toast.LENGTH_SHORT).show();
            super.onPostExecute(result);

        }

    }

}