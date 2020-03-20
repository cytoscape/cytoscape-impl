package org.cytoscape.ding.impl.work;

public class ConsoleProgressMonitor implements ProgressMonitor {

	private final String name;
	private boolean cancelled = false;
	private double currentProgress = 0.0;  // Maybe use DoubleAdder for real progressbar monitor
	
	public ConsoleProgressMonitor(String name) {
		this.name = name;
	}
	
	public void cancel() {
		cancelled = true;
		println("cancelled");
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void start(String taskName) {
		println("started " + taskName);
	}
	
	@Override
	public void addProgress(double progress) {
		synchronized (this) {
			currentProgress = Math.max(0.0, Math.min(1.0, currentProgress + progress));
		}
		if(currentProgress >= 1.0)
			println("done");
//		else
//			println("progress " + currentProgress);
	}
	
	@Override
	public void done() {
		synchronized (this) {
			currentProgress = 1.0;
		}
		println("donezo");
	}

	private void println(String message) {
		System.out.print(name);
		System.out.print(": ");
		System.out.println(message);
	}
	
}
