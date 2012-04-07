/*
 Copyright (c) 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.graph.render.stateful;


import junit.framework.*;

import java.awt.Font;
import java.awt.font.*;
import java.awt.GraphicsEnvironment;
import java.util.List;


public class MeasuredLineCreatorTest extends TestCase {
	Font serif;
	Font sansSerif;
	FontRenderContext frc;
	MeasuredLineCreator mlc;


	public void setUp() {
		// For whatever reason, this will force a GraphicsEnvironment to be created,
		// which ensures that all of the tests work that make use of GlyphVectors
		// work as designed.
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		serif = new Font("Serif",Font.PLAIN,10);
		sansSerif = new Font("SansSerif",Font.BOLD,12);
		frc = new FontRenderContext(null,true,true);
	}

	public void testOneLine() {
		mlc = new MeasuredLineCreator("homer",serif,frc,2.0,true,100);
		printLines("one line",mlc);
		assertTrue(mlc.getMeasuredLines().size() == 1);
	}

	public void testOneNewLine() {
		mlc = new MeasuredLineCreator("homer\nmarge",serif,frc,2.0,true,100);
		printLines("one newline",mlc);
		assertTrue(mlc.getMeasuredLines().size() == 2);
	}

	public void testLongLine() {
		mlc = new MeasuredLineCreator("homer bart lisa marge",serif,frc,2.0,true,10);
		printLines("long line",mlc);
		assertTrue(mlc.getMeasuredLines().size() > 1);
	}

	public void testLongLineAndNewLines() {
		mlc = new MeasuredLineCreator("homer bart lisa marge\nmaggie\nsmithers",serif,frc,2.0,false,10);
		printLines("long line and newlines",mlc);
		assertTrue(mlc.getMeasuredLines().size() > 3);
	}

	public void testLongLineAndSpaces() {
		mlc = new MeasuredLineCreator("homer bart lisa marge          smithers",serif,frc,2.0,true,10);
		printLines("long line and spaces",mlc);
		assertTrue(mlc.getMeasuredLines().size() > 2);
	}

	public void testLongWord() {
		mlc = new MeasuredLineCreator("homerbartlisamargesmithers",serif,frc,2.0,false,10);
		printLines("long word",mlc);
		assertTrue(mlc.getMeasuredLines().size() == 1);
	}

	public void testWidthUpdate() {
		mlc = new MeasuredLineCreator("homer\nmarge",serif,frc,2.0,true,100);
		printLines("width update",mlc);
		double w = mlc.getMaxLineWidth();
		for ( MeasuredLine ml : mlc.getMeasuredLines() )
			if ( ml.getWidth() == w )
				return;
		
		fail("max line width should have updated");
	}

	public void testFirstLineNotEmpty() {
		mlc = new MeasuredLineCreator("homerbart lisa margesmithers",serif,frc,2.0,false,10);
		printLines("first line not empty",mlc);
		List<MeasuredLine> ml = mlc.getMeasuredLines(); 
		assertFalse( ml.get(0).getLine().equals("") );
	}

	public void testLastLineNotEmpty() {
		mlc = new MeasuredLineCreator("homerbart lisa margesmithers",serif,frc,2.0,false,10);
		printLines("last line not empty",mlc);
		List<MeasuredLine> ml = mlc.getMeasuredLines(); 
		assertFalse( ml.get(ml.size()-1).getLine().equals("") );
	}

	public void testTotalHeight() {
		mlc = new MeasuredLineCreator("homerbart lisa margesmithers",serif,frc,2.0,false,10);
		printLines("total height",mlc);
		double h = mlc.getTotalHeight();
		double total = 0;
		for ( MeasuredLine ml : mlc.getMeasuredLines() )
			total += ml.getHeight();

		assertEquals( total, h, 0.001 ); 
	}

	public void testRespectFontHeight() {
		mlc = new MeasuredLineCreator("homerbart lisa margesmithers",serif,frc,2.0,false,10);
		double h1 = mlc.getTotalHeight();
		printLines("respect font height 1",mlc);
		mlc = new MeasuredLineCreator("homerbart lisa margesmithers",sansSerif,frc,2.0,false,10);
		double h2 = mlc.getTotalHeight();
		printLines("respect font height 2",mlc);

		assertTrue( h1 < h2 );
	}

	public void testRespectOverallWidthLimit() {
		mlc = new MeasuredLineCreator("homer marge bart lisa maggie smithers",
		                              serif,frc,2.0,false,50.0);
		double mw = mlc.getMaxLineWidth();
		printLines("respect overall width",mlc);
		
		assertTrue( mw < (50.0*2.0) );
	}

	private void printLines(String title, MeasuredLineCreator mlx) {
		System.out.println("------------------------- " + title);
		System.out.println("max line width: " + mlx.getMaxLineWidth());
		System.out.println("total height  : " + mlx.getTotalHeight());
		for ( MeasuredLine ml : mlc.getMeasuredLines() )
			System.out.println(ml.toString());
		System.out.println();
	}
}
