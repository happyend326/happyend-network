package mobi.happyend.framework.network.interceptor;

import org.apache.http.*;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

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
