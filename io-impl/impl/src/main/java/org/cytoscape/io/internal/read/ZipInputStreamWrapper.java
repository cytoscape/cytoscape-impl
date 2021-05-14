package org.cytoscape.io.internal.read;

import java.io.IOException;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
import java.util.zip.ZipInputStream;

/**
 * This is a wrapper for ZipInputStream that makes it safe to pass around without it getting
 * accidentally closed. The original creator of the ZipInputStream is responsible for
 * closing the stream.
 */
public class ZipInputStreamWrapper extends InputStream {

	private final InputStream is;

	public ZipInputStreamWrapper(final ZipInputStream eis) throws IOException {
		this.is = eis;
	}

	public void close() throws IOException {
		// DO NOTHING!!!
	}

	public int available() throws IOException {
		return is.available();
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
