package org.cytoscape.work;


import java.util.Properties;

import org.cytoscape.integration.ServiceTestSupport;
import org.cytoscape.property.BasicCyProperty;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.util.swing.FileUtil;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;


@RunWith(MavenConfiguredJUnit4TestRunner.class)
public class ServiceConfigurationTest extends ServiceTestSupport {

	@Before
	public void setup() {
		Properties coreP = new Properties();
		coreP.setProperty("cyPropertyName","cytoscape3.props");

		Properties p = new Properties();
		p.setProperty("cyPropertyName","bookmarks");

		CyProperty<Properties> cyProp =
			new BasicCyProperty(new Properties(), CyProperty.SavePolicy.CONFIG_DIR);
		registerMockService(CyProperty.class, cyProp, coreP);

		BookmarksCyProperty cyBookProp =
			new BookmarksCyProperty(new Bookmarks(), CyProperty.SavePolicy.CONFIG_DIR);
		registerMockService(CyProperty.class, cyBookProp, p);

		registerMockService(BookmarksUtil.class);
		registerMockService(FileUtil.class);
	}

	@Test
	public void testExpectedServices() {
		checkService(PanelTaskManager.class);
		checkService(DialogTaskManager.class);
		checkService(SubmenuTaskManager.class);
		checkService(GUITunableHandlerFactory.class);
		checkService(UndoSupport.class);
	}
}


/**
 * A simple implementation of CyProperty&lt;Properties&gt; suitable for
 * general purpose use.
 */
final class BookmarksCyProperty implements CyProperty<Bookmarks> {
        private final Bookmarks bookmarks;
        private final CyProperty.SavePolicy savePolicy;

        public BookmarksCyProperty(final Bookmarks bookmarks, final CyProperty.SavePolicy savePolicy)
	{
                if (bookmarks == null)
			throw new NullPointerException("\"bookmarks\" parameter is null.");
                if (savePolicy == null)
                        throw new NullPointerException("\"savePolicy\" parameter is null.");

                this.bookmarks = bookmarks;
                this.savePolicy = savePolicy;
        }

        public Bookmarks getProperties() {
                return bookmarks;

        }

        public CyProperty.SavePolicy getSavePolicy() {
                return savePolicy;
        }
}
