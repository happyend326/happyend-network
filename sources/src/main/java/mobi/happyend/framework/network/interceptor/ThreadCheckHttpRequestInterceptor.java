package mobi.happyend.framework.network.interceptor;

import android.os.Looper;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

public class ThreadCheckHttpRequestInterceptor implements
		HttpRequestInterceptor {

	@Override
	public void process(HttpRequest request, HttpContext context)
			throws HttpException, IOException {
		Looper looper = Looper.myLooper();
		if(looper != null && looper == Looper.getMainLooper()){
			throw new RuntimeException("Main thread HTTP Request is not allowed");
		}
	}

}
