package org.cytoscape.work.internal.task;

import javax.swing.Icon;
import java.awt.event.ActionListener;

/**
* Manipulates an individual task panel's user interface.
* Calls to these methods don't have to be on the
* Swing thread.
*
* The task's user interface consists of these components:
* <ul>
* <li>The task title at the top of the task panel</li>
* <li>The progress bar below the title</li>
* <li>A cancel button next to the progress bar</li>
* <li>A message panel below the progress bar with the most recent message shown at the top</li>
* <li>A cancel status message field to the right of the message panel</li>
* </ul>
*/
interface TaskUI {
	/**
	* Hides the progress bar, the cancel button, and the cancel status field.
	*/
	public void setTaskAsCompleted();
	public void setTitle(String title);
	public void setProgress(float progress);
	public void addMessage(Icon icon, String msg);
	public void addCancelListener(ActionListener l);
	public void setCancelStatus(String status);
	public void disableCancelButton();
}
