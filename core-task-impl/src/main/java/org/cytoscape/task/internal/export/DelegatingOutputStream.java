package org.cytoscape.task.internal.export;

import java.io.IOException;
import java.io.OutputStream;

public class DelegatingOutputStream extends OutputStream {
	
	private OutputStream delegate = nullOutputStream();

	public void setDelegate(OutputStream o) {
		this.delegate = o == null ? nullOutputStream() : o;
	}
	
	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		delegate.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
	}
	
	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
