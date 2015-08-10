package org.cytoscape.work.internal.properties;

/*
 * #%L
 * org.cytoscape.work-impl
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2015 The Cytoscape Consortium
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

import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

public class LotsOfTunables {

    @ContainsTunables
    public Line line = new Line();
    
    
    @Tunable
    public YesNoMaybe yesNoMaybe = YesNoMaybe.YES;
    
    @Tunable
    public boolean why = true;
    
    @Tunable
    public String str = "This is a string";

    @Tunable 
    public int primitiveInt = 0;
    
    @Tunable
    public Integer integer = 5;
    
    
    @Tunable
    public ListSingleSelection<Integer> listSingleNumbers = new ListSingleSelection<>(1,2,3,4,5);
    
    @Tunable
    public ListMultipleSelection<String> listMultipleNames = new ListMultipleSelection<>("Fred", "Mark", "Max", "Nancy", "Ester");
    
    @Tunable
    public ListSingleSelection<YesNoMaybe> listSingleEnum = new ListSingleSelection<>(YesNoMaybe.YES, YesNoMaybe.NO, YesNoMaybe.MAYBE);

    @Tunable
    public BoundedInteger intRange = new BoundedInteger(0, 5, 9, true, true);
    
    @Tunable
    public BoundedDouble doubleRange = new BoundedDouble(0.0, 0.5, 1.0, true, true);

    
    // The following tunables will be missing
    @Tunable 
    public int missing;
    
    @Tunable
    public ListSingleSelection<Integer> missingListSS = new ListSingleSelection<>(1,2,3,4);
    
    @Tunable
    public ListMultipleSelection<String> missingNames = new ListMultipleSelection<>("Fred", "Mark", "Max", "Nancy", "Ester");
    
    @Tunable
    public int missingValue;
    
    

    
    
}
