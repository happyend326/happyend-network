package demo.network;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import mobi.happyend.framework.network.HdHttpClient;
import mobi.happyend.framework.network.HdHttpRequestException;
import mobi.happyend.framework.network.HdHttpResponse;
import mobi.happyend.framework.network.HdHttpResponseException;
import mobi.happyend.framework.network.utils.HdHttpUtil;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends BaseActivity {

    Button btn1;
    Button btn2;
    Button btn3;
    TextView text;
    HdHttpClient httpClient ;
    ImageView image;

    boolean isCancelFile;
    boolean isCancelBitmap;
    String btn2Str;
    String btn3Str;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.demo4);

        httpClient = new HdHttpClient().supportGzip().
                supportMainThreadCheck().supportTimeoutAndRetry();

        this.btn1 = (Button)this.findViewById(R.id.download_file);
        this.btn2 = (Button)this.findViewById(R.id.download_bitmap);
        btn2Str = btn2.getText().toString();
        btn2.setText(btn2Str+"--开始下载");
        this.btn3 = (Button)this.findViewById(R.id.download_file_b);
        btn3Str = btn3.getText().toString();
        btn3.setText(btn3Str+"--开始下载");
        this.text = (TextView)this.findViewById(R.id.dataview);
        this.image = (ImageView)this.findViewById(R.id.image);

        this.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DownloadFileTask().execute(false);
            }
        });

        this.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCancelBitmap = !isCancelBitmap;
                if(isCancelBitmap){
                    btn2.setText(btn2Str + "--开始下载");
                    httpClient.cancel();
                } else {
                    btn2.setText(btn2Str + "--停止下载");
                    new DownloadBitmap().execute();
                }
            }
        });

        this.btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isCancelFile = !isCancelFile;
                if(isCancelFile){
                    btn3.setText(btn3Str + "--开始下载");
                    httpClient.cancel();
                } else {
                    btn3.setText(btn3Str + "--停止下载");
                    new DownloadFileTask().execute(true);
                }
            }
        });
    }

    private class DownloadFileTask extends AsyncTask<Boolean, Long, File> {
        @Override
        protected File doInBackground(Boolean... voids) {
            boolean isBreakPoint = voids[0];
            String filePath = Environment.getExternalStorageDirectory()+File.separator+"tbclient_4_0_0.apk";
            try {
                httpClient.download("http://static.tieba.baidu.com/client/android/tbclient_4_0_0.apk", filePath, new HdHttpClient.DownloadProgressListener() {
                    @Override
                    public void onProgress(long downloaded, long contentLength) {
                        Log.d("benben", "total:"+contentLength+"-----"+"downloaded:"+downloaded);
                        publishProgress(downloaded, contentLength);
                    }
                }, isBreakPoint);
            } catch (HdHttpRequestException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (HdHttpResponseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            return new File(filePath);
        }

        @Override
        protected void onProgressUpdate(Long...progress){
            long dowloaded = progress[0];
            long total = progress[1];

            long per =  dowloaded*100/total;
            if(per > 0) {
                text.setText("downloaded:"+per+"%");
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if(result == null) {
                text.setText("加载错误");
            } else {
                text.setText("success! size:"+result.length());
            }

            super.onPostExecute(result);

        }

    }

    private class DownloadBitmap extends AsyncTask<Void, Long, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;
            String filePath = Environment.getExternalStorageDirectory()+File.separator+"20130327329.jpg";
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                bitmap = httpClient.downloadBitmap("http://hi.baidu.com/cm/static/20130327329.jpg", filePath, options, new HdHttpClient.DownloadProgressListener() {
                    @Override
                    public void onProgress(long downloaded, long contentLength) {
                        publishProgress(downloaded, contentLength);

                    }
                }, true);
            } catch (HdHttpRequestException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (HdHttpResponseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Long...progress){
            long dowloaded = progress[0];
            long total = progress[1];

            long per =  dowloaded*100/total;
            if(per > 0) {
                text.setText("downloaded:"+per+"%");
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result == null) {
                //text.setText("加载错误");
            } else {
                text.setText("success!");
                image.setImageBitmap(result);
            }

            super.onPostExecute(result);

        }
    }

}