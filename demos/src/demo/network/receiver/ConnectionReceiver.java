package demo.network.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import mobi.happyend.framework.network.utils.HdHttpUtil;

/**
 * Created with IntelliJ IDEA.
 * User: xulingzhi
 * Date: 13-5-28
 * Time: 下午2:58
 * To change this template use File | Settings | File Templates.
 */
public class ConnectionReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        if(HdHttpUtil.isNetAvailable(context)){
            if(HdHttpUtil.isWifi(context)){
                Toast.makeText(context, "connect wifi", Toast.LENGTH_SHORT).show();
            } else if(HdHttpUtil.is3G(context)){
                Toast.makeText(context, "connect 3g", Toast.LENGTH_SHORT).show();
            } else if(HdHttpUtil.is2G(context)){
                Toast.makeText(context, "connect 2g", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "lost connection", Toast.LENGTH_SHORT).show();
        }
    }
}
