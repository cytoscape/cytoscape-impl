package org.cytoscape.ding.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * A timer that can be used to coalesce multiple quick calls to a handler in order
 * to avoid running the same task in quick succession.
 */
public class CoalesceTimer {

	public static final int DEFAULT_DELAY = 120;
	public static final int DEFAULT_THREADS = 1;
	
	private static final Object DEFAULT_KEY = new Object();
	
	private final Map<Object,Future<?>> store;
	
	private final ScheduledThreadPoolExecutor executor;
	private final int delay; // milliseconds
	
	
	public CoalesceTimer() {
		this(DEFAULT_DELAY, DEFAULT_THREADS);
	}
	
	public CoalesceTimer(int delay, int threads) {
		// Synchronize the store because the future runs on a different thread than the one calling coalesce().
		this.store = Collections.synchronizedMap(new HashMap<>());
		this.delay = delay;
		
		executor = new ScheduledThreadPoolExecutor(threads, r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName(CoalesceTimer.class.getSimpleName() + "_" + thread.getName());
			return thread;
		});
	}

	
	/**
	 * Starts a timer that will run the runnable after a short delay. If another
	 * call to this method occurs before the timer expires the timer will be 
	 * reset. The result is multiple quick calls to this method where the time between
	 * the calls is less than the delay will result in the runnable running once.
	 * <br><br>
	 * It is recommended to use a single thread if this method is preferred over
	 * {@link CoalesceTimer#coalesce(Object, Runnable)}
	 * 
	 * <pre>
	 * public void handleEvent(RowsSetEvent e) {
	 *     if(e.containsColumn(CyNetwork.SELECTED)) {
	 *         coalesceTimer.coalesce(() -> updateUI());
	 *     }
	 * }
	 * </pre>
	 */
	public synchronized void coalesce(Runnable runnable) {
		coalesce(DEFAULT_KEY, runnable);
	}
	
	/**
	 * Starts a timer that will run the runnable after a short delay. If another
	 * call to this method occurs before the timer expires the timer will be 
	 * reset. The result is multiple quick calls to this method where the time between
	 * the calls is less than the delay will result in the runnable running once.
	 * 
	 * * <pre>
	 * public void handleEvent(RowsSetEvent e) {
	 *     if(e.containsColumn(CyNetwork.SELECTED)) {
	 *         CyNetworkView networkView = applicationManager.getCurrentNetworkView();
	 *         if(networkView != null) {
	 *             coalesceTimer.coalesce(networkView, () -> updateUI(networkView));
	 *         }
	 *     }
	 * }
	 * </pre>
	 */
	public synchronized void coalesce(Object key, Runnable runnable) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(runnable);
		
		Future<?> future = store.get(key);
		if(future != null) {
			future.cancel(false);
		}
		
		Runnable r = () -> {
			store.remove(key);
			runnable.run();
		};
		
		future = executor.schedule(r, delay, TimeUnit.MILLISECONDS);
		store.put(key, future);
	}
	
	
	public void shutdown() { 
		executor.shutdown();
	}
	
	public boolean isShutdown() {
		return executor.isShutdown();
	}
	
}
