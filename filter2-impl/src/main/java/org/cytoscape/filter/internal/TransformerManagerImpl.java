package org.cytoscape.filter.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.filters.composite.CompositeFilterImpl;
import org.cytoscape.filter.internal.view.DiscreteProgressMonitor;
import org.cytoscape.filter.internal.view.ProgressMonitor;
import org.cytoscape.filter.internal.view.SubProgressMonitor;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.ElementTransformer;
import org.cytoscape.filter.model.ElementTransformerFactory;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.FilterFactory;
import org.cytoscape.filter.model.HolisticTransformer;
import org.cytoscape.filter.model.HolisticTransformerFactory;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.model.TransformerSource;

public class TransformerManagerImpl implements TransformerManager {
	Map<Class<?>, TransformerSource<?, ?>> sources;
	Map<String, FilterFactory<?, ?>> filterFactories;
	Map<String, ElementTransformerFactory<?, ?>> elementTransformerFactories;
	Map<String, HolisticTransformerFactory<?, ?>> holisticTransformerFactories;
	
	TransformerExecutionStrategy bufferedStrategy;
	TransformerExecutionStrategy unbufferedStrategy;

	public TransformerManagerImpl() {
		int maximumThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		bufferedStrategy = new BufferedExecutionStrategy();
		unbufferedStrategy = new UnbufferedExecutionStrategy(maximumThreads);
		
		sources = new ConcurrentHashMap<Class<?>, TransformerSource<?,?>>(16, 0.75f, 2);
		filterFactories = new ConcurrentHashMap<String, FilterFactory<?,?>>(16, 0.75f, 2);
		elementTransformerFactories = new ConcurrentHashMap<String, ElementTransformerFactory<?,?>>(16, 0.75f, 2);
		holisticTransformerFactories = new ConcurrentHashMap<String, HolisticTransformerFactory<?,?>>(16, 0.75f, 2);
	}
	
	@Override
	public <C, E> void execute(C context, TransformerSource<C, E> source, List<Transformer<C, E>> transformers, TransformerSink<E> sink) {
		execute(context, source, transformers, sink, ProgressMonitor.nullMonitor());
	}
	
	/**
	 * Not yet API.
	 * Need to make all of the methods in this class support progress monitoring.
	 */
	public <C, E> void execute(C context, TransformerSource<C, E> source, List<Transformer<C, E>> transformers, TransformerSink<E> sink, ProgressMonitor monitor) {
		TransformerExecutionStrategy strategy = getOptimalStrategy(transformers);
		strategy.execute(context, transformers, source, sink, monitor);
	}
	
	@Override
	public <C, E> void execute(C context, List<Transformer<C, E>> transformers, TransformerSink<E> sink) {
		if (transformers.size() == 0) {
			return;
		}
		Class<C> contextType = transformers.get(0).getContextType();
		TransformerSource<C, E> source = getTransformerSource(contextType);
		execute(context, source, transformers, sink);
	}

	@Override
	public <C, E> void execute(C context, Transformer<C, E> transformer, TransformerSink<E> sink) {
		Class<C> contextType = transformer.getContextType();
		TransformerSource<C, E> source = getTransformerSource(contextType);
		if (transformer instanceof Filter) {
			applyFilter(context, source, (Filter<C, E>) transformer, sink, ProgressMonitor.nullMonitor());
		} else if (transformer instanceof HolisticTransformer) {
			((HolisticTransformer<C, E>) transformer).apply(context, source, sink);
		} else if (transformer instanceof ElementTransformer) {
			TransformerBuffer<C, E> sinkBuffer = createTransformerBuffer(source, context);
			applyElementTransformer(context, source, (ElementTransformer<C, E>) transformer, sinkBuffer, ProgressMonitor.nullMonitor());
			for (E element : sinkBuffer.getElementList(context)) {
				sink.collect(element);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public <C, E> TransformerExecutionStrategy getOptimalStrategy(List<Transformer<C, E>> transformers) {
		for (Transformer<C, E> transformer : transformers) {
			if (transformer instanceof ElementTransformer || transformer instanceof HolisticTransformer) {
				return bufferedStrategy;
			}
		}
		return unbufferedStrategy;
	}
	
	<C, E> void applyElementTransformer(C context, TransformerSource<C, E> source, ElementTransformer<C, E> transformer, TransformerSink<E> sink, ProgressMonitor monitor) {
		List<E> elements = source.getElementList(context);
		DiscreteProgressMonitor dpm = new DiscreteProgressMonitor(monitor);
		dpm.setTotalWork(elements.size());
		for (E element : elements) {
			if(dpm.isCancelled()) {
				return;
			}
			transformer.apply(context, element, sink);
			dpm.addWork(1);
		}
		dpm.done();
	}

	<C, E> void applyFilter(C context, TransformerSource<C, E> source, Filter<C, E> filter, TransformerSink<E> sink, ProgressMonitor monitor) {
		List<E> elements = source.getElementList(context);
		DiscreteProgressMonitor dpm = new DiscreteProgressMonitor(monitor);
		dpm.setTotalWork(elements.size());
		for (E element : source.getElementList(context)) {
			if(dpm.isCancelled()) {
				return;
			}
			if (filter.accepts(context, element)) {
				sink.collect(element);
			}
			dpm.addWork(1);
		}
		dpm.done();
	}
	
	<C, E> TransformerBuffer<C, E> createTransformerBuffer(TransformerSource<C, E> source, C context) {
		Class<C> contextType = source.getContextType();
		return new DefaultBuffer<C, E>(contextType, source.getElementType(), source.getElementCount(context), 4);
	}
	
	public void registerTransformerSource(TransformerSource<?, ?> source, Map<String, String> properties) {
		sources.put(source.getContextType(), source);
	}
	
	public void unregisterTransformerSource(TransformerSource<?, ?> source, Map<String, String> properties) {
		sources.remove(source.getContextType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C, E> TransformerSource<C, E> getTransformerSource(Class<C> contextType) {
		TransformerSource<C, E> source = (TransformerSource<C, E>) sources.get(contextType);
		if (source == null) {
			throw new IllegalArgumentException("No TransformerSource registered for " + contextType);
		}
		return source;
	}
	
	@Override
	public <C, E> CompositeFilter<C, E> createCompositeFilter(Class<C> contextType, Class<E> elementType) {
		return new CompositeFilterImpl<C, E>(contextType, elementType);
	}
	
	public void registerFilterFactory(FilterFactory<?, ?> factory, Map<String, String> properties) {
		filterFactories.put(factory.getId(), factory);
	}
	
	public void unregisterFilterFactory(FilterFactory<?, ?> factory, Map<String, String> properties) {
		filterFactories.remove(factory.getId());
	}

	public void registerElementTransformerFactory(ElementTransformerFactory<?, ?> factory, Map<String, String> properties) {
		elementTransformerFactories.put(factory.getId(), factory);
	}
	
	public void unregisterElementTransformerFactory(ElementTransformerFactory<?, ?> factory, Map<String, String> properties) {
		elementTransformerFactories.remove(factory.getId());
	}

	public void registerHolisticTransformerFactory(HolisticTransformerFactory<?, ?> factory, Map<String, String> properties) {
		holisticTransformerFactories.put(factory.getId(), factory);
	}
	
	public void unregisterHolisticTransformerFactory(HolisticTransformerFactory<?, ?> factory, Map<String, String> properties) {
		holisticTransformerFactories.remove(factory.getId());
	}

	@Override
	public <C, E> Transformer<C, E> createTransformer(String id) {
		Transformer<C, E> transformer = createFilter(id);
		if (transformer != null) {
			return transformer;
		}
		transformer = createElementTransformer(id);
		if (transformer != null) {
			return transformer;
		}
		return createHolisticTransformer(id);
	}
	
	@SuppressWarnings("unchecked")
	<C, E> Filter<C, E> createFilter(String id) {
		FilterFactory<?, ?> factory = filterFactories.get(id);
		if (factory == null) {
			return null;
		}
		return (Filter<C, E>) factory.createFilter();
	}

	@SuppressWarnings("unchecked")
	<C, E> ElementTransformer<C, E> createElementTransformer(String id) {
		ElementTransformerFactory<?, ?> factory = elementTransformerFactories.get(id);
		if (factory == null) {
			return null;
		}
		return (ElementTransformer<C, E>) factory.createElementTransformer();
	}

	@SuppressWarnings("unchecked")
	<C, E> HolisticTransformer<C, E> createHolisticTransformer(String id) {
		HolisticTransformerFactory<?, ?> factory = holisticTransformerFactories.get(id);
		if (factory == null) {
			return null;
		}
		return (HolisticTransformer<C, E>) factory.createHolisticTransformer();
	}

	class BufferedExecutionStrategy implements TransformerExecutionStrategy {
		@Override
		public <C, E> void execute(C context, List<Transformer<C, E>> transformers, TransformerSource<C, E> source, TransformerSink<E> sink, ProgressMonitor monitor) {
			// Use double buffering to push elements through the transformers.
			TransformerBuffer<C, E> sourceBuffer = createTransformerBuffer(source, context);
			TransformerBuffer<C, E> sinkBuffer = createTransformerBuffer(source, context);
			
			TransformerSource<C, E> currentSource = source;
			int n = transformers.size();
			double stepSize = 1.0/(double)n;
			
			for(int i = 0; i < n; i++) {
				if(monitor.isCancelled())
					return;
				
				double stepStart = i * stepSize;
				double stepEnd = stepStart + stepSize;
				ProgressMonitor subMonitor = new SubProgressMonitor(monitor, stepStart, stepEnd);
				subMonitor.start();
				
				Transformer<C, E> transformer = transformers.get(i);
				if (transformer instanceof Filter) {
					applyFilter(context, currentSource, (Filter<C, E>) transformer, sinkBuffer, subMonitor);
				} else if (transformer instanceof ElementTransformer) {
					applyElementTransformer(context, currentSource, (ElementTransformer<C, E>) transformer, sinkBuffer, subMonitor);
				} else if (transformer instanceof HolisticTransformer) {
					((HolisticTransformer<C, E>) transformer).apply(context, currentSource, sinkBuffer);
				} else {
					throw new UnsupportedOperationException();
				}
				
				// Swap buffers
				TransformerBuffer<C, E> tempBuffer = sinkBuffer;
				sinkBuffer = sourceBuffer;
				sourceBuffer = tempBuffer;
				
				sinkBuffer.clear();
				currentSource = sourceBuffer;
			}
			
			// Push the buffered elements into the sink.
			for (E element : sourceBuffer.getElementList(context)) {
				sink.collect(element);
			}
		}
	}

	class UnbufferedExecutionStrategy implements TransformerExecutionStrategy {
		private static final int PARALLEL_THRESHOLD = 100000;
		
		int maximumThreads;
		BlockingQueue<Runnable> workQueue;
		ThreadPoolExecutor executor;
		
		public UnbufferedExecutionStrategy(int maximumThreads) {
			this.maximumThreads = maximumThreads;
			workQueue = new ArrayBlockingQueue<Runnable>(maximumThreads);
			executor = new ThreadPoolExecutor(maximumThreads, maximumThreads, Integer.MAX_VALUE, TimeUnit.SECONDS, workQueue, new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					return thread;
				}
			});
		}

		@Override
		public <C, E> void execute(final C context, final List<Transformer<C, E>> transformers, TransformerSource<C, E> source, final TransformerSink<E> sink, ProgressMonitor monitor) {
			// I don't think this code is actually being called anywhere.
			if (source.getElementCount(context) < PARALLEL_THRESHOLD) {
				execute(context, transformers, source.getElementList(context).iterator(), sink);
				return;
			}
			
			// This assumes all transformers are Filters only
			Iterator<E>[] iterators = partitionIterators(source.getElementList(context), maximumThreads);
			int totalWorkers = iterators.length;
			
			
			// When we're done, we want one permit available
			final Semaphore finished = new Semaphore(-totalWorkers + 1);
			
			for (int i = 0; i < totalWorkers; i++) {
				final Iterator<E> iterator = iterators[i];
				Runnable worker = new Runnable() {
					@Override
					public void run() {
						execute(context, transformers, iterator, sink);
						finished.release();
					}
				};
				executor.execute(worker);
			}
			try {
				finished.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private <C, E> void execute(C context, List<Transformer<C, E>> transformers, Iterator<E> iterator, TransformerSink<E> sink) {
			ALL_ELEMENTS: while (iterator.hasNext()) {
				E element = iterator.next();
				for (Transformer<C, E> transformer : transformers) {
					if (!(transformer instanceof Filter)) {
						throw new UnsupportedOperationException();
					}
					if (!((Filter<C, E>) transformer).accepts(context, element)) {
						continue ALL_ELEMENTS;
					}
				}
				sink.collect(element);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	<E> Iterator<E>[] partitionIterators(final List<E> elements, final int totalWorkers) {
		final int total = elements.size();
		Iterator<E>[] iterators = new Iterator[totalWorkers];
		for (int i = 0; i < totalWorkers; i++) {
			final int offset = i;
			iterators[i] = new Iterator<E>() {
				int index;
				
				@Override
				public boolean hasNext() {
					return index * totalWorkers + offset < total;
				}
				
				@Override
				public E next() {
					int elementIndex = index * totalWorkers + offset;
					if (elementIndex < total) {
						index++;
						return elements.get(elementIndex);
					}
					throw new NoSuchElementException();
				}
				
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		return iterators;
	}
}
