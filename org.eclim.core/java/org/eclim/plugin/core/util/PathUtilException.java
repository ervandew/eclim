package org.eclim.plugin.core.util;

public class PathUtilException extends Exception
{
  private static final long serialVersionUID = 1564613385435L;

  public PathUtilException(String message)
  {
    super(message);
  }

  public PathUtilException(String message, Exception exception)
  {
    super(message, exception);
  }
}
