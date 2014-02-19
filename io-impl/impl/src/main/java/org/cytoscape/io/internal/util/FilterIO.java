package org.cytoscape.io.internal.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.filter.model.Transformer;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterIO {
	public static final String ID_FIELD = "id";
	public static final String PARAMETERS_FIELD = "parameters";
	public static final String NAME_FIELD = "name";
	public static final String TRANSFORMERS_FIELD = "transformers";

	static final Logger logger = LoggerFactory.getLogger(FilterIO.class);
	
	public static Map<String, Object> getParameters(Transformer<?, ?> transformer) throws IntrospectionException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		
		Class<?> type = transformer.getClass();
		BeanInfo info = Introspector.getBeanInfo(type);
		for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
			Method method = descriptor.getReadMethod();
			if (method.isAnnotationPresent(Tunable.class)) {
				try {
					map.put(descriptor.getName(), method.invoke(transformer));
				} catch (IllegalArgumentException e) {
					logger.error("Unexpected error", e);
				} catch (IllegalAccessException e) {
					logger.error("Unexpected error", e);
				} catch (InvocationTargetException e) {
					logger.error("Unexpected error", e);
				}
			}
		}
		
		for (Field field : type.getFields()) {
			if (field.isAnnotationPresent(Tunable.class)) {
				try {
					map.put(field.getName(), field.get(transformer));
				} catch (IllegalArgumentException e) {
					logger.error("Unexpected error", e);
				} catch (IllegalAccessException e) {
					logger.error("Unexpected error", e);
				}
			}
		}
		return map;
	}
	
	public static void applyParameters(Map<String, Object> parameters, Transformer<?, ?> transformer) {
		Map<String, PropertyInfo> properties = getProperties(transformer);
		for (Entry<String, Object> entry : parameters.entrySet()) {
			applyParameter(entry.getKey(), entry.getValue(), transformer, properties);
		}
	}
	
	private static Map<String, PropertyInfo> getProperties(Transformer<?, ?> transformer) {
		Map<String, PropertyInfo> properties = new HashMap<String, PropertyInfo>();
		Class<?> type = transformer.getClass();
		try {
			BeanInfo info = Introspector.getBeanInfo(type);
			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				Method getter = descriptor.getReadMethod();
				Method setter = descriptor.getWriteMethod();
				if (getter == null || !getter.isAnnotationPresent(Tunable.class) || setter == null) {
					continue;
				}
				properties.put(descriptor.getName(), new PropertyInfo(descriptor.getPropertyType(), getter, setter, null));
			}
		} catch (IntrospectionException e) {
			logger.error("Unexpected error", e);
		}
		
		for (Field field : type.getFields()) {
			if (!field.isAnnotationPresent(Tunable.class)) {
				continue;
			}
			properties.put(field.getName(), new PropertyInfo(field.getType(), null, null, field));
		}
		return properties;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void applyParameter(String name, Object value, Transformer<?, ?> transformer, Map<String, PropertyInfo> properties) {
		PropertyInfo info = properties.get(name);
		if (info == null) {
			return;
		}
		
		if (value == null) {
			info.set(transformer, value);
			return;
		}
		
		Class<?> targetType = info.type;
		Class<?> sourceType = value.getClass();
		
		if (targetType == ListSingleSelection.class) {
			ListSingleSelection list = (ListSingleSelection) info.get(transformer);
			if (list != null) {
				list.setSelectedValue(value);
			}
		} else if (targetType == ListMultipleSelection.class && value instanceof List) {
			ListMultipleSelection list = (ListMultipleSelection) info.get(transformer);
			if (list != null) {
				list.setSelectedValues((List) value);
			}
		} else {
			Object convertedValue = convertValue(name, value, sourceType, targetType);
			info.set(transformer, convertedValue);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object convertValue(String name, Object value, Class<?> sourceType, Class<?> targetType) {
		if (targetType.isAssignableFrom(sourceType)) {
			return value;
		}
		
		if (targetType.isEnum()) {
			Class<Enum> enumType = (Class<Enum>) targetType;
			return Enum.valueOf(enumType, value.toString());
		}
		
		// Primitives have to be handled specifically
		if (value instanceof Boolean && targetType == boolean.class) {
			return value;
		}

		if (value instanceof Number) {
			Number number = (Number) value;
			if (targetType == float.class || targetType == Float.class) {
				return number.floatValue();
			}
			if (targetType == double.class || targetType == Double.class) {
				return number.doubleValue();
			}
			if (targetType == int.class || targetType == Integer.class) {
				return number.intValue();
			}
			if (targetType == long.class || targetType == Long.class) {
				return number.longValue();
			}
			if (targetType == short.class || targetType == Short.class) {
				return number.shortValue();
			}
			if (targetType == byte.class || targetType == Byte.class) {
				return number.byteValue();
			}
		}
		
		if (value instanceof String) {
			String string = (String) value;
			if (targetType == char.class || targetType == Character.class) {
				return string.charAt(0);
			}
		}
		
		if (value instanceof List) {
			List list = (List) value;
			if (targetType.isArray()) {
				Class<?> componentType = targetType.getComponentType();
				Object array = Array.newInstance(componentType, list.size());
				for (int i = 0; i < list.size(); i++) {
					Object listValue = list.get(i);
					if (listValue != null) {
						listValue = convertValue(null, listValue, listValue.getClass(), componentType);
					}
					Array.set(array, i, listValue);
				}
				return array;
			}
		}
		
		throw new RuntimeException(String.format("Unsupported parameter type: %s. Source: %s. Target: %s", name, sourceType, targetType));
	}

	static class PropertyInfo {
		final Class<?> type;
		final Method getter;
		final Method setter;
		final Field field;
		
		public PropertyInfo(Class<?> type, Method getter, Method setter, Field field) {
			this.type = type;
			this.getter = getter;
			this.setter = setter;
			this.field = field;
		}
		
		public void set(Object target, Object value) {
			try {
				if (setter != null) {
					setter.invoke(target, value);
				} else if (field != null) {
					field.set(target, value);
				}
			} catch (IllegalArgumentException e) {
				logger.error("Unexpected error", e);
			} catch (IllegalAccessException e) {
				logger.error("Unexpected error", e);
			} catch (InvocationTargetException e) {
				logger.error("Unexpected error", e);
			}
		}
		
		public Object get(Object target) {
			try {
				if (getter != null) {
					return getter.invoke(target);
				} else if (field != null) {
					return field.get(target);
				}
			} catch (IllegalArgumentException e) {
				logger.error("Unexpected error", e);
			} catch (IllegalAccessException e) {
				logger.error("Unexpected error", e);
			} catch (InvocationTargetException e) {
				logger.error("Unexpected error", e);
			}
			return null;
		}
	}
	
	public static NamedTransformer<?, ?> createNamedTransformer(String name, Transformer<?, ?>... transformers) {
		return new NamedTransformerImpl(name, transformers);
	}
	
	public static NamedTransformer<?, ?> createNamedTransformer(String name, List<Transformer<?, ?>> transformers) {
		return new NamedTransformerImpl(name, transformers);
	}

	private static class NamedTransformerImpl implements NamedTransformer<Object, Object> {
		String name;
		private List<Transformer<Object, Object>> transformers;
		
		@SuppressWarnings("unchecked")
		public NamedTransformerImpl(String name, Transformer<?, ?>... transformers) {
			this.name = name;
			
			this.transformers = new ArrayList<Transformer<Object, Object>>(transformers.length);
			for (Transformer<?, ?> transformer : transformers) {
				this.transformers.add((Transformer<Object, Object>) transformer);
			}
		}
		
		@SuppressWarnings("unchecked")
		public NamedTransformerImpl(String name, List<Transformer<?, ?>> transformers) {
			this.name = name;
			
			this.transformers = new ArrayList<Transformer<Object, Object>>(transformers.size());
			for (Transformer<?, ?> transformer : transformers) {
				this.transformers.add((Transformer<Object, Object>) transformer);
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public List<Transformer<Object, Object>> getTransformers() {
			return transformers;
		}
	}
}
