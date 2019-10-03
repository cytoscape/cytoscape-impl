package org.cytoscape.search.internal.util;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class EnhancedSearchUtilsTest {
	
	@Test
	public void testReplaceWhiteSpace() {
		
		// null
		assertEquals("", EnhancedSearchUtils.replaceWhitespace(null));
		
		// empty string
		assertEquals("", EnhancedSearchUtils.replaceWhitespace(""));

		// lots of white space
		assertEquals("_hi__how_are_you", EnhancedSearchUtils.replaceWhitespace(" hi  how are you "));
	}
	
	@Test
	public void testQueryToLowerCase() {
		// null
		try {
			EnhancedSearchUtils.queryToLowerCase(null);
			fail("Expected NullPointerException");
		}catch(NullPointerException npe) {
			
		}
		
		// empty string
		assertEquals("", EnhancedSearchUtils.queryToLowerCase(""));
		
		// multiple ands ors and nots
		assertEquals(" OR this AND that NOT how to", EnhancedSearchUtils.queryToLowerCase(" oR tHIS and that Not How To"));
		assertEquals(" OR thor is AND NOT that TO NOT how", EnhancedSearchUtils.queryToLowerCase(" oR tHor IS and not that To Not How"));
		
		
	}

}
