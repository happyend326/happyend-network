package mobi.happyend.framework.network;

/**
 * HTTP StatusCode is not 200
 */
public class HdHttpResponseException extends Exception {
    private static final long serialVersionUID = 4655610556929371076L;
    private int statusCode = HdHttpClient.INTERNAL_ERROR;

    public HdHttpResponseException(String msg) {
        super(msg);
    }

    public HdHttpResponseException(Exception cause) {
        super(cause);
    }

    public HdHttpResponseException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public HdHttpResponseException(String msg, Exception cause) {
        super(msg, cause);
    }

    public HdHttpResponseException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

}

