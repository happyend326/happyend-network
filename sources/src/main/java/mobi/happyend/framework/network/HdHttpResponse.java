package mobi.happyend.framework.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import mobi.happyend.framework.config.HdConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HdHttpResponse {
	private final HttpResponse mResponse;
    private long contentLength;
	

	public HdHttpResponse(HttpResponse res) {
		mResponse = res;
	}

    public long getContentLength(){
        return contentLength;
    }

	/**
	 * Convert HdHttpResponse to inputStream
	 * 
	 * @return InputStream or null
	 * @throws HdHttpResponseException
	 */
	public InputStream asStream() throws HdHttpResponseException {
		try {
			final HttpEntity entity = mResponse.getEntity();
            contentLength = entity.getContentLength();
			if (entity != null) {
				return entity.getContent();
			}
		} catch (IllegalStateException e) {
			throw new HdHttpResponseException(e.getMessage(), e);
		} catch (IOException e) {
			throw new HdHttpResponseException(e.getMessage(), e);
		}
		return null;
	}
	
	public byte[] asBytes() throws HdHttpResponseException {
		try {
			final HttpEntity entity = mResponse.getEntity();
            contentLength = entity.getContentLength();
			if (entity != null) {
				return EntityUtils.toByteArray(mResponse.getEntity());
			}
		} catch (IllegalStateException e) {
			throw new HdHttpResponseException(e.getMessage(), e);
		} catch (IOException e) {
			throw new HdHttpResponseException(e.getMessage(), e);
		}
		return null;
	}
	
	public Bitmap asImage() throws HdHttpResponseException {
		byte[] byteImg = asBytes();
		return BitmapFactory.decodeByteArray(byteImg, 0, byteImg.length);
	}


	/**
	 * Convert HdHttpResponse to Context String
	 * 
	 * @return response context string or null
	 * @throws HdHttpResponseException
	 */
	public String asString(String encode) throws HdHttpResponseException {
		try {
			return entityToString(mResponse.getEntity(), encode);
		} catch (IOException e) {
			throw new HdHttpResponseException(e.getMessage(), e);
		}
	}
	
	public String asString() throws HdHttpResponseException {
		return asString(HdConfig.getInstance().getProperty("http.encode"));
	}

	/**
	 * EntityUtils.toString(entity, "UTF-8");
	 * 
	 * @param entity
	 * @return
	 * @throws java.io.IOException
	 * @throws HdHttpResponseException
	 */
	private String entityToString(final HttpEntity entity, final String encode) throws IOException,
            HdHttpResponseException {
		if (null == entity) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
        contentLength = entity.getContentLength();
		// InputStream instream = asStream(entity);
		if (instream == null) {
			return "";
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"HTTP entity too large to be buffered in memory");
		}

		int i = (int) entity.getContentLength();
		if (i < 0) {
			i = 4096;
		}

		Reader reader = new BufferedReader(new InputStreamReader(instream,
				encode));
		CharArrayBuffer buffer = new CharArrayBuffer(i);
		try {
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} finally {
			reader.close();
		}

		return buffer.toString();
	}

	public JSONObject asJSONObject(String encode) throws HdHttpResponseException {
		try {
			return new JSONObject(asString(encode));
		} catch (JSONException jsone) {
			throw new HdHttpResponseException(jsone.getMessage() + ":" + asString(),
					jsone);
		}
	}
	
	public JSONObject asJSONObject() throws HdHttpResponseException {
		return asJSONObject(HdConfig.getInstance().getProperty("http.encode"));
	}

	public JSONArray asJSONArray(String encode) throws HdHttpResponseException {
		try {
			return new JSONArray(asString(encode));
		} catch (Exception jsone) {
			throw new HdHttpResponseException(jsone.getMessage(), jsone);
		}
	}
	
	public JSONArray asJSONArray() throws HdHttpResponseException {
		return asJSONArray(HdConfig.getInstance().getProperty("http.encode"));
	}
}
