package org.cytoscape.biopax.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class BioPaxFilterUtils {
	
	private BioPaxFilterUtils() {
	}

//TODO not all input streams work: IllegalArgumentException at sun.nio.ch.FileChannelImpl.transferFrom(FileChannelImpl.java:661)
//	public static File copyToTempFile(InputStream is) throws IOException { 
//		ReadableByteChannel source = Channels.newChannel(is); 
//		File tmpf = File.createTempFile("cy3_istream_", ".data"); 
//		tmpf.deleteOnExit(); 
//		FileOutputStream dest = new FileOutputStream(tmpf); 
//		dest.getChannel().transferFrom(source, 0, 10 * 1024 * 1024 * 1024); //10GB
//		dest.close(); 
//		return tmpf; 
//	}
	
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
