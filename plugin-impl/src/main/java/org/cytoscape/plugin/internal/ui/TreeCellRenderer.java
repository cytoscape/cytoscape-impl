/**
 * 
 */
package org.cytoscape.plugin.internal.ui;

import javax.swing.tree.DefaultTreeCellRenderer;

import org.cytoscape.plugin.internal.DownloadableInfo;
import org.cytoscape.plugin.internal.action.PluginManagerAction;

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
				setToolTipText("This plugin is not verified to work with the current version of Cytoscape.");
			} else if (leaf && !isOutdated(value)) {
				setIcon(okIcon);
				setToolTipText("Verified to work in "
						+ PluginManagerAction.cyVersion.getVersion());
			} else {
				setToolTipText(null); // no tool tip
			}
		}
		return this;
	}

	private boolean isOutdated(Object value) {
		TreeNode node = (TreeNode) value;
		DownloadableInfo infoObj = node.getObject();
		if (infoObj != null && !infoObj.isPluginCompatibleWithCurrent()) {
			return true;
		}
		return false;
	}

}
