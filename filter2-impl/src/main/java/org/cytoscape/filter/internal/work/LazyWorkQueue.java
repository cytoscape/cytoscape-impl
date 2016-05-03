package org.cytoscape.filter.internal.work;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A worker that ignores work requests if it is already in the middle of doing
 * work.  
 */
public class LazyWorkQueue {
	transient boolean hasNewWork;

	Lock lock;
	Condition working;
	LazyWorker worker;
	
	public LazyWorkQueue() {
		lock = new ReentrantLock();
		working = lock.newCondition();
		
		new Thread(() -> {
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
                    worker.doWork();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
	}
	
	public void assignWorker(LazyWorker worker) {
		lock.lock();
		try {
			hasNewWork = true;
			this.worker = worker;
			working.signal();
		} finally {
			lock.unlock();
		}
	}
}
