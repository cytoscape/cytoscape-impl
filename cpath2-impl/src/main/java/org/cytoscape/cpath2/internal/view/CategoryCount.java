package org.cytoscape.cpath2.internal.view;

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
