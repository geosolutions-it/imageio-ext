package it.geosolutions.imageio.plugins;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Utilities {

	/**
	 * Returns an URI given an inputSource which could be a <code>File</code>,
	 * a <code>File</code>, a <code>String</code>, an <code>URL</code>
	 * or something else.
	 * 
	 * TODO: Improve this.
	 * 
	 * @param source
	 * @return
	 * @throws URISyntaxException
	 */
	public static URI getURIFromSource(Object source) {
		URI uri = null;
		try {
			if (source instanceof String) {

				uri = new URI((String) source);
			} else if (source instanceof File) {
				uri = ((File) source).toURI();
			} else if (source instanceof URL) {
				uri = ((URL) source).toURI();
			} else if (source instanceof FileImageInputStreamExt) {
				uri = (((FileImageInputStreamExt)source).getFile()).toURI();
				// uri = new URI(((URL)source).toString());
			}
		} catch (URISyntaxException e) {
//			 XXX
		}
		return uri;
	}

}
