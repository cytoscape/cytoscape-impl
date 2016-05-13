package org.cytoscape.internal.view;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Allows JUnit tests to run on the AWT thread.
 * Just add the annotation <code>@RunWith(SwingRunner.class)</code> to the test class.
 */
public class SwingRunner extends BlockJUnit4ClassRunner {

	public SwingRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	public void run(final RunNotifier notifier) {
		try {
			SwingUtilities.invokeAndWait(() -> {
				SwingRunner.super.run(notifier);
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
