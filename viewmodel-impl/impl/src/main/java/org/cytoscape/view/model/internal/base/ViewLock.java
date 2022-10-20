package org.cytoscape.view.model.internal.base;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A lock object with some extra fields.
 * Find lock objects in a profiler like YourKIT by searching for the name "ViewLock".
 */
public class ViewLock implements ReadWriteLock {
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private int updateDirty = 0;
	private final ViewLock parent;
	
	public ViewLock() {
		this(null);
	}
	
	public ViewLock(ViewLock parent) {
		this.parent = parent;
	}

	public boolean isUpdateDirty() {
		return updateDirty == 0 && (parent == null || parent.isUpdateDirty());
	}

	public void enterBatch(Runnable runnable) {
		try {
			++updateDirty;
			runnable.run();
		} finally {
			--updateDirty;
		}
	}

	@Override
	public Lock readLock() {
		return lock.readLock();
	}

	@Override
	public Lock writeLock() {
		return lock.writeLock();
	}
	
	
//	public void withWriteLock(Runnable runnable) {
//		lock.writeLock().lock();
//		try {
//			runnable.run();
//		} finally {
//			lock.writeLock().unlock();
//		}
//	}
//	
//	public void tryReadLock(Runnable runnable) {
//		if(!lock.readLock().tryLock())
//			return;
//		try {
//			runnable.run();
//		} finally {
//			lock.readLock().unlock();
//		}
//	}
}
