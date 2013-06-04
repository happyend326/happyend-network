package mobi.happyend.framework.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import mobi.happyend.framework.config.HdConfig;
import mobi.happyend.framework.network.interceptor.GzipHttpResponseInterceptor;
import mobi.happyend.framework.network.interceptor.PreemptiveAuthHttpRequestInterceptor;
import mobi.happyend.framework.network.interceptor.ThreadCheckHttpRequestInterceptor;
import mobi.happyend.framework.network.utils.HdHttpUtil;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * Wrap of org.apache.http.impl.client.DefaultHttpClient
 * 
 * @author happyend
 * 
 */
public class HdHttpClient {

    public static String DEFAULT_PROPERTY_FILENAME = "appConfig.properties";

	/** OK: Success! */
	public static final int OK = 200;
    /** Patial content for break point download */
    public static final int PARTIAL = 206;
	/** Not Modified: There was no new data to return. */
	public static final int NOT_MODIFIED = 304;
	/**
	 * Bad Request: The request was invalid. An accompanying error message will
	 * explain why. This is the status code will be returned during rate
	 * limiting.
	 */
	public static final int BAD_REQUEST = 400;
	/** Not Authorized: Authentication credentials were missing or incorrect. */
	public static final int NOT_AUTHORIZED = 401;
	/**
	 * Forbidden: The request is understood, but it has been refused. An
	 * accompanying error message will explain why.
	 */
	public static final int FORBIDDEN = 403;
	/** Not Found: The URI requested is invalid or the resource requested does not exists. */
	public static final int NOT_FOUND = 404;
	/**
	 * Not Acceptable: Returned by the Search API when an invalid format is
	 * specified in the request.
	 */
	public static final int NOT_ACCEPTABLE = 406;

    public static final int RANGE_NOT_SATISFIED = 416;
	/** Internal Server Error: Something is broken.*/
	public static final int INTERNAL_SERVER_ERROR = 500;
	/** Bad Gateway */
	public static final int BAD_GATEWAY = 502;
	/** Service Unavailable */
	public static final int SERVICE_UNAVAILABLE = 503;

    public static final int INTERNAL_ERROR = -1;

	private DefaultHttpClient mClient;
	private Map<String, String> headers;
	
	private CookieStore persistantCookieStore;
	private CookieStore tempCookieStore;

    private boolean isCancel = false;
	
	public HdHttpClient() {
        HdConfig.getInstance().loadProperties(HdHttpClient.class.getResourceAsStream(DEFAULT_PROPERTY_FILENAME));
		// Create and initialize HTTP parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		// Create and initialize scheme registry
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		// Create an HttpClient with the ThreadSafeClientConnManager.
		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
				params, schemeRegistry);

        // initialize cookie store
		persistantCookieStore = new BasicCookieStore();
		mClient = new DefaultHttpClient(cm, params);
	}
	
	public DefaultHttpClient getDelegateHttpClient(){
		return this.mClient;
	}
	
	/**
	 * @param host the hostname (IP or DNS name)
	 * @param port the port number. -1 indicates the scheme default port.
	 * @param scheme the name of the scheme. null indicates the default scheme
	 */
	public HdHttpClient setProxy(String host, int port, String scheme) {
		HttpHost proxy = new HttpHost(host, port, scheme);
		mClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		return this;
	}
	

	public HdHttpClient removeProxy() {
		mClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
		return this;
	}
	
	/**
	 * @param agent user defined agent
	 */
	public HdHttpClient setUserAgent(String agent){
		if(agent == null){
			agent = HdConfig.getInstance().getProperty("http.userAgent");
		}
		mClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, agent);
		return this;
	}
	
	// Configuration
	public HdHttpClient supportGzip(){
		mClient.addResponseInterceptor(new GzipHttpResponseInterceptor());
		return this;
	}

    public HdHttpClient supportBasicAuth(AuthScope scope, String username, String password){
        mClient.getCredentialsProvider().setCredentials(scope, new UsernamePasswordCredentials(username, password));
        mClient.addRequestInterceptor(new PreemptiveAuthHttpRequestInterceptor());
        return this;
    }
	
	public HdHttpClient supportMainThreadCheck(){
		mClient.addRequestInterceptor(new ThreadCheckHttpRequestInterceptor());
		return this;
	}
	
	public HdHttpClient supportTimeoutAndRetry(){
		supportTimeoutAndRetry(HdConfig.getInstance().getIntProperty("http.connectTimeoutMs"), HdConfig.getInstance().getIntProperty("http.socketTimeoutMs"), HdConfig.getInstance().getIntProperty("http.timeoutRetryTimes"));
		return this;
	}
	
	public HdHttpClient supportTimeoutAndRetry(final int retries){
		supportTimeoutAndRetry(HdConfig.getInstance().getIntProperty("http.connectTimeoutMs"), HdConfig.getInstance().getIntProperty("http.socketTimeoutMs"), retries);
		return this;
	}
	
	public HdHttpClient supportTimeoutAndRetry(final int timeout, final int retries){
		supportTimeoutAndRetry(timeout, timeout, retries);
		return this;
	}

    public HdHttpClient supportTimeoutAndRetry(final int connTimeout, final int soTimeout, final int retries){
        HttpConnectionParams.setConnectionTimeout(mClient.getParams(),
                connTimeout);
        HttpConnectionParams
                .setSoTimeout(mClient.getParams(), soTimeout);
        mClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount,
                                        HttpContext context) {
                if (executionCount >= retries) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {
                    // Retry if the server dropped connection on us
                    return true;
                }

                if(exception instanceof  ClientProtocolException){
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {
                    // Do not retry on SSL handshake exception
                    return false;
                }
                HttpRequest request = (HttpRequest) context
                        .getAttribute(ExecutionContext.HTTP_REQUEST);
                boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
                if (!idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        });
        return this;
    }
	
	
	/**
	 * Write cookie
	 * @param name cookie name
	 * @param value cookie value
	 */
	public HdHttpClient setCookie(String name, String value, String domain, long expires, boolean persistant){
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(domain);
        cookie.setPath("/");
        Date expiry = new Date();
        //Set cookie expired time
        expiry.setTime(expiry.getTime() + expires);
        cookie.setExpiryDate(expiry);
        if(persistant){
        	persistantCookieStore.addCookie(cookie);
        	this.getDelegateHttpClient().setCookieStore(persistantCookieStore);
        } else {
        	tempCookieStore = new BasicCookieStore();
        	tempCookieStore.addCookie(cookie);
        	this.getDelegateHttpClient().setCookieStore(tempCookieStore);
        }
        
        return this;
	}
	
	public HdHttpClient setCookie(String name, String value, boolean persistant){
		return setCookie(name, value, HdConfig.getInstance().getProperty("http.cookieDomain"), HdConfig.getInstance().getIntProperty("http.cookieExpires"), persistant);
	}
	
	public HdHttpClient setCookie(String name, String value){
		return setCookie(name, value, HdConfig.getInstance().getProperty("http.cookieDomain"), HdConfig.getInstance().getIntProperty("http.cookieExpires"), false);
	}
	
	
	/**
	 * Set http header
	 * @param key http header key
	 * @param value http header value
	 */
	public HdHttpClient setHeader(String key, String value){
		headers = new HashMap<String, String>();
		headers.put(key, value);
		return this;
	}

	///////////////////////////////POST REQUEST////////////////////////////////////////////////////////

    public HdHttpResponse post(String url, List<BasicNameValuePair> params, String encode) throws HdHttpRequestException {
        return httpRequest(url, params, HttpPost.METHOD_NAME, encode);
    }

	public HdHttpResponse post(String url, List<BasicNameValuePair> params) throws HdHttpRequestException {
		return httpRequest(url, params, HttpPost.METHOD_NAME, HdConfig.getInstance().getProperty("http.encode"));
	}

    public HdHttpResponse post(String url, String encode) throws HdHttpRequestException {
        return httpRequest(url, null, HttpPost.METHOD_NAME, encode);
    }

	public HdHttpResponse post(String url) throws HdHttpRequestException {
		return httpRequest(url, null, HttpPost.METHOD_NAME, HdConfig.getInstance().getProperty("http.encode"));
	}


    /////////////////////////////// UPLOAD FILE ////////////////////////////////////////////////////////

    public HdHttpResponse post(String url, UploadFile file, List<BasicNameValuePair> params, String encode) throws HdHttpRequestException {
        return httpRequest(url, params, file, HttpPost.METHOD_NAME, encode, 0);
    }

    public HdHttpResponse post(String url, UploadFile file, List<BasicNameValuePair> params)
            throws HdHttpRequestException {
        return httpRequest(url, params, file, HttpPost.METHOD_NAME, HdConfig.getInstance().getProperty("http.encode"), 0);
    }

    public HdHttpResponse post(String url, UploadFile file, String encode)
            throws HdHttpRequestException {
        return httpRequest(url, null, file, HttpPost.METHOD_NAME, encode, 0);
    }

    public HdHttpResponse post(String url, UploadFile file)
			throws HdHttpRequestException {
		return httpRequest(url, null, file, HttpPost.METHOD_NAME, HdConfig.getInstance().getProperty("http.encode"), 0);
	}


    /////////////////////////////// GET REQUEST ////////////////////////////////////////////////////////

    public HdHttpResponse get(String url, List<BasicNameValuePair> params, String encode, long breakpoint) throws HdHttpRequestException {
        if(params != null){
            if(url.indexOf("?") != -1){
                url = url + "&" +this.buildQueryString(params, encode);
            } else {
                url = url + "?" + this.buildQueryString(params, encode);
            }
        }

        return httpRequest(url, null, HttpGet.METHOD_NAME, encode, breakpoint);
    }

    public HdHttpResponse get(String url, String encode, long breakpoint) throws HdHttpRequestException {
        return get(url, null, encode, breakpoint);
    }

    public HdHttpResponse get(String url, String encode) throws HdHttpRequestException {
        return get(url, null, encode, 0);
    }

    public HdHttpResponse get(String url, long breakpoint) throws HdHttpRequestException {
        return get(url, null, HdConfig.getInstance().getProperty("http.encode"), breakpoint);
    }

    public HdHttpResponse get(String url) throws HdHttpRequestException {
		return get(url, null, HdConfig.getInstance().getProperty("http.encode"), 0);
	}


    /////////////////////////////// DOWNLOAD Bitmap REQUEST ////////////////////////////////////////////////////////

    public Bitmap downloadBitmap(String url, String savePath, BitmapFactory.Options options, DownloadProgressListener listener, boolean isDownloadFromBreakPoint)  throws HdHttpRequestException, HdHttpResponseException{
        byte[] out = downloadBytes(url, savePath, listener, isDownloadFromBreakPoint);
        if(savePath != null && isDownloadFromBreakPoint){
            if(options != null){
                return BitmapFactory.decodeFile(savePath, options);
            }  else {
                return BitmapFactory.decodeFile(savePath);
            }
        }

        if(out != null){
            if(options == null){
                return BitmapFactory.decodeByteArray(out, 0, out.length);
            } else {
                return BitmapFactory.decodeByteArray(out, 0, out.length, options);
            }
        }
        return null;
    }

    public Bitmap downloadBitmap(String url, DownloadProgressListener listener)  throws HdHttpRequestException, HdHttpResponseException{
        return downloadBitmap(url, null, null, listener, false);
    }

    public Bitmap downloadBitmap(String url) throws HdHttpRequestException, HdHttpResponseException{
        return downloadBitmap(url, null, null, null, false);
    }


    /////////////////////////////// DOWNLOAD Bytes REQUEST ////////////////////////////////////////////////////////

    public byte[] downloadBytes(String url, String saveFilePath, DownloadProgressListener listener, boolean isDownloadFromBreakPoint) throws HdHttpRequestException, HdHttpResponseException{
        isCancel = false;
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        byte[] out = null;
        try{
            long breakpoint = 0;
            if(isDownloadFromBreakPoint && saveFilePath != null){
                RandomAccessFile destfile = new RandomAccessFile (saveFilePath, "rw");
                breakpoint = destfile.length();
            }

            HdHttpResponse res = get(url, breakpoint);
            inputStream = res.asStream();
            if (inputStream != null) {
                if(breakpoint > 0) {
                    RandomAccessFile destfile = new RandomAccessFile (saveFilePath, "rw");
                    destfile.seek(breakpoint);

                    outputStream = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int read = -1;
                    int count = 0;
                    while ((read = inputStream.read(buf)) != -1) {
                        if(isCancel){
                            listener.onProgress(count+breakpoint, res.getContentLength()+breakpoint);
                            break;
                        }
                        outputStream.write(buf, 0, read);
                        count += read;
                        if(listener!=null){
                            listener.onProgress(count+breakpoint, res.getContentLength()+breakpoint);
                        }
                    }

                    byte[] bytes = outputStream.toByteArray();
                    destfile.write(bytes);
                    out = bytes;
                } else {
                    outputStream = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int read = -1;
                    int count = 0;
                    while ((read = inputStream.read(buf)) != -1) {
                        if(isCancel){
                            listener.onProgress(count, res.getContentLength());
                            break;
                        }
                        outputStream.write(buf, 0, read);
                        count += read;
                        if(listener!=null){
                            listener.onProgress(count, res.getContentLength());
                        }
                    }
                    out = outputStream.toByteArray();
                    if(saveFilePath != null){
                        RandomAccessFile destfile = new RandomAccessFile (saveFilePath, "rw");
                        destfile.write(out);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return out;
    }

    public byte[] downloadBytes(String url, DownloadProgressListener listener) throws HdHttpRequestException, HdHttpResponseException{
        return downloadBytes(url, null, listener, false);
    }

    public byte[] downloadBytes(String url) throws HdHttpRequestException, HdHttpResponseException{
        return downloadBytes(url, null, null, false);
    }




    /////////////////////////////// DOWNLOAD File REQUEST ////////////////////////////////////////////////////////

    public void download(String url, String saveFilePath, DownloadProgressListener listener, boolean isDownloadFromBreakPoint)  throws HdHttpRequestException, HdHttpResponseException{
        isCancel = false;
        if(saveFilePath == null){
            return;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try{
            long breakpoint = 0;
            if(isDownloadFromBreakPoint){
                RandomAccessFile destfile = new RandomAccessFile (saveFilePath, "rw");
                breakpoint = destfile.length();
            }
            HdHttpResponse res = get(url, breakpoint);
            inputStream = res.asStream();
            if(inputStream != null){
                if(breakpoint > 0) {
                    RandomAccessFile destfile = new RandomAccessFile (saveFilePath, "rw");
                    destfile.seek(breakpoint);

                    outputStream = new ByteArrayOutputStream();
                    byte[] buf = new byte[1024];
                    int read = -1;
                    int count = 0;
                    while ((read = inputStream.read(buf)) != -1) {
                        if(isCancel){
                            listener.onProgress(count+breakpoint, res.getContentLength()+breakpoint);
                            break;
                        }
                        outputStream.write(buf, 0, read);
                        count += read;
                        if(listener!=null){
                            listener.onProgress(count+breakpoint, res.getContentLength()+breakpoint);
                        }
                    }

                    byte[] bytes = ((ByteArrayOutputStream)outputStream).toByteArray();
                    destfile.write(bytes);

                } else {
                    outputStream = new FileOutputStream(new File(saveFilePath));
                    byte[] b = new byte[1024];
                    int readedLength = -1;
                    int count = 0;
                    while( (readedLength = inputStream.read(b)) != -1){
                        if(isCancel){
                            listener.onProgress(count, res.getContentLength());
                            break;
                        }
                        outputStream.write(b, 0, readedLength);
                        count += readedLength;
                        if(listener != null){
                            listener.onProgress(count, res.getContentLength());
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void download(String url, String saveFilePath, boolean isDownloadFromBreakPoint) throws HdHttpRequestException, HdHttpResponseException{
        download(url, saveFilePath, null, isDownloadFromBreakPoint);
    }

    public void download(String url, String saveFilePath, DownloadProgressListener listener) throws HdHttpRequestException, HdHttpResponseException{
        download(url, saveFilePath, listener, false);
    }

    public void download(String url, String saveFilePath) throws HdHttpRequestException, HdHttpResponseException{
        download(url, saveFilePath, null, false);
    }

	/**
	 * Execute the DefaultHttpClient
	 *
	 * @param url target
	 * @param postParams
	 * @param file can be NULL
	 * @param httpMethod
	 *            HttpPost.METHOD_NAME HttpGet.METHOD_NAME
	 *            HttpDelete.METHOD_NAME
	 * @return HdHttpResponse from server
	 * @throws HdHttpRequestException
	 *             此异常包装了一系列底层异常 <br />  <br />
	 *             1. 底层异常, 可使用getCause()查看: <br />
	 *             <li>URISyntaxException, 由`new URI` 引发的.</li>
	 *             <li>IOException, 由`createMultipartEntity` 或 `UrlEncodedFormEntity` 引发的. </li>
	 *             <li>IOException和ClientProtocolException, 由`HttpClient.execute` 引发的.</li>
	 *             <br />
	 *             2. 当响应码不为200时报出的各种子类异常:
	 *             <li>HdHttpRequestException,
	 *             通常发生在请求的错误,如请求错误了 网址导致404等, 抛出此异常, 首先检查request log,
	 *             确认不是人为错误导致请求失败</li>
	 *             <li>HttpAuthException, 通常发生在Auth失败,
	 *             检查用于验证登录的用户名/密码/KEY等</li>
	 *             <li>HttpRefusedException, 通常发生在服务器接受到请求, 但拒绝请求, 可是多种原因, 具体原因 服务器会返回拒绝理由,
	 *             调用HttpRefusedException#getError#getMessage查看</li>
	 *             <li>HttpServerException, 通常发生在服务器发生错误时, 检查服务器端是否在正常提供服务</li>
	 *             <li>HdHttpRequestException, 其他未知错误.</li>
	 */
	public HdHttpResponse httpRequest(String url,
			List<BasicNameValuePair> postParams, UploadFile file, String httpMethod, String encode, long breakpoint) throws HdHttpRequestException {
		URI uri = createURI(url);

		org.apache.http.HttpResponse response = null;
		HdHttpResponse res = null;
		HttpUriRequest method = null;

		// Create POST, GET or DELETE METHOD
		method = createMethod(httpMethod, uri, file, postParams, encode, breakpoint);
		wrapperHttpMethod(method);
		// Execute Request
		try {
			response = mClient.execute(method);
			res = new HdHttpResponse(response);
		} catch (ClientProtocolException e) {
			throw new HdHttpRequestException(e.getMessage(), e, INTERNAL_ERROR);
		} catch (IOException ioe) {
			throw new HdHttpRequestException(ioe.getMessage(), ioe, INTERNAL_ERROR);
		} catch (Exception e) {
			throw new HdHttpRequestException(e.getMessage(), e, INTERNAL_ERROR);
		}
		if (response != null) {
			int statusCode = response.getStatusLine().getStatusCode();
			// It will throw a HdHttpRequestException while status code is not 200 and 206
			handleResponseStatusCode(statusCode, res);
		}
		return res;
	}

    public HdHttpResponse httpRequest(String url,
                                      List<BasicNameValuePair> postParams,String httpMethod, String encode, long breakpoint) throws HdHttpRequestException {
        return httpRequest(url, postParams, null, httpMethod, encode, breakpoint);
    }

    public HdHttpResponse httpRequest(String url,
                                List<BasicNameValuePair> postParams,String httpMethod, String encode) throws HdHttpRequestException {
        return httpRequest(url, postParams, null, httpMethod, encode, 0);
    }

	public void close(){
		if(mClient != null){
			mClient.getConnectionManager().shutdown();
		}
	}

    public void cancel(){
        this.isCancel = true;
    }

	private URI createURI(String url) throws HdHttpRequestException {
		URI uri;

		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new HdHttpRequestException("Invalid URL.");
		}

		return uri;
	}

	/**
	 * 创建可带一个File的MultipartEntity
	 *
	 * @param filename 文件名
	 * @param file 文件
	 * @param postParams 其他POST参数
	 * @return 带文件和其他参数的Entity
	 * @throws java.io.UnsupportedEncodingException
	 */
	private MultipartEntity createMultipartEntity(String filename, File file, CustomMultipartEntity.ProgressListener l,
			List<BasicNameValuePair> postParams) throws UnsupportedEncodingException {

		CustomMultipartEntity entity = new CustomMultipartEntity(l);
		// Don't try this. Server does not appear to support chunking.
		// entity.addPart("media", new InputStreamBody(imageStream, "media"));
		entity.addPart(filename, new FileBody(file));
        if(postParams != null){
            for (BasicNameValuePair param : postParams) {
                entity.addPart(param.getName(), new StringBody(param.getValue()));
            }
        }

		return entity;
	}

    private MultipartEntity createMultipartEntity(String filename, byte[] bytes, CustomMultipartEntity.ProgressListener l,
                                                  List<BasicNameValuePair> postParams) throws UnsupportedEncodingException {

        CustomMultipartEntity entity = new CustomMultipartEntity(l);
        // Don't try this. Server does not appear to support chunking.
        // entity.addPart("media", new InputStreamBody(imageStream, "media"));
        entity.addPart(filename, new ByteArrayBody(bytes, filename));
        if(postParams != null){
            for (BasicNameValuePair param : postParams) {
                entity.addPart(param.getName(), new StringBody(param.getValue()));
            }
        }

        return entity;
    }

	/**
	 * Create request method, such as POST, GET, DELETE
	 *
	 * @param httpMethod
	 *            "GET","POST","DELETE"
	 * @param uri
	 *            请求的URI
	 * @param file
	 *            可为null
	 * @param postParams
	 *            POST参数
	 * @return httpMethod Request implementations for the various HTTP methods
	 *         like GET and POST.
	 * @throws HdHttpRequestException
	 *             createMultipartEntity 或 UrlEncodedFormEntity引发的IOException
	 */
	private HttpUriRequest createMethod(String httpMethod, URI uri, UploadFile file,
			List<BasicNameValuePair> postParams, String encode, long breakpoint) throws HdHttpRequestException {

		HttpUriRequest method;
		if (httpMethod.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
			// POST METHOD
			HttpPost post = new HttpPost(uri);
			// See this:
			// http://groups.google.com/group/twitter-development-talk/browse_thread/thread/e178b1d3d63d8e3b
			post.getParams().setBooleanParameter(
					"http.protocol.expect-continue", false);

			try {
				HttpEntity entity = null;
				if (null != file) {
                    if(file.uploadFile != null){
                        entity = createMultipartEntity(file.uploadName, file.uploadFile, file.uploadListener, postParams);
                    }  else if(file.uploadBytes != null){
                        entity = createMultipartEntity(file.uploadName, file.uploadBytes, file.uploadListener, postParams);
                    }

				} else if (null != postParams) {
					entity = new UrlEncodedFormEntity(postParams, encode);
				}
				post.setEntity(entity);
			} catch (IOException ioe) {
				throw new HdHttpRequestException(ioe.getMessage(), ioe);
			}

			method = post;
		} else if (httpMethod.equalsIgnoreCase(HttpDelete.METHOD_NAME)) {
			method = new HttpDelete(uri);
		} else {
			method = new HttpGet(uri);
		}

        if(breakpoint >0 ){
            method.addHeader("Range", "bytes="+breakpoint+"-");
        }
		return method;
	}

	/**
	 * Parse HTTP status code with error message
	 *
	 * @param statusCode http status code
	 * @return String error message
	 */
	private static String getCause(int statusCode) {
		String cause = null;
		switch (statusCode) {
		case NOT_MODIFIED:
			cause = "";
			break;
		case BAD_REQUEST:
			cause = "The request was invalid.";
			break;
		case NOT_AUTHORIZED:
			cause = "Authentication credentials were missing or incorrect.";
			break;
		case FORBIDDEN:
			cause = "The request is understood, but it has been refused.";
			break;
		case NOT_FOUND:
			cause = "The URI requested is invalid or the resource requested does not exists.";
			break;
		case NOT_ACCEPTABLE:
			cause = "An invalid format is specified in the request.";
			break;
        case RANGE_NOT_SATISFIED:
            cause = "Requested Range not satisfiable).";
            break;
		case INTERNAL_SERVER_ERROR:
			cause = "Something is broken.";
			break;
		case BAD_GATEWAY:
			cause = "server is down or being upgraded.";
			break;
		case SERVICE_UNAVAILABLE:
			cause = "Service Unavailable: The servers are up, but overloaded with requests. Try again later. ";
			break;
		default:
			cause = "";
		}
		return statusCode + ":" + cause;
	}

	/**
	 * Handle Status code
	 *
	 * @param statusCode 响应的状态码
	 * @param res 服务器响应
	 * @throws HdHttpRequestException 当响应码不为200时都会报出此异常
	 */
	private void handleResponseStatusCode(int statusCode, HdHttpResponse res)
			throws HdHttpRequestException {
		String msg = getCause(statusCode) + "\n";
        if(statusCode != OK && statusCode != PARTIAL){
            throw new HdHttpRequestException(msg, statusCode);
        }
	}

	public String encode(String value) throws HdHttpRequestException {
		try {
			return URLEncoder.encode(value, HdConfig.getInstance().getProperty("http.encode"));
		} catch (UnsupportedEncodingException e) {
			throw new HdHttpRequestException(e.getMessage(), e);
		}
	}

	public String buildQueryString(List<BasicNameValuePair> params, String encode)
			throws HdHttpRequestException {
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < params.size(); j++) {
			if (j != 0) {
				buf.append("&");
			}
			if(encode == null){
				buf.append(params.get(j).getName())
						.append("=")
						.append(params.get(j).getValue());
			} else {
				try {
					buf.append(URLEncoder.encode(params.get(j).getName(), encode))
							.append("=")
							.append(URLEncoder.encode(params.get(j).getValue(),
									encode));
				} catch (UnsupportedEncodingException neverHappen) {
					throw new HdHttpRequestException(neverHappen.getMessage(), neverHappen);
				}
			}
			
		}
		return buf.toString();
	}

    public String buildQueryString(List<BasicNameValuePair> params) throws HdHttpRequestException {
        return buildQueryString(params, null);
    }

    private void wrapperHttpMethod(HttpUriRequest method) {
        if(headers == null){
            return;
        }
        Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = (Entry<String, String>) iter.next();
            String key = (String)entry.getKey();
            String val = (String)entry.getValue();
            method.addHeader(key, val);
        }
    }

    public byte[] gzipRequest(byte[] bytes) throws Exception{
        ByteArrayInputStream tmpInput = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream tmpOutput = new ByteArrayOutputStream(1024);
        HdHttpUtil.compress(tmpInput, tmpOutput);
        return tmpOutput.toByteArray();
    }
	
	public static class UploadFile{
		public String uploadName;
		public File uploadFile;
        public byte[] uploadBytes;
		public CustomMultipartEntity.ProgressListener uploadListener;
		public UploadFile(String name, File file, CustomMultipartEntity.ProgressListener listener){
			uploadName = name;
			uploadFile = file;
			uploadListener = listener;
		}

        public UploadFile(String name, File file){
            uploadName = name;
            uploadFile = file;
            uploadListener = new CustomMultipartEntity.ProgressListener() {
                @Override
                public void transferred(long num) {

                }
            };
        }

        public UploadFile(String name, byte[] bytes, CustomMultipartEntity.ProgressListener listener){
            uploadName = name;
            uploadBytes = bytes;
            uploadListener = listener;
        }

        public UploadFile(String name, byte[] bytes){
            uploadName = name;
            uploadBytes = bytes;
            uploadListener = new CustomMultipartEntity.ProgressListener() {
                @Override
                public void transferred(long num) {

                }
            };
        }
	}

    public static interface DownloadProgressListener{
        public void onProgress(long downloaded, long contentLength);
    }

}
