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
	private static Pattern escaped = Pattern.compile("&#([0-9]{3,5});");
	private final org.apache.http.HttpResponse mResponse;
	private boolean mStreamConsumed = false;
	

	public HdHttpResponse(org.apache.http.HttpResponse res) {
		mResponse = res;
	}

	/**
	 * Convert HdHttpResponse to inputStream
	 * 
	 * @return InputStream or null
	 * @throws HttpResponseException
	 */
	public InputStream asStream() throws HttpResponseException {
		try {
			final HttpEntity entity = mResponse.getEntity();
			if (entity != null) {
				return entity.getContent();
			}
		} catch (IllegalStateException e) {
			throw new HttpResponseException(e.getMessage(), e);
		} catch (IOException e) {
			throw new HttpResponseException(e.getMessage(), e);
		}
		return null;
	}
	
	public byte[] asBytes() throws HttpResponseException {
		try {
			final HttpEntity entity = mResponse.getEntity();
			if (entity != null) {
				return EntityUtils.toByteArray(mResponse.getEntity());
			}
		} catch (IllegalStateException e) {
			throw new HttpResponseException(e.getMessage(), e);
		} catch (IOException e) {
			throw new HttpResponseException(e.getMessage(), e);
		}
		return null;
	}
	
	public Bitmap asImage() throws HttpResponseException {
		byte[] byteImg = asBytes();
		return BitmapFactory.decodeByteArray(byteImg, 0, byteImg.length);
	}


	/**
	 * Convert HdHttpResponse to Context String
	 * 
	 * @return response context string or null
	 * @throws HttpResponseException
	 */
	public String asString(String encode) throws HttpResponseException {
		try {
			return entityToString(mResponse.getEntity(), encode);
		} catch (IOException e) {
			throw new HttpResponseException(e.getMessage(), e);
		}
	}
	
	public String asString() throws HttpResponseException {
		return asString(HdConfig.getInstance().getProperty("http.encode"));
	}

	/**
	 * EntityUtils.toString(entity, "UTF-8");
	 * 
	 * @param entity
	 * @return
	 * @throws java.io.IOException
	 * @throws HttpResponseException
	 */
	private String entityToString(final HttpEntity entity, final String encode) throws IOException,
            HttpResponseException {
		if (null == entity) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
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

	public JSONObject asJSONObject(String encode) throws HttpResponseException {
		try {
			return new JSONObject(asString(encode));
		} catch (JSONException jsone) {
			throw new HttpResponseException(jsone.getMessage() + ":" + asString(),
					jsone);
		}
	}
	
	public JSONObject asJSONObject() throws HttpResponseException {
		return asJSONObject(HdConfig.getInstance().getProperty("http.encode"));
	}

	public JSONArray asJSONArray(String encode) throws HttpResponseException {
		try {
			return new JSONArray(asString(encode));
		} catch (Exception jsone) {
			throw new HttpResponseException(jsone.getMessage(), jsone);
		}
	}
	
	public JSONArray asJSONArray() throws HttpResponseException {
		return asJSONArray(HdConfig.getInstance().getProperty("http.encode"));
	}

	public boolean isStreamConsumed() {
		return mStreamConsumed;
	}

	/**
	 * Unescape UTF-8 escaped characters to string.
	 * 
	 * @param original
	 *            The string to be unescaped.
	 * @return The unescaped string
	 */
	public static String unescape(String original) {
		Matcher mm = escaped.matcher(original);
		StringBuffer unescaped = new StringBuffer();
		while (mm.find()) {
			mm.appendReplacement(unescaped, Character.toString((char) Integer
					.parseInt(mm.group(1), 10)));
		}
		mm.appendTail(unescaped);
		return unescaped.toString();
	}

}
