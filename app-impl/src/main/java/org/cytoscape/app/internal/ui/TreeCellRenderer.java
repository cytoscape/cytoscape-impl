/**
 * 
 */
package org.cytoscape.app.internal.ui;

import javax.swing.tree.DefaultTreeCellRenderer;

import org.cytoscape.app.internal.DownloadableInfo;
import org.cytoscape.app.internal.action.AppManagerAction;

/**
 * @author skillcoy
 * 
 */
public class TreeCellRenderer extends DefaultTreeCellRenderer {

	javax.swing.Icon warningIcon;

	javax.swing.Icon okIcon;

	public TreeCellRenderer(javax.swing.Icon warningLeafIcon,
			javax.swing.Icon okLeafIcon) {
		warningIcon = warningLeafIcon;
		okIcon = okLeafIcon;
	}

	public java.awt.Component getTreeCellRendererComponent(
			javax.swing.JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		if (((TreeNode) value).getObject() != null) {
			if (leaf && isOutdated(value)) {
				setIcon(warningIcon);
				setToolTipText("This app is not verified to work with the current version of Cytoscape.");
			} else if (leaf && !isOutdated(value)) {
				setIcon(okIcon);
				setToolTipText("Verified to work in "
						+ AppManagerAction.cyVersion.getVersion());
			} else {
				setToolTipText(null); // no tool tip
			}
		}
		return this;
	}

	private boolean isOutdated(Object value) {
		TreeNode node = (TreeNode) value;
		DownloadableInfo infoObj = node.getObject();
		if (infoObj != null && !infoObj.isAppCompatibleWithCurrent()) {
			return true;
		}
		return false;
	}

}
