package org.cytoscape.model.internal.column;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class FastUtilColumnDataFactory implements ColumnDataFactory {

	private CanonicalStringPool stringPool;
	
	private synchronized CanonicalStringPool getStringPool() {
		if(stringPool == null) {
			stringPool = new CanonicalStringPool();
		}
		return stringPool;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ColumnData create(Class<?> primaryKeyType, Class<?> type, Class<?> listElementType, int defaultInitSize) {
		// Maps that store Object references can hold Equation objects directly.
		// Primitive maps require a wrapper to hold the Equations.
		if(Long.class.equals(primaryKeyType)) {
			if(Integer.class.equals(type)) {
				return new EquationSupport(new MapColumn((Map)new Long2IntOpenHashMap()));
			} else if(Long.class.equals(type)) {
				return new EquationSupport(new MapColumn((Map)new Long2LongOpenHashMap()));
			} else if(Double.class.equals(type)) {
				return new EquationSupport(new MapColumn((Map)new Long2DoubleOpenHashMap()));
			} else if(String.class.equals(type)) {
				return new StringSupport(getStringPool(), new MapColumn((Map)new Long2ObjectOpenHashMap()));
			} else if(Boolean.class.equals(type)) {
				return new EquationSupport(new LongToBooleanColumn());
			}
		}
		
		return new MapColumn(new HashMap<>(defaultInitSize));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<?> createList(Class<?> elementType, List<?> data) {
		if(Integer.class.equals(elementType)) {
			return new IntArrayList((List<Integer>)data);
		} else if(Long.class.equals(elementType)) {
			return new LongArrayList((List<Long>)data);
		} else if(Double.class.equals(elementType)) {
			return new DoubleArrayList((List<Double>)data);
		} else if(Boolean.class.equals(elementType)) {
			return new BooleanArrayList((List<Boolean>)data);
		} else if(String.class.equals(elementType)) {
			List<Object> canonData = new ArrayList<>(data.size());
			for(Object value : data) {
				if(value instanceof String) {
					CanonicalStringPool stringPool = getStringPool();
					value = stringPool.canonicalize((String)value);
				}
				canonData.add(value);
			}
			return canonData;
		}
		
		return new ArrayList<>(data);
	}
	
}
