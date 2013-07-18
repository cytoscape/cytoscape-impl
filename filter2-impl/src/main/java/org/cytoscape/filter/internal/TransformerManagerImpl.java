package org.cytoscape.filter.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.internal.transformers.CompositeFilterImpl;
import org.cytoscape.filter.model.CompositeFilter;
import org.cytoscape.filter.model.ElementTransformer;
import org.cytoscape.filter.model.Filter;
import org.cytoscape.filter.model.HolisticTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.filter.model.TransformerExecutionStrategy;
import org.cytoscape.filter.model.TransformerFactory;
import org.cytoscape.filter.model.TransformerSink;
import org.cytoscape.filter.model.TransformerSource;

public class TransformerManagerImpl implements TransformerManager {
	Map<Class<?>, TransformerSource<?, ?>> sources;
	Map<String, TransformerFactory<?, ?>> transformerFactories;
	
	TransformerExecutionStrategy bufferedStrategy;
	TransformerExecutionStrategy unbufferedStrategy;

	public TransformerManagerImpl() {
		int maximumThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
		bufferedStrategy = new BufferedExecutionStrategy();
		unbufferedStrategy = new UnbufferedExecutionStrategy(maximumThreads);
		
		sources = new HashMap<Class<?>, TransformerSource<?,?>>();
		transformerFactories = new HashMap<String, TransformerFactory<?,?>>();
	}
	
	@Override
	public <C, E> void execute(C context, List<Transformer<C, E>> transformers, TransformerSink<E> sink) {
		if (transformers.size() == 0) {
			return;
		}
		Class<C> contextType = transformers.get(0).getContextType();
		TransformerExecutionStrategy strategy = getOptimalStrategy(transformers);
		TransformerSource<C, E> source = getTransformerSource(contextType);
		strategy.execute(context, transformers, source, sink);
	}

	@Override
	public <C, E> void execute(C context, Transformer<C, E> transformer, TransformerSink<E> sink) {
		Class<C> contextType = transformer.getContextType();
		TransformerSource<C, E> source = getTransformerSource(contextType);
		if (transformer instanceof Filter) {
			applyFilter(context, source, (Filter<C, E>) transformer, sink);
		} else if (transformer instanceof HolisticTransformer) {
			((HolisticTransformer<C, E>) transformer).apply(context, source, sink);
		} else if (transformer instanceof ElementTransformer) {
			TransformerBuffer<C, E> sinkBuffer = createTransformerBuffer(source, context);
			applyElementTransformer(context, source, (ElementTransformer<C, E>) transformer, sinkBuffer);
			for (E element : sinkBuffer.getElementList(context)) {
				sink.collect(element);
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@Override
	public <C, E> TransformerExecutionStrategy getOptimalStrategy(List<Transformer<C, E>> transformers) {
		for (Transformer<C, E> transformer : transformers) {
			if (transformer instanceof ElementTransformer || transformer instanceof HolisticTransformer) {
				return bufferedStrategy;
			}
		}
		return unbufferedStrategy;
	}
	
	<C, E> void applyElementTransformer(C context, TransformerSource<C, E> source, ElementTransformer<C, E> transformer, TransformerSink<E> sink) {
		for (E element : source.getElementList(context)) {
			transformer.apply(context, element, sink);
		}
	}

	<C, E> void applyFilter(C context, TransformerSource<C, E> source, Filter<C, E> filter, TransformerSink<E> sink) {
		for (E element : source.getElementList(context)) {
			if (filter.accepts(context, element)) {
				sink.collect(element);
			}
		}
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
	
	@Override
	public <C, E> List<Transformer<C, E>> optimize(List<Transformer<C, E>> transformers) {
		// TODO: Implement me
		return transformers;
	}
	
	public void registerTransformerFactory(TransformerFactory<?, ?> factory, Map<String, String> properties) {
		transformerFactories.put(factory.getId(), factory);
	}
	
	public void unregisterTransformerFactory(TransformerFactory<?, ?> factory, Map<String, String> properties) {
		transformerFactories.remove(factory.getId());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C, E> Transformer<C, E> createTransformer(String id) {
		TransformerFactory<?, ?> factory = transformerFactories.get(id);
		if (factory == null) {
			return null;
		}
		return (Transformer<C, E>) factory.createTransformer();
	}
	
	@Override
	public <C, E> List<Transformer<C, E>> createTransformerList(String id) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	@Override
	public <C, E> void registerTransformerList(String id, Transformer<C, E>... transformers) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	@Override
	public Set<String> getRegisteredTransformerListIds() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}
	
	class BufferedExecutionStrategy implements TransformerExecutionStrategy {
		@Override
		public <C, E> void execute(C context, List<Transformer<C, E>> transformers, TransformerSource<C, E> source, TransformerSink<E> sink) {
			// Use double buffering to push elements through the transformers.
			TransformerBuffer<C, E> sourceBuffer = createTransformerBuffer(source, context);
			TransformerBuffer<C, E> sinkBuffer = createTransformerBuffer(source, context);
			
			TransformerSource<C, E> currentSource = source;
			for (Transformer<C, E> transformer : transformers) {
				if (transformer instanceof Filter) {
					applyFilter(context, currentSource, (Filter<C, E>) transformer, sinkBuffer);
				} else if (transformer instanceof ElementTransformer) {
					applyElementTransformer(context, currentSource, (ElementTransformer<C, E>) transformer, sinkBuffer);
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
		public <C, E> void execute(final C context, final List<Transformer<C, E>> transformers, TransformerSource<C, E> source, final TransformerSink<E> sink) {
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
