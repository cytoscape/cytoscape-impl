package org.cytoscape.io.internal.write.json;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Provide UTF-8 encoder/decoder if available.
 *
 */
final class EncodingUtil {

	private static final String CHARSET = "UTF-8";

	public static final CharsetEncoder getEncoder() {
		final CharsetEncoder encoder;

		if (Charset.isSupported(CHARSET)) {
			// UTF-8 is supported by system
			encoder = Charset.forName(CHARSET).newEncoder();
		} else {
			// Use default.
			encoder = Charset.defaultCharset().newEncoder();
		}
		
		return encoder;
	}
	
	public static final CharsetDecoder getDecoder() {
		final CharsetDecoder decoder;

		if (Charset.isSupported(CHARSET))
			decoder = Charset.forName(CHARSET).newDecoder();
		else
			decoder = Charset.defaultCharset().newDecoder();
		
		return decoder;
	}
}
