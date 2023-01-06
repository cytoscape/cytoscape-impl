package org.cytoscape.cycl.internal;

import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cycl.CyCLContext;
import org.cytoscape.cycl.CyCLDevice;
import org.cytoscape.cycl.CyCLFactory;
import org.cytoscape.cycl.CyCLPlatform;
import org.cytoscape.property.CyProperty;

public class CyCLFactoryImpl implements CyCLFactory {

  // Empty constructor for now
  public CyCLFactoryImpl() {
  }

  @Override
  public boolean isInitialized() {
    return CyCL.isInitialized();
  }

  @Override
	public List<CyCLDevice> getDevices() {
    return CyCL.getDevices();
  }

  @Override
	public CyCLDevice getDevice() {
    return CyCL.getDevices().get(0);
  }

  @Override
	public void makePreferred(String name) {
    CyCL.makePreferred(name);
  }

  @Override
  public CyCLContext createContext(CyCLDevice device) {
    CyCLPlatform platform = device.getPlatform();
    CyCLContext context = new CyCLContextImpl(platform, device);
    return context;
  }

}
