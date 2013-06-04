package mobi.happyend.framework.network;

/**
 * HTTP StatusCode is not 200 and 206
 */
public class HdHttpRequestException extends Exception {
	private static final long serialVersionUID = 4655610556929371076L;
	private int statusCode = HdHttpClient.INTERNAL_ERROR;

	public HdHttpRequestException(String msg) {
		super(msg);
	}

	public HdHttpRequestException(Exception cause) {
		super(cause);
	}

	public HdHttpRequestException(String msg, int statusCode) {
		super(msg);
		this.statusCode = statusCode;
	}

	public HdHttpRequestException(String msg, Exception cause) {
		super(msg, cause);
	}

	public HdHttpRequestException(String msg, Exception cause, int statusCode) {
		super(msg, cause);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return this.statusCode;
	}

}
