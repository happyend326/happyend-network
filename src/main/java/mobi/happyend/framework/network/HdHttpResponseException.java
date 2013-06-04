package mobi.happyend.framework.network;

/**
 * 解析response时出现IOException, JSONException等
 */
public class HdHttpResponseException extends HdHttpRequestException {

	private static final long serialVersionUID = -9161304367990941666L;

	public HdHttpResponseException(Exception cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public HdHttpResponseException(String msg, Exception cause, int statusCode) {
		super(msg, cause, statusCode);
		// TODO Auto-generated constructor stub
	}

	public HdHttpResponseException(String msg, Exception cause) {
		super(msg, cause);
		// TODO Auto-generated constructor stub
	}

	public HdHttpResponseException(String msg, int statusCode) {
		super(msg, statusCode);
		// TODO Auto-generated constructor stub
	}

	public HdHttpResponseException(String msg) {
		super(msg);
		// TODO Auto-generated constructor stub
	}

}
