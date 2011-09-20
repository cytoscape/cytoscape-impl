
package org.cytoscape.io.internal.read;


import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * An InputStream that copies another InputStream into a ByteArrayInputStream.
 * The purpose is to allow the InputStream to be reset so that it can be read
 * mulitple times.
 */
public class MarkSupportedInputStream extends InputStream {

	private InputStream is;

	public MarkSupportedInputStream(InputStream eis) throws IOException {
		super();
		ByteArrayOutputStream copy = new ByteArrayOutputStream();
		int read = 0;
		int chunk = 0;
		byte[] data = new byte[1024];
		
		while(-1 != (chunk = eis.read(data))) { 
			read += data.length;
			copy.write(data, 0, chunk);
		}

		is = new ByteArrayInputStream( copy.toByteArray() );
	}


	public int available() throws IOException {
		return is.available();
	}

	public void close() throws IOException {
		is.close();
	}

	public void mark(int readlimit) {
		is.mark(readlimit);
	}

	public boolean markSupported() {
		return is.markSupported();
	}

	public int read() throws IOException {
		return is.read();
	}

	public int read(byte[] b) throws IOException {
		return is.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return is.read(b,off,len);
	}

	public void reset() throws IOException {
		is.reset();
	}

	public long skip(long n) throws IOException {
		return is.skip(n);
	}
}
