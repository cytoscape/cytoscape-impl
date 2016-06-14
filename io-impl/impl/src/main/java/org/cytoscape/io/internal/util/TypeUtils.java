package org.cytoscape.io.internal.util;

import java.util.List;

public class TypeUtils {
	
	// Adapted from code in BrowserTableModel
	// Note that this code assumes all Integer values of eqnType have been converted to Longs
	// TODO: Refactor to eliminate code duplication
	private TypeUtils() {}
	
	public static boolean eqnTypeIsCompatible(final Class<?> columnType, final Class<?> listElementType,
	                                          final Class<?> eqnType) {
		if (columnType == eqnType)
			return true;
		if (columnType == String.class) // Anything can be trivially converted to a string.
			return true;
		if (columnType == Integer.class && (eqnType == Long.class || eqnType == Double.class))
			return true;
		if (columnType == Double.class && eqnType == Long.class)
			return true;
		if (columnType == Boolean.class && (eqnType == Long.class || eqnType == Double.class))
			return true;

		if (columnType != List.class || !columnType.isAssignableFrom(eqnType))
			return false;

		// HACK!!!!!!  We don't know the type of the List, but we can do some type checking
		// for our own builtins.  We need to do this as a negative evaluation in case
		// an App wants to add a new List function
		if (eqnType.getSimpleName().equals("DoubleList") && listElementType != Double.class)
			return false;

		if (eqnType.getSimpleName().equals("LongList") && 
		    (listElementType != Integer.class && listElementType != Long.class))
			return false;

		if (eqnType.getSimpleName().equals("BooleanList") && listElementType != Boolean.class)
			return false;

		if (eqnType.getSimpleName().equals("StringList") && listElementType != String.class)
			return false;

		return true;
	}

	public static String getUnqualifiedName(final Class<?> type) {
		final String typeName = type.getName();
		final int lastDotPos = typeName.lastIndexOf('.');
		return lastDotPos == -1 ? typeName : typeName.substring(lastDotPos + 1);
	}

}
