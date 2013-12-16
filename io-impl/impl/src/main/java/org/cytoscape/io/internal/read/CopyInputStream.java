package org.cytoscape.io.internal.read;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class CopyInputStream {
	/**
	 * @param is The InputStream to be copied.
	 * @param kb The number of kilobytes to read. For example, 
	 * a value of 5 means 5120 bytes will be read.
	 */
	public static InputStream copyKBytes(InputStream is, int kb ) throws IOException {
		ByteArrayOutputStream copy = new ByteArrayOutputStream();
//		int read = 0;
		int chunk = 0;
		byte[] data = new byte[1024];
		
		while((-1 != (chunk = is.read(data))) && ( kb-- > 0 ) ) {
//			read += data.length;
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
	
	/**
	 * 
	 * @param is The InputStream to be copied.
	 * @throws IOException 
	 */
	public static InputStream copy(InputStream is) throws IOException {
		ReadableByteChannel source = Channels.newChannel(is);   
		File tmpf = File.createTempFile("cy3_istream_", ".data");
       	tmpf.deleteOnExit();
	    FileOutputStream dest = new FileOutputStream(tmpf);        
	    dest.getChannel().transferFrom(source, 0, 100 * 1024 * 1024 * 1024);
	    dest.close();
	    return new FileInputStream(tmpf);
	}
	
}
