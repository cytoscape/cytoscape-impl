package org.cytoscape.cycl.internal;

import org.cytoscape.cycl.CyCLPlatform;

import org.lwjgl.*;
import org.lwjgl.opencl.*;
import org.lwjgl.opencl.KHRICD.*;
import org.lwjgl.system.*;

import static org.lwjgl.opencl.CL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.*;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

class CyCLPlatformImpl implements CyCLPlatform {
  static List<CyCLPlatform> allPlatforms = null;

  long cl_platform_id;
  CLCapabilities capabilities = null;
  String name = null;
  MemoryStack stack;
  PointerBuffer ctxProps = null;

  long[] device_ids = null;

  public CyCLPlatformImpl(long platform) {
    this.cl_platform_id = platform;
    stack = stackPush();
    name = this.getPlatformInfoStringUTF8(CL10.CL_PLATFORM_NAME);
  }

  public String getName() { return name; }

  public CLCapabilities getCapabilities() {
    if (capabilities != null) return capabilities;

    capabilities = CL.createPlatformCapabilities(cl_platform_id);
    return capabilities;
  }

  public PointerBuffer getContextProps() {
    // if (ctxProps != null) return ctxProps;

    ctxProps = stack.mallocPointer(3);
    ctxProps
      .put(0, CL_CONTEXT_PLATFORM)
      .put(1, cl_platform_id)
      .put(2, NULL);

    // System.out.println("contextProps limit = "+ctxProps.limit());
    // System.out.println("Terminator = "+(ctxProps.get(ctxProps.limit() - 1) == NULL));

    return ctxProps;

  }

  public long[] getDevices(int type) {
    if (device_ids != null)
      return device_ids;

    try {
      IntBuffer pi = stack.mallocInt(1);

      // Get the number of devices
      CyCLUtils.checkCLError(clGetDeviceIDs(cl_platform_id, type, null, pi));

      PointerBuffer devices = stack.mallocPointer(pi.get(0));
      CyCLUtils.checkCLError(clGetDeviceIDs(cl_platform_id, type, devices, (IntBuffer)null));

      device_ids = new long[pi.get(0)];
      for (int d = 0; d < devices.capacity(); d++) { device_ids[d] = devices.get(d); }
    } catch (Exception e) {
      // Assume this platform doesn't have devices
      device_ids = null;
    }
    return device_ids;
  }

  public String getPlatformInfoStringASCII(int param_name) {
    try (MemoryStack stack = stackPush()) {
      PointerBuffer pp = stack.mallocPointer(1);
      CyCLUtils.checkCLError(clGetPlatformInfo(cl_platform_id, param_name, (ByteBuffer)null, pp));
      int bytes = (int)pp.get(0);

      ByteBuffer buffer = stack.malloc(bytes);
      CyCLUtils.checkCLError(clGetPlatformInfo(cl_platform_id, param_name, buffer, null));

      return memASCII(buffer, bytes - 1);
      }
  }

  public String getPlatformInfoStringUTF8(int param_name) {
    try (MemoryStack stack = stackPush()) {
      PointerBuffer pp = stack.mallocPointer(1);
      CyCLUtils.checkCLError(clGetPlatformInfo(cl_platform_id, param_name, (ByteBuffer)null, pp));
      int bytes = (int)pp.get(0);

      ByteBuffer buffer = stack.malloc(bytes);
      CyCLUtils.checkCLError(clGetPlatformInfo(cl_platform_id, param_name, buffer, null));

      return memUTF8(buffer, bytes - 1);
    }
  }

  public static List<CyCLPlatform> getPlatforms() {
    if (allPlatforms != null) return allPlatforms;

    MemoryStack mstack = stackPush();
    IntBuffer pi = mstack.mallocInt(1);
    CyCLUtils.checkCLError(clGetPlatformIDs(null, pi));
    if (pi.get(0) == 0) {
      throw new RuntimeException("No OpenCL platforms found");
    }

    System.out.println("Found "+pi.get(0)+" platforms");

    PointerBuffer platforms = mstack.mallocPointer(pi.get(0));
    CyCLUtils.checkCLError(clGetPlatformIDs(platforms, (IntBuffer)null));

    allPlatforms = new ArrayList<CyCLPlatform>();

    for (int p = 0; p < platforms.capacity(); p++) {
      allPlatforms.add(new CyCLPlatformImpl(platforms.get(p))); 
    }
    return allPlatforms;
  }


}
