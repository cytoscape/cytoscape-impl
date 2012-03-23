
package org.cytoscape.model.internal;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class IntTHashTest {

	@Test
	public void testBasicPutGet() {
		IntTHash<String> ith = new IntTHash<String>(String.class);
		ith.put(1,"homer");
		ith.put(2,"marge");
		ith.put(3,"lisa");
		assertEquals( "homer", ith.get(1) );
		assertEquals( "marge", ith.get(2) );
		assertEquals( "lisa", ith.get(3) );
	}

	@Test
	public void testSize() {
		IntTHash<String> ith = new IntTHash<String>(String.class);
		ith.put(1,"homer");
		ith.put(2,"marge");
		ith.put(3,"lisa");
		assertEquals( 3, ith.size() );
	}

	@Test
	public void testRemove() {
		IntTHash<String> ith = new IntTHash<String>(String.class);
		ith.put(1,"homer");
		ith.put(2,"marge");
		ith.put(3,"lisa");

		assertEquals( 3, ith.size() );

		ith.remove(3);
		assertEquals( 2, ith.size() );
		assertNull( ith.get(3) );

		ith.remove(2);
		assertEquals( 1, ith.size() );
		assertNull( ith.get(2) );

		ith.remove(1);
		assertEquals( 0, ith.size() );
		assertNull( ith.get(1) );
	}

	@Test
	public void testAddRemove() {
		IntTHash<String> ith = new IntTHash<String>(String.class);
		ith.put(1,"homer");
		ith.put(2,"marge");
		ith.put(3,"lisa");
		ith.put(4,"bart");
		ith.put(5,"maggie");
		ith.put(6,"smithers");
		ith.put(7,"burns");

		ith.remove(1);
		ith.remove(2);
		ith.remove(3);

		ith.put(1,"homer");
		ith.put(2,"marge");
		ith.put(3,"lisa");

		ith.remove(4);
		ith.remove(5);
		ith.remove(6);

		ith.put(4,"bart");
		ith.put(5,"maggie");
		ith.put(6,"smithers");
	}
	
}
