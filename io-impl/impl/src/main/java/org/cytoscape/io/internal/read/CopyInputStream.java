
package org.cytoscape.io.internal.read;


import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class CopyInputStream {
	/**
	 * @param is The InputStream to be copied.
	 * @param kb The number of kilobytes to read. For example, 
	 * a value of 5 means 5120 bytes will be read.
	 */
	public static InputStream copyKBytes(InputStream is, int kb ) throws IOException {
		ByteArrayOutputStream copy = new ByteArrayOutputStream();
		int read = 0;
		int chunk = 0;
		byte[] data = new byte[1024];
		
		while((-1 != (chunk = is.read(data))) && ( kb-- > 0 ) ) {
			read += data.length;
			copy.write(data, 0, chunk);
		}
	
		if (is.markSupported()) {
			try {
				is.reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return new ByteArrayInputStream( copy.toByteArray() );
	}
}
