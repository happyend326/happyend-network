package demo.network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import demo.network.receiver.ConnectionReceiver;

/**
 * Created with IntelliJ IDEA.
 * User: xulingzhi
 * Date: 13-5-28
 * Time: 下午3:03
 * To change this template use File | Settings | File Templates.
 */
public class BaseActivity extends Activity {

    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        receiver = new ConnectionReceiver();
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
