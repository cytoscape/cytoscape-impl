package org.cytoscape.ding.impl;

import java.awt.Image;
import java.util.concurrent.CompletableFuture;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class ImageFuture {

	private final CompletableFuture<Image> future;
	private final ProgressMonitor progressMonitor;
	private final RenderDetailFlags lastRenderDetail;
	
	public ImageFuture(ProgressMonitor progressMonitor, CompletableFuture<Image> future, RenderDetailFlags lastRenderDetail) {
		this.future = future;
		this.progressMonitor = progressMonitor;
		this.lastRenderDetail = lastRenderDetail;
	}
	
	public void cancel() {
		progressMonitor.cancel();
	}
	
	public Image join() {
		return future.join();
	}
	
	public void thenRun(Runnable r) {
		future.thenRun(r);
	}
	
	public RenderDetailFlags getLastRenderDetail() {
		return lastRenderDetail;
	}
}
