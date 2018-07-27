package org.cytoscape.model.internal.column;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.cytoscape.equations.Equation;
import org.junit.Test;

public class ColumnDataTest {

	private static Equation createEquation(String name) {
		return new Equation(name, null, null, new Object[0], new int[0], null);
	}
	
	@Test
	public void testEquationSupport() {
		EquationSupport columnData = new EquationSupport(new MapColumn(new HashMap<>()));
		
		assertEquals(0, columnData.keySet().size());
		
		columnData.put(1, 100);
		columnData.put(2, 200);
		columnData.put(3, 300);
		
		assertEquals(3, columnData.keySet().size());
		assertEquals(100, columnData.get(1));
		assertEquals(200, columnData.get(2));
		assertEquals(300, columnData.get(3));
		
		columnData.remove(3);
		
		assertEquals(2, columnData.keySet().size());
		assertEquals(100, columnData.get(1));
		assertEquals(200, columnData.get(2));
		assertEquals(null, columnData.get(3));
		
		columnData.put(2, null);
		
		assertEquals(1, columnData.keySet().size());
		assertEquals(100, columnData.get(1));
		assertEquals(null, columnData.get(2));
		assertEquals(null, columnData.get(3));
		
		Equation equation1 = createEquation("TestEquation_1");
		columnData.put(10, equation1);
		
		assertEquals("TestEquation_1", ((Equation)columnData.get(10)).toString());
		
		columnData.put(10, 99);
		
		assertEquals(99, columnData.get(10));

		Equation equation2 = createEquation("TestEquation_2");
		columnData.put(10, equation2);
		
		assertEquals("TestEquation_2", ((Equation)columnData.get(10)).toString());
		
		boolean removed = columnData.remove(10);
		
		assertTrue(removed);
		assertEquals(1, columnData.keySet().size());
	}
	
	
	@Test
	public void testStringPool() {
		CanonicalStringPoolFilter columnData = new CanonicalStringPoolFilter(new CanonicalStringPool(), new MapColumn(new HashMap<>()));

		assertEquals(0, columnData.keySet().size());
		
		String a = new String("apple");
		String b = new String("banana");
		String c = new String("carrot");
		
		columnData.put(1, a);
		columnData.put(2, b);
		columnData.put(3, c);
		
		assertEquals(3, columnData.keySet().size());
		
		columnData.put(4, new String("apple"));
		
		assertSame(a, columnData.get(4));
		assertSame(a, columnData.get(1));
		
		columnData.remove(1);
		
		assertSame(a, columnData.get(4));
		assertNull(columnData.get(1));
	}
	
	
	@Test
	public void testBooleanColumn() {
		LongToBooleanColumn columnData = new LongToBooleanColumn(() -> new HashSet<>());
		
		assertEquals(0, columnData.keySet().size());
		
		columnData.put(1l, true);
		columnData.put(2l, false);
		columnData.put(3l, true);
		
		assertEquals(3, columnData.keySet().size());
		assertEquals(true, columnData.get(1l));
		assertEquals(false, columnData.get(2l));
		assertEquals(true, columnData.get(3l));
		
		assertEquals(2, columnData.countMatchingRows(true));
		assertEquals(1, columnData.countMatchingRows(false));
		
		Collection<Long> trueKeys = columnData.getMatchingKeys(true, Long.class);
		assertEquals(2, trueKeys.size());
		assertTrue(trueKeys.contains(1l));
		assertTrue(trueKeys.contains(3l));
		
		Collection<Long> falseKeys = columnData.getMatchingKeys(false, Long.class);
		assertEquals(1, falseKeys.size());
		assertTrue(falseKeys.contains(2l));
		
		boolean removed = columnData.remove(1l);
		assertNull(columnData.get(1l));
		assertTrue(removed);
		
		boolean changed = columnData.put(2l, null);
		assertNull(columnData.get(2l));
		assertTrue(changed);
		
		assertEquals(1, columnData.keySet().size());
	}
	
	
	
	
}
