package mobi.happyend.framework.network.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class HdHttpUtil {
	public static final int NETWORK_OPERATOR_UNKOWN = 0;
	public static final int NETWORK_OPERATOR_MOBILE = 1;
	public static final int NETWORK_OPERATOR_UNICOM = 2;
	public static final int NETWORK_OPERATOR_TELECOM = 3;
	
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    /**
     * String is null。
     *
     * @param src
     * @return boolean
     */
    public static boolean isEmptyString(String src) {
        return src == null || src.trim().length() == 0;
    }
    
    /**
     * Return if the device is connected to wifi.
     *
     * @return boolean
     */
    public static boolean isWifi(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * Return if the device is connected to the 3G net.
     *
     * @param context
     * @return boolean
     */
    public static boolean is3G(Context context){
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephony.getNetworkType();

        return networkType == TelephonyManager.NETWORK_TYPE_UMTS ||
                networkType == TelephonyManager.NETWORK_TYPE_HSDPA ||
                networkType == TelephonyManager.NETWORK_TYPE_EVDO_0 ||
                networkType == TelephonyManager.NETWORK_TYPE_EVDO_A ||
                networkType == TelephonyManager.NETWORK_TYPE_LTE ||
                networkType == TelephonyManager.NETWORK_TYPE_EVDO_B ||
                networkType == TelephonyManager.NETWORK_TYPE_EHRPD ||
                networkType == TelephonyManager.NETWORK_TYPE_HSUPA;

    }

    /**
     * Return if the device is connected to the 2G net.
     *
     * @param context
     * @return boolean
     */
    public static boolean is2G(Context context){
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = telephony.getNetworkType();

        return networkType == TelephonyManager.NETWORK_TYPE_1xRTT ||
                networkType == TelephonyManager.NETWORK_TYPE_CDMA ||
                networkType == TelephonyManager.NETWORK_TYPE_EDGE ||
                networkType == TelephonyManager.NETWORK_TYPE_IDEN ||
                networkType == TelephonyManager.NETWORK_TYPE_GPRS;

    }


	/**
	 * Return if the network is available for the device
	 * 
	 * @return  boolean
	 */
	public static boolean isNetAvailable(Context context) {
	    ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	    if (networkInfo != null) {
	        return networkInfo.isAvailable();
	    }
	
	    return false;
	}
	
	/**
	 * Return if the device is connected to mobile network.
	 * 
	 * @return boolean
	 */
	public static boolean isMobile(Context context) {
	    try {
	        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
	            return true;
	        }
	    } catch (Exception e) {
	    }
	    return false;
	}

    /**
     * Return if the device is connected to wap network.
     *
     * @return boolean
     */
	public static boolean isWap(String proxyhost) {
		Pattern pattern = Pattern.compile("^[0]{0,1}10\\.[0]{1,3}\\.[0]{1,3}\\.172$", Pattern.MULTILINE);
		boolean ret = false;
		Matcher m = pattern.matcher(proxyhost);
		if(m.find()){
			ret = true;
		}else{
			ret = false;
		}
		return ret;
	}
	
	/**
	 * return if proxy is used for connect to the network
	 * @return
	 */
	public static boolean isPorxyUsed(Context context) {
		if (isWifi(context)) {
			return false;
		}
		
		// 不管cmnet和cmwap都选择直连方式
		if (readNetworkOperatorType(context) == NETWORK_OPERATOR_MOBILE) {
			return false;
		}
	
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (isEmptyString(proxyHost)) {
			return false;
		}
	
		return true;
	}
	
    /**
     * read the network operator type
     */
	public static int readNetworkOperatorType(Context context) {
        TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getNetworkOperator();

        // 飞行模式下 获取不到operator
        if (isEmptyString(operator)) {
        	return NETWORK_OPERATOR_UNKOWN;
        }
        
        // 非中国运营商
        String mcc = operator.substring(0,3);
        if (mcc == null || !mcc.equals("460")){
        	return NETWORK_OPERATOR_UNKOWN;
        }
        
        // operator 由mcc + mnc组成 中国的mcc为460
        // 这里取得mnc来判断是国内的哪个运营商
        String mnc = operator.substring(3);
        int mncIntVar = 0;
        try {
            mncIntVar = Integer.parseInt(mnc);
        } catch (NumberFormatException e) {
        }

        switch (mncIntVar) {
            case 0:
            case 2:
            case 7:
            	return NETWORK_OPERATOR_MOBILE;
            case 1:
            case 6:
            	return NETWORK_OPERATOR_UNICOM;
            case 3:
            case 5:
            	return NETWORK_OPERATOR_TELECOM;
            default:
                break;
        }
        
        return NETWORK_OPERATOR_UNKOWN;
    }

    /**
     * gzip decompress
     * @param is
     * @param os
     * @throws Exception
     */
    public static void decompress(InputStream is, OutputStream os)throws Exception{
        GZIPInputStream gin = new GZIPInputStream(is);
        int count;
        byte data[] = new byte[1024];
        while ((count = gin.read(data, 0, 1024)) != -1){
            os.write(data, 0, count);
        }
        gin.close();
    }

    /**
     * gzip compress
     * @param is
     * @param os
     * @throws Exception
     */
    public static void compress(InputStream is, OutputStream os)throws Exception {
        GZIPOutputStream gos = new GZIPOutputStream(os);
        int count;
        byte data[] = new byte[1024];
        while ((count = is.read(data, 0, 1024)) != -1) {
            gos.write(data, 0, count);
        }
        gos.flush();
        gos.finish();
        gos.close();
    }

    public static byte[] compress(byte[] is) throws Exception {
        ByteArrayInputStream tmpInput = new ByteArrayInputStream(is);
        ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(1024);
        HdHttpUtil.compress(tmpInput, tmpOutput);
        return tmpOutput.toByteArray();
    }
}
