package org.cytoscape.filter.internal.view;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A worker that ignores work requests if it is already in the middle of doing
 * work.  
 */
public abstract class LazyWorker {
	transient boolean hasNewWork;

	Lock lock;
	Condition working;
	
	public LazyWorker() {
		lock = new ReentrantLock();
		working = lock.newCondition();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					lock.lock();
					try {
						if (!hasNewWork) {
							working.awaitUninterruptibly();
						}
						hasNewWork = false;
					} finally {
						lock.unlock();
					} try {
						doWork();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	protected abstract void doWork();

	public void requestWork() {
		lock.lock();
		try {
			hasNewWork = true;
			working.signal();
		} finally {
			lock.unlock();
		}
	}
	
	public static void main(String[] args) throws Exception {
		LazyWorker updater = new LazyWorker() {
			int count;
			protected void doWork() {
				System.out.print("Updating... ");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("done. " + ++count);
			}
		};
		for (int i = 0; i < 100; i++) {
			updater.requestWork();
			Thread.sleep(10);
		}
	}
}
