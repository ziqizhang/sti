package cz.cuni.mff.xrg.odalic.util;

import java.net.MalformedURLException;

import javax.ws.rs.core.UriInfo;

public final class URL {
  private URL() {}

  public static java.net.URL getResourceUrL(UriInfo requestUriInfo, String resourceName)
      throws MalformedURLException {
    return requestUriInfo.getAbsolutePath().resolve(resourceName).toURL();
  }
}
