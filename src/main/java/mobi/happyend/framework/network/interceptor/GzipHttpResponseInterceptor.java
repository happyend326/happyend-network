package mobi.happyend.framework.network.interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.protocol.HttpContext;

public class GzipHttpResponseInterceptor implements
		HttpResponseInterceptor {

	@Override
	public void process(HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		HttpEntity entity = response.getEntity();
		Header ceheader = entity.getContentEncoding();
		if (ceheader != null) {
			HeaderElement[] codecs = ceheader.getElements();
			for (int i = 0; i < codecs.length; i++) {
				if (codecs[i].getName().equalsIgnoreCase("gzip")) {
					response.setEntity(new GzipDecompressingEntity(response
							.getEntity()));
					return;
				}
			}
		}
	}
	
	private static class GzipDecompressingEntity extends HttpEntityWrapper {

		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException {

			// the wrapped entity's getContent() decides about repeatability
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength() {
			// length of ungzipped content is not known
			return -1;
		}

	}

}
