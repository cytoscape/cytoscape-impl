package org.cytoscape.webservice.ncbi.rest;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class EntryProcessor<V> extends FutureTask<V> {
	
	public EntryProcessor(Callable<V> callable) {
		super(callable);
	}
}
