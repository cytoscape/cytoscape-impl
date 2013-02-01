package org.cytoscape.cpath2.internal.view;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

/**
 * Created by IntelliJ IDEA.
 * User: cerami
 * Date: Nov 20, 2007
 * Time: 10:33:26 AM
 * To change this template use File | Settings | File Templates.
 */
class CategoryCount {
    private String categoryName;
    private int count;

    public CategoryCount (String categoryName, int count) {
        this.categoryName = categoryName;
        this.count = count;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCount() {
        return count;
    }

    public String toString() {
        return categoryName + ":  " + count;
    }
}
