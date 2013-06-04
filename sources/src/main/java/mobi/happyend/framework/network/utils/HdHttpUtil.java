package mobi.happyend.framework.network.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class HdHttpUtil {
	public static final int NETWORK_OPERATOR_UNKOWN = 0;
	public static final int NETWORK_OPERATOR_MOBILE = 1;
	public static final int NETWORK_OPERATOR_UNICOM = 2;
	public static final int NETWORK_OPERATOR_TELECOM = 3;
	
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    /**
     * 字符串是否为空。
     * 
     * @param src 字符串
     * @return 是否为空
     */
    public static boolean isEmptyString(String src) {
        return src == null || src.trim().length() == 0;
    }
    
    /**
     * 当前是否是wifi网络。
     * 
     * @return 当前是否是wifi网络
     */
    public static boolean isWifi() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)BdHttpManager.getInstance()
            		.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }


	/**
	 * 判断当前网络连接是否可用
	 * 
	 * @return
	 */
	public static boolean isNetAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager)BdHttpManager.getInstance()
	    		.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
	    if (networkInfo != null) {
	        return networkInfo.isAvailable();
	    }
	
	    return false;
	}
	
	/**
	 * 当前是否是移动网络。
	 * 
	 * @return 当前是否是移动网络
	 */
	public static boolean isMobile() {
	    try {
	        ConnectivityManager connectivityManager = (ConnectivityManager)BdHttpManager.getInstance()
	        		.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
	        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
	            return true;
	        }
	    } catch (Exception e) {
	    }
	    return false;
	}
	
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
	 * 根据请求应答的contentType判断是否是wap资费页。
	 * 
	 * @param contentType
	 * @return 是否是wap资费页
	 */
	public static boolean isWAPFeePage(String contentType) {
	    return contentType != null && contentType.contains("vnd.wap.wml");
	}
	
	/**
	 * 从请求应答的contentType中解析charset信息。
	 * 
	 * @param contentType
	 * @return charset
	 */
	public static String parseCharset(String contentType) {
	    String codingType = "utf-8";
	    if (contentType != null) {
	        String[] segs = contentType.split(";");
	        for (String seg : segs) {
	            if (seg.contains("charset")) {
	                String[] nv = seg.split("=");
	                if (nv.length > 1)
	                    codingType = nv[1].trim();
	                break;
	            }
	        }
	    }
	    return codingType;
	}
	
	/**
	 * 在建立连接时是否使用代理
	 * @return
	 */
	public static boolean isPorxyUsed() {
		if (isWifi()) {
			return false;
		}
		
		// 不管cmnet和cmwap都选择直连方式
		if (readNetworkOperatorType() == NETWORK_OPERATOR_MOBILE) {
			return false;
		}
	
		String proxyHost = android.net.Proxy.getDefaultHost();
		if (isEmptyString(proxyHost)) {
			return false;
		}
	
		return true;
	}
	
    /**
     * 读取运营商类型
     */
	public static int readNetworkOperatorType() {
        TelephonyManager telManager = (TelephonyManager)BdHttpManager.getInstance().getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
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

	public static boolean writeFile(String dest, byte[] content, long startpos) {
		if(dest==null || content==null || content.length<=0)
			return false;

		try {
			RandomAccessFile destfile = new RandomAccessFile (dest, "rw");
			destfile.seek(startpos);
			destfile.write(content);
			destfile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return true;
	}
	
	public static byte[] readFile(String src) {
		File file = new File(src);
		if(file.exists() && file.isFile())
			return readFile(src, 0, file.length());
		return null;
	}
	
	public static byte[] readFile(String src, long startpos, long endpos) {
		try {
			RandomAccessFile srcfile = new RandomAccessFile (src, "r");
			if (srcfile.length()<=startpos) {
				srcfile.close();
				return null;
			}
			srcfile.seek(startpos);
			byte[] buffer = new byte[(int) ((endpos>srcfile.length()?srcfile.length():endpos)-startpos)];
			srcfile.read(buffer);
			srcfile.close();
			return buffer;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static byte[] mergeByteArray(ArrayList<byte[]> lst) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			for(byte[] btarr : lst) {
				out.write(btarr);
			}
			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static InputStream byte2Stream(ArrayList<byte[]> lst) {
		try {
			return byte2Stream(mergeByteArray(lst));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static InputStream byte2Stream(byte[] btarr) {
    	try {
    		return new ByteArrayInputStream(btarr);
    	} catch (Exception e) {
    		return null;
    	}
	}

	public static int getDefaultSliceSize() {
		if(isWifi())
			return 500000;
		else
			return 200000;
	}
	
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        if (input == null) {
            return -1;
        }

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}
