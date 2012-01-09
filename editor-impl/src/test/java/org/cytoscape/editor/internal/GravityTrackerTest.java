

/*
 Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.editor.internal;


import org.junit.Test;
import static org.junit.Assert.*;


public class GravityTrackerTest { 

	@Test
	public void testEmpty() {
		GravityTracker<String> gt = new GravityTracker<String>();
		int index = gt.add("homer",1.0);
		assertEquals(0,index);
		assertEquals(0,gt.getIndex("homer"));
	}

	@Test
	public void testNonExistent() {
		GravityTracker<String> gt = new GravityTracker<String>();
		assertEquals(0,gt.getIndex("mr. burns"));
	}

	@Test
	public void testNonExistent2() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("homer",1.0);
		gt.add("marge",2.0);
		assertEquals(2,gt.getIndex("mr. burns"));
	}

	@Test
	public void testEnd() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("homer",1.0);
		gt.add("marge",2.0);
		gt.add("bart",3.0);
		gt.add("lisa",4.0);
		int index = gt.add("maggie",5.0);

		assertEquals(4,index);
		assertEquals(4,gt.getIndex("maggie"));
	}

	@Test
	public void testBegin() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("marge",2.0);
		gt.add("bart",3.0);
		gt.add("lisa",4.0);
		gt.add("maggie",5.0);

		int index = gt.add("homer",1.0);

		assertEquals(0,index);
		assertEquals(0,gt.getIndex("homer"));
	}

	@Test
	public void testMiddle() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("homer",1.0);
		gt.add("marge",2.0);
		gt.add("lisa",4.0);
		gt.add("maggie",5.0);

		int index = gt.add("bart",3.0);

		assertEquals(2,index);
		assertEquals(2,gt.getIndex("bart"));
	}

	@Test
	public void testEqual() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("homer",1.0);
		gt.add("marge",2.0);
		gt.add("bart",3.0);
		gt.add("lisa",4.0);
		gt.add("maggie",5.0);

		int index = gt.add("smithers",3.0);

		assertEquals(3,index);
		assertEquals(3,gt.getIndex("smithers"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEqual2() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("homer",1.0);
		gt.add("marge",2.0);
		gt.add("bart",3.0);
		gt.add("lisa",4.0);
		gt.add("maggie",5.0);

		gt.add("bart",15.0);
	}

	@Test
	public void testRemove() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add("homer",1.0);
		gt.add("marge",2.0);
		gt.add("bart",3.0);
		gt.add("lisa",4.0);
		gt.add("maggie",5.0);

		assertEquals(4,gt.getIndex("maggie"));

		gt.remove("homer");

		assertEquals(3,gt.getIndex("maggie"));

		int index = gt.add("lenny",6.0);
		assertEquals(4,index);
		assertEquals(4,gt.getIndex("lenny"));
	}

	@Test(expected=NullPointerException.class)
	public void testNullAdd() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.add(null,1.0);
	}

	@Test(expected=NullPointerException.class)
	public void testNullGetIndex() {
		GravityTracker<String> gt = new GravityTracker<String>();
		gt.getIndex(null);
	}
}
