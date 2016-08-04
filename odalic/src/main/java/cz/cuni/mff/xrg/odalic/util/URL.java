package cz.cuni.mff.xrg.odalic.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.UriInfo;

/**
 * Utility class for -- you guessed it -- working with URLs.
 * 
 * @author Václav Brodec
 *
 */
public final class URL {

  /**
   * We want to keep this class uninstantiable, so no visible constructor is available.
   */
  private URL() {}

  /**
   * One-off function that essentially takes the absolute path of the request URI (encapsulated by
   * the {@link UriInfo} instance) and resolves the sub-resource name against it to get the absolute
   * path of the sub-resource.
   * 
   * @param requestUriInfo URI info
   * @param subResource a string to resolve against the URI
   * @return absolute path of the string
   * @throws MalformedURLException If a protocol handler for the URL could not be found, or if some
   *         other error occurred while constructing the URL
   * @throws IllegalStateException If called outside a scope of a request
   * @throws IllegalArgumentException If the given string violates RFC 2396
   */
  public static java.net.URL getSubResourceAbsolutePath(UriInfo requestUriInfo, String subResource)
      throws MalformedURLException, IllegalStateException {
    try {
      return requestUriInfo.getAbsolutePath()
          .resolve(URLEncoder.encode(subResource, StandardCharsets.UTF_8.displayName())).toURL();
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError(e);
    }
  }
}
