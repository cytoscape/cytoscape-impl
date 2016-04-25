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

	public MarkSupportedInputStream(final InputStream eis) throws IOException {
		super();
		ByteArrayOutputStream copy = new ByteArrayOutputStream();
		int read = 0;
		int chunk;
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
