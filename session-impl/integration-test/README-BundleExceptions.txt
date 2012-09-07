NOTE: If the integration tests fail with a bundle loading error like:

ERROR: Bundle org.cytoscape.ding-presentation-impl [69] Error starting file:/tmp/1346975522850-0/bundles/org.cytoscape.ding-presentation-impl_3.0.0.alpha10-SNAPSHOT.jar (org.osgi.framework.BundleException: Unresolved constraint in bundle org.cytoscape.ding-presentation-impl [69]: Unable to resolve 69.0: missing requirement [69.0] osgi.wiring.package; (&(osgi.wiring.package=org.cytoscape.view.presentation.customgraphics)(version>=3.0.0)(!(version>=4.0.0))))
org.osgi.framework.BundleException: Unresolved constraint in bundle org.cytoscape.ding-presentation-impl [69]: Unable to resolve 69.0: missing requirement [69.0] osgi.wiring.package; (&(osgi.wiring.package=org.cytoscape.view.presentation.customgraphics)(version>=3.0.0)(!(version>=4.0.0)))
  at org.apache.felix.framework.Felix.resolveBundleRevision(Felix.java:3826)
  at org.apache.felix.framework.Felix.startBundle(Felix.java:1868)
  at org.apache.felix.framework.Felix.setActiveStartLevel(Felix.java:1191)
  at org.apache.felix.framework.FrameworkStartLevelImpl.run(FrameworkStartLevelImpl.java:295)
  at java.lang.Thread.run(Thread.java:662)

or something similar, it may be that a new dependency was added to Cytoscape that wasn't updated in session-impl/integration-test/src/test/java/org/cytoscape/session/BasicIntegrationTest.java.  Add the neccesary bundle in that file and see if it fixes the problem.

