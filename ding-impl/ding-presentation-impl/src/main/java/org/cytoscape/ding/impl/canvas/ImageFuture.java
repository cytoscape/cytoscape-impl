package org.cytoscape.ding.impl.canvas;

import java.awt.Image;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.cytoscape.ding.impl.work.ProgressMonitor;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;

public class ImageFuture {

	private final CompletableFuture<Image> future;
	private final RenderDetailFlags lastRenderDetail;
	private final ProgressMonitor progressMonitor;
	
	
	public ImageFuture(CompletableFuture<Image> future, RenderDetailFlags lastRenderDetail, ProgressMonitor progressMonitor) {
		this.future = Objects.requireNonNull(future);
		this.lastRenderDetail = Objects.requireNonNull(lastRenderDetail);
		this.progressMonitor = ProgressMonitor.notNull(progressMonitor);
	}
	
	public ImageFuture(CompletableFuture<Image> future, RenderDetailFlags lastRenderDetail) {
		this(future, lastRenderDetail, null);
	}
	
	public ImageFuture(Image image, RenderDetailFlags lastRenderDetail) {
		this(CompletableFuture.completedFuture(image), lastRenderDetail);
	}
	
	public void cancel() {
		progressMonitor.cancel();
	}
	
	public boolean isCancelled() {
		return progressMonitor.isCancelled();
	}
	
	public Image join() {
		return future.join();
	}
	
	public void thenRun(Runnable r) {
		future.thenRun(() -> {
			if(!progressMonitor.isCancelled()) {
				r.run();
			}
		});
	}
	
	public RenderDetailFlags getLastRenderDetail() {
		return lastRenderDetail;
	}
	
	public boolean isReady() {
		return !progressMonitor.isCancelled() && future.isDone();
	}
}
